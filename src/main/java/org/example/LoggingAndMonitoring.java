package org.example;


import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.monitoring.MonitoringClient;
import com.oracle.bmc.monitoring.model.Datapoint;
import com.oracle.bmc.monitoring.model.MetricDataDetails;
import com.oracle.bmc.monitoring.model.PostMetricDataDetails;
import com.oracle.bmc.monitoring.requests.PostMetricDataRequest;
import com.oracle.bmc.monitoring.responses.PostMetricDataResponse;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingAndMonitoring {

    static Logger logger = Logger.getLogger(LoggingAndMonitoring.class.getName());

    // private static MonitoringClient monitoringClient = getMonitoringClient();

    public static void main(String[] args) {
//        try {
//            LogManager.getLogManager().readConfiguration(new FileInputStream("mylogging.properties"));
//        } catch (SecurityException | IOException e1) {
//            e1.printStackTrace();
//        }
        logger.setLevel(Level.FINE);
        // logger.addHandler(new ConsoleHandler());
        logger.setUseParentHandlers(false);

        // adding custom handler
        logger.addHandler(new MyHandler());
        try {
            //FileHandler file name with max size and number of log files limit
            Handler fileHandler = new FileHandler("/home/opc/tmp/logger.log",
                    Integer.MAX_VALUE, 4, true);
            fileHandler.setFormatter(new MyFormatter());
            //setting custom filter for FileHandler
            fileHandler.setFilter(new MyFilter());
            logger.addHandler(fileHandler);

            for (long i = 0; i < (10000000); i++) {
                //logging messages
                logger.log(Level.INFO, "Processed " + i + " users");
            }
            logger.log(Level.CONFIG, "Config data");
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void postMetricsToOci() {
        List<Datapoint> datapoints = new ArrayList<>();
        Datapoint dp = new Datapoint(new Date(), 50.0, 1);
        datapoints.add(dp);
        MonitoringClient monitoringClient = getMonitoringClient();

        final PostMetricDataRequest request =
                PostMetricDataRequest.builder()
                        .postMetricDataDetails(
                                PostMetricDataDetails.builder()
                                        .metricData(
                                                Arrays.asList(
                                                        MetricDataDetails.builder()
                                                                .compartmentId(ConfigHolder.monitoringCompartmentId)
                                                                .namespace(ConfigHolder.namespace)
                                                                .name(ConfigHolder.mem_metric)
                                                                .resourceGroup(ConfigHolder.resourceGroup)
                                                                .datapoints(datapoints)
                                                                //.dimensions(bulkMetric.dimensions)
                                                                .metadata(makeMap("unit", "percentage"))
                                                                .build()))
                                        .build())
                        .build();

        try {
            System.out.printf("Request constructed:\n%s\n\n", request.getPostMetricDataDetails());
            System.out.println("Trying to post metrics...");
            final PostMetricDataResponse response = monitoringClient.postMetricData(request);
            System.out.printf(
                    "\n\nReceived response [opc-request-id: %s]\n", response.getOpcRequestId());
            System.out.printf("%s\n\n", response.getPostMetricDataResponseDetails());
        } catch (Exception exception) {
            System.out.println("Error: Could not post these metrics ... Problematic Metrics are " + "\nDue to exception " + exception);
        }

    }

    private static Map<String, String> makeMap(String... data) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < data.length; i += 2) {
            map.put(data[i], data[i + 1]);
        }
        return map;
    }

    private static MonitoringClient getMonitoringClient() {
        try {
            final MonitoringClient monitoringClient = new MonitoringClient((BasicAuthenticationDetailsProvider) getMonitoringClient());
            monitoringClient.setEndpoint(ConfigHolder.monitoringServiceEndpoint);
            return monitoringClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Properties fetchProperties() {
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            // LOGGER.error(e.getMessage());
        }
        return properties;
    }

    private BasicAuthenticationDetailsProvider getOciAuthProvider() throws IOException {
        final InstancePrincipalsAuthenticationDetailsProvider provider;
        try {
            provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException
                    || e.getCause() instanceof ConnectException) {
                System.out.println(
                        "This sample only works when running on an OCI instance. Are you sure you’re running on an OCI instance? For more info see: https://docs.cloud.oracle.com/Content/Identity/Tasks/callingservicesfrominstances.htm");
                File file = ResourceUtils.getFile("~/.oci/config");
                final ConfigFileReader.ConfigFile configFile;
                configFile = ConfigFileReader.parse(file.getAbsolutePath(), ConfigHolder.monitoringProfileName);
                return
                        new ConfigFileAuthenticationDetailsProvider(configFile);
            }
            throw e;
        }
        return provider;
    }

}
