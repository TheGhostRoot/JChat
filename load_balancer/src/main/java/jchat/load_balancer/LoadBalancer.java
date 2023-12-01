package jchat.load_balancer;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadBalancer {


    public static final Logger logger = LogManager.getRootLogger();

    private static Yaml yaml = new Yaml();

    public static void main(String[] args) {

        // TODO Add DDoS protection

        // Each user that uses the APP product will have 1 request to the server per second
        // Allow every request from IP address that is not blocked
        // Allow every request from IP address which sends 1 request per second or slower
        // Otherwise, block them
        // All the blocked IP address will be cleared in some time

        // Now attackers can't send too many requests, because they will be blocked,
        // and they can't send too little requests, because the server will handle it and it won't shut them down.

        // The system needs only 2 requests to block an IP
        // 1 is the original which will pass the protection, because it will be the first one that the server will see.
        // 2 is the request which the server says if it will be blocked or not.

        // So if an attacker has 1mil. bots to attack it then the server will only pass 1mil.
        // requests and block the rest, because they are send to fast.

        // if the API servers get very busy then the Load Balancer will start a queue and this protection applies there as well.


        /*
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            // smt
        }, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // smt
        }, "Shutdown-thread"));

         */

        /* config.yml
         *
         * port: 123141
         * */

        SpringApplication app = new SpringApplication(LoadBalancer.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", readPortFromConfig()));
        app.run(args);
    }

    public static int readPortFromConfig() {
        try {
            return (int) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("port");

        } catch (Exception e) {
            return 25533;
        }
    }
}
