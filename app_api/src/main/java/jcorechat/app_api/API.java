package jcorechat.app_api;


import jcorechat.app_api.security.Cription;
import jcorechat.app_api.security.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;

@SpringBootApplication
public class API {

    public static final short API_VERSION = 1;

    public static final Logger logger = LogManager.getRootLogger();
    public static final Yaml yaml = new Yaml();

    public static ConfigManager configManager;

    public static JwtService jwtService;

    public static Cription cription;

    public static void main(String[] args) {

        // TODO use custom configs
        // TODO make log4j2.xml custom
        // TODO finish endpoints
        // TODO make connection with SQL and NoSQL database based on the config
        // TODO research more options for SpringApplication properties for more customization
        // TODO when this API is done make it more custom based on config
        // TODO think of every secure way of login in and acccount stuff

        // TODO the ...Manager  job is to make connection between REST Controller and Database or something that it
        //  will get it's data

        // TODO the ...Controller  job is to handle REST API requests and run some security checks before passing the
        //  given data to the Manager


        //  /api/v{VERSION}/friends

        //  /api/v{VERSION}/groups

        //  /api/v{VERSION}/chats

        //  /api/v{VERSION}/posts

        //  /api/v{VERSION}/profile

        // /api/v{VERSION}/account

        configManager = new ConfigManager();
        jwtService = new JwtService();
        cription = new Cription();

        SpringApplication app = new SpringApplication(API.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", configManager.getServerPort()));
        app.run(args);
    }
}