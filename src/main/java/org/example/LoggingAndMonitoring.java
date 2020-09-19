package org.example;


import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
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

public class LoggingAndMonitoring implements  Runnable{

    static Logger logger = Logger.getLogger(LoggingAndMonitoring.class.getName());

    private static MonitoringClient monitoringClient = getMonitoringClient();

    public static void logging(String msg) {
//        try {
//            LogManager.getLogManager().readConfiguration(new FileInputStream("mylogging.properties"));
//        } catch (SecurityException | IOException e1) {
//            e1.printStackTrace();
//        }

        try {
            logger.log(Level.INFO, msg);
            // logger.log(Level.CONFIG, "Config data");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void setupLogger() throws IOException {
        logger.setLevel(Level.FINE);
        // logger.addHandler(new ConsoleHandler());
        logger.setUseParentHandlers(false);

        // adding custom handler
        logger.addHandler(new MyHandler());
        //FileHandler file name with max size and number of log files limit
        Handler fileHandler = new FileHandler("/home/opc/tmp/logger.log",
                Integer.MAX_VALUE/100, 4, true);
        fileHandler.setFormatter(new MyFormatter());
        //setting custom filter for FileHandler
        fileHandler.setFilter(new MyFilter());
        logger.addHandler(fileHandler);
    }

    public static void postMetricsToOci(String metricType, Double cpuOrMemUsage) {
        List<Datapoint> datapoints = new ArrayList<>();
        Datapoint dp = new Datapoint(new Date(), cpuOrMemUsage, 1);
        datapoints.add(dp);
        // MonitoringClient monitoringClient = getMonitoringClient();

        final PostMetricDataRequest request =
                PostMetricDataRequest.builder()
                        .postMetricDataDetails(
                                PostMetricDataDetails.builder()
                                        .metricData(
                                                Arrays.asList(
                                                        MetricDataDetails.builder()
                                                                .compartmentId(ConfigHolder.monitoringCompartmentId)
                                                                .namespace(ConfigHolder.namespace)
                                                                .name(metricType.equals("mem") ? ConfigHolder.mem_metric : ConfigHolder.cpu_metric)
                                                                .resourceGroup(ConfigHolder.resourceGroup)
                                                                .datapoints(datapoints)
                                                                .dimensions(makeMap(
                                                                        "region",
                                                                        Region.US_PHOENIX_1
                                                                                .getRegionId(),
                                                                        "host",
                                                                        "Oct_Demo"))
                                                                .metadata(makeMap("unit", metricType.equals("mem") ? "percentage" : " GBs"))
                                                                .build()))
                                        .build())
                        .build();

        try {
//            System.out.printf("Request constructed:\n%s\n\n", request.getPostMetricDataDetails());
//            System.out.println("Trying to post metrics...");
            final PostMetricDataResponse response = monitoringClient.postMetricData(request);
//            System.out.printf(
//                    "\n\nReceived response [opc-request-id: %s]\n", response.getOpcRequestId());
//            System.out.printf("%s\n\n", response.getPostMetricDataResponseDetails());
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
            final MonitoringClient monitoringClient = new MonitoringClient(getOciAuthProvider());
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

    private static BasicAuthenticationDetailsProvider getOciAuthProvider() throws IOException {
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

    static boolean newVersionDeployed = false;
    @Override
    public void run() {
        setup();

        long runInterval = (long) (2.0f*60*60*1000l);
        long startTimeInMillis = System.currentTimeMillis();
        long degree=-1;
        for(;System.currentTimeMillis() < runInterval + startTimeInMillis;){
            try {
                degree++;
                final boolean isOldAppVersion = (long) ((runInterval + startTimeInMillis) * 0.7f) > System.currentTimeMillis();
                Thread.sleep(1000);
                final String appBusinessPerfMsg = "Processed Account # %d accounts in last 1 sec::Success";

                if (isOldAppVersion) {
                    String logMsg = String.format(appBusinessPerfMsg, (int)random(300, 500) );
                    logging(logMsg);
                    postMetricsToOci("cpu", 30 + 10*mySine(degree));
                    postMetricsToOci("mem", 25 + 7 *mySine(degree));
                }else{
                    if(!newVersionDeployed){
                        newVersionDeployed = true;
                        logging("New app/config version deployed. New Version SHA d43858e15bb3f898d221c9501aee84dc19a336c0.");
                        logging("Previous version SHA for Rollback 5f9cb12485279767e85b3a85dfd992c512bc048e.");
                        logging("Deployment Engineer Email : goodDeveloper@example.com");
                        Thread.sleep(1000);
                    }
                    Thread.sleep(1000);
                    String logMsg = String.format(appBusinessPerfMsg, random(50, 100) );
                    logging(logMsg);
                    postMetricsToOci("cpu", 74 + 15*mySine(degree));
                    postMetricsToOci("mem", 48 + 10*mySine(degree));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void setup() {
        try {
            Thread.sleep(1000*5);
            setupLogger();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    static double random(int min, int max) {
        return  (min + (Math.random() * (max - min)));
    }

    static double mySine(double degrees){
        double radians = Math.toRadians(degrees+random(-20,20));
        return Math.sin(radians);
    }
}
