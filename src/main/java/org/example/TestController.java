package org.example;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class TestController {

    public static Thread worker=null;

    @RequestMapping("/start")
    public String start() {
        if(worker==null) {
            worker = new Thread(new LoggingAndMonitoring());
            worker.start();
            return "\nWorker started!";
        }else{
            return "Worker already running";
        }
    }

    @RequestMapping("/stop")
    public String stop() throws InterruptedException {
        synchronized (LoggingAndMonitoring.lock) {
            LoggingAndMonitoring.stopThread = true;
        }
        worker.join();
        worker=null;
        return "\nWorker stopped!";
    }

    @RequestMapping("/reset")
    public String reset() throws InterruptedException {
        synchronized (LoggingAndMonitoring.lock) {
            LoggingAndMonitoring.stopThread = true;
        }
        worker.join();

        LoggingAndMonitoring.causeError = false;
        LoggingAndMonitoring.errorJustOccurred = true;
        LoggingAndMonitoring.errorTS = Long.MAX_VALUE;
        LoggingAndMonitoring.stopThread = false;
        LoggingAndMonitoring.degree = -1;

        worker = new Thread(new LoggingAndMonitoring());
        worker.start();

        return "\nWorker reset!";
    }

    @RequestMapping("/err")
    public String cause_error(@RequestParam(name = "min", required = false, defaultValue = "0") Integer min) {
        synchronized (LoggingAndMonitoring.lock) {
            LoggingAndMonitoring.causeError = true;
            LoggingAndMonitoring.errorTS = System.currentTimeMillis() + min * 60 * 1000l;
            return "\nError happens at " + new Date(LoggingAndMonitoring.errorTS);
        }
    }
}
