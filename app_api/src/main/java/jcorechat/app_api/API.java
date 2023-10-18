package jcorechat.app_api;


import jcorechat.app_api.accounts.AccountManager;
import jcorechat.app_api.security.Cription;
import jcorechat.app_api.security.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@SpringBootApplication
public class API {

    public static final short API_VERSION = 1;

    public static final Logger logger = LogManager.getRootLogger();
    public static final Yaml yaml = new Yaml();

    public static ConfigManager configManager;

    public static JwtService jwtService;

    public static Cription cription;

    public static AccountManager accountManager;

    public static HashMap<Long, String> emails = new HashMap<>();

    public static HashMap<Long, String> names = new HashMap<>();

    public static HashMap<Long, String> passwords = new HashMap<>();

    public static HashMap<Long, String> code = new HashMap<>();

    public static HashMap<Long, String> sessions = new HashMap<>();

    public static HashMap<Long, String> encryption_user_keys = new HashMap<>();

    public static HashMap<Long, String> sign_user_keys = new HashMap<>();

    public static Random random = new Random();

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

        // TODO per-user keying

        // TODO handle DDoS/brute force


        //  /api/v{VERSION}/friends

        //  /api/v{VERSION}/groups

        //  /api/v{VERSION}/chats

        //  /api/v{VERSION}/posts

        //  /api/v{VERSION}/profile

        // /api/v{VERSION}/account

        configManager = new ConfigManager();
        jwtService = new JwtService();
        cription = new Cription();
        accountManager = new AccountManager();


        /*
        URL url = new URL ("https://reqres.in/api/users");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        String jsonInputString = "{"name": "Upendra", "job": "Programmer"}";

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try(BufferedReader br = new BufferedReader(
          new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

         */

        long id = 1;

        emails.put(id, "test@email.smth");
        names.put(id, "My name is");
        passwords.put(id, "123");
        code.put(id, "IdK");

        String encryption_key = cription.generateUserKey();
        logger.info("User Encryption Key: "+encryption_key);
        encryption_user_keys.put(id, encryption_key);

        String sign_key = jwtService.generateRandomUserSign();

        logger.info("User Sign Key: "+sign_key);
        sign_user_keys.put(id, sign_key);


        SpringApplication app = new SpringApplication(API.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", configManager.getServerPort()));
        app.run(args);
    }
}