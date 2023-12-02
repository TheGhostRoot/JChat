package jchat.load_balancer;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class LoadBalancer {


    public static List<String> servers;

    public static long critical_ram;

    public static int critical_cpu;

    public static long critical_disk;

    public static final Logger logger = LogManager.getRootLogger();

    public static ServerLoad serverLoad;

    private static Yaml yaml = new Yaml();

    public static List<String> servers1 = new ArrayList<>();
    public static List<String> servers2 = new ArrayList<>();

    public static boolean useServer1 = false;

    public static List<Map<String, Object>> queue = new CopyOnWriteArrayList<>();

    public static RederectFilter rederectFilter;

    public static Thread rederectThread;

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



        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            serverLoad.setServerLoad();
        }, 0, 5, TimeUnit.MINUTES);

/*
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // smt
        }, "Shutdown-thread"));

         */

        /* config.yml
         *
         * port: 123141
         * servers: ["123123.123123.1313.3131:2413", "141.4.12412.4:1231"]
         * critical_ram_usage_bytes: 20_000_000_000
         * critical_cpu_usage_percent: 90
         * critical_disk_usage_bytes: null
         * */

        serverLoad = new ServerLoad();

        servers = readServersFromConfig();
        critical_ram = readCriticalRamFromConfig();
        critical_cpu = readCriticalCpuFromConfig();
        critical_disk = readCriticalDiskFromConfig();
        rederectFilter = new RederectFilter();

        serverLoad.setServerLoad();

        rederectThread = new Thread(() -> {
            while (true) {
                rederectFilter.sendRederect();
            }
        });
        rederectThread.setDaemon(true);
        rederectThread.start();

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

    public static List<String> readServersFromConfig() {
        try {
            List<String> servs =  (List<String>) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("servers");

            if (servs == null) {
                return new ArrayList<>();
            }

            return servs;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public static long readCriticalRamFromConfig() {
        try {
            return (long) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("critical_ram_usage_bytes");

        } catch (Exception e) {
            return 0l;
        }
    }

    public static int readCriticalCpuFromConfig() {
        try {
            return (int) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("critical_cpu_usage_percent");

        } catch (Exception e) {
            return 0;
        }
    }


    public static long readCriticalDiskFromConfig() {
        try {
            return (long) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("critical_disk_usage_bytes");

        } catch (Exception e) {
            return 0l;
        }
    }
}
