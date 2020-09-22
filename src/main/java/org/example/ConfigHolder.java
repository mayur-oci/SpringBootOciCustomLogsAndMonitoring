package org.example;

import com.oracle.bmc.Region;

public class ConfigHolder {


    static public String ociConfigFilePath = "./resources/config";


    public static String monitoringProfileName = "MAYUR_ADMIN_PHX";

    static public String monitoringServiceEndpoint = "https://telemetry-ingestion.us-phoenix-1.oraclecloud.com/";
    static public String monitoringCompartmentId = "ocid1.compartment.oc1..aaaaaaaaks6tggapluiw5egbylax2voxnga6e7qtt2ssyapnl4u3p2chydya";
    static public Region monitoringRegion = Region.US_PHOENIX_1;

    public static String namespace = "emedia_compute_ns";
    public static String mem_metric = "MemoryUtilization";
    public static String cpu_metric = "CPUUtilization";
    public static String resourceGroup = "EMediaResources";
}
