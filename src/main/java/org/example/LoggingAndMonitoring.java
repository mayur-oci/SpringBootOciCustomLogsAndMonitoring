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
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingAndMonitoring implements Runnable {

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

    private static FileHandler fileHandler;

    static {
        logger.setLevel(Level.FINE);
        // logger.addHandler(new ConsoleHandler());
        logger.setUseParentHandlers(false);

        // adding custom handler
        logger.addHandler(new MyHandler());
        //FileHandler file name with max size and number of log files limit
        try {
            fileHandler = new FileHandler("/home/opc/tmp/logger.log",
                    Integer.MAX_VALUE / 100, 4, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    static boolean causeError = false;
    static boolean errorJustOccurred = true;
    static long errorTS = Long.MAX_VALUE;
    static boolean stopThread = false;
    static long degree = -1;

    static public Object lock = new Object();

    @Override
    public void run() {


        while (true) {
            synchronized (lock) {
                if (stopThread) {
                    break;
                }
                try {
                    degree++;
                    Thread.sleep(500);
                    final String appBusinessPerfMsg = "Transcoded %d #videos in last 1 min::Success";

                    if (causeError == false || System.currentTimeMillis() < errorTS) {
                        if (degree % 35 == 0) {
                            String logMsg = String.format(appBusinessPerfMsg, (int) random(380, 500));
                            logging(logMsg);
                        }
                        postMetricsToOci("cpu", 36 + 10 * mySine1(degree + 90));
                        postMetricsToOci("mem", 30 + 7 * mySine2(degree));
                    } else {
                        if (!errorJustOccurred) {
                            errorJustOccurred = true;
                            logging("New app/config version deployed. New Version SHA d43858e15bb3f898d221c9501aee84dc19a336c0.");
                            logging("Previous version SHA for Rollback 5f9cb12485279767e85b3a85dfd992c512bc048e.");
                            logging("Deployment Engineer Email : goodDeveloper@example.com");
                        }
                        Thread.sleep(500);
                        if (degree % 35 == 0) {
                            String logMsg = String.format(appBusinessPerfMsg, (int) random(50, 100));
                            logging(logMsg);
                        }
                        postMetricsToOci("cpu", 74 + 15 * mySine1(degree + 90));
                        postMetricsToOci("mem", 55 + 10 * mySine2(degree));
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    static double random(int min, int max) {
        return (min + (Math.random() * (max - min)));
    }

    static double mySine1(double degrees) {
        double radians = Math.toRadians(degrees + random(-100, 100));
        if ((System.currentTimeMillis() / 1000) % 200 != 0)
            return Math.sin(radians);
        else
            return (Math.sin(radians) + Math.cos(radians + 60)) / 2;
    }

    static double mySine2(double degrees) {
        double radians = Math.toRadians(degrees + random(-100, 100));
        if ((System.currentTimeMillis() / 1000) % 400 != 0)
            return Math.sin(radians);
        else
            return (Math.sin(radians) + Math.cos(radians + 45)) / 2;
    }
}
