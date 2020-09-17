package org.example;

import com.oracle.bmc.Region;

public class ConfigHolder {


    static public String ociConfigFilePath = "./resources/config";


    public static String monitoringProfileName = "MAYUR_ADMIN_PHX";

    static public String monitoringServiceEndpoint = "https://telemetry-ingestion.us-phoenix-1.oraclecloud.com/";
    static public String monitoringCompartmentId = "ocid1.compartment.oc1..aaaaaaaa2z4wup7a4enznwxi3mkk55cperdk3fcotagepjnan5utdb3tvakq";
    static public Region monitoringRegion = Region.US_PHOENIX_1;

    public static String namespace = "Compute";
    public static String mem_metric = "Memory Utilization";
    public static String cpu_metric = "CPU Utilization";
    public static String resourceGroup = "MuShop Resources";
}
