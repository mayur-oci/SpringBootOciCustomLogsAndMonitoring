package org.example;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class App {


    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

        TestController.worker = new Thread(new LoggingAndMonitoring());
        TestController.worker.start();
        SpringApplication.run(App.class, args);
    }
}
