package jcorechat.app_api;


import jakarta.servlet.http.HttpServletRequest;
import jcorechat.app_api.accounts.AccountManager;
import jcorechat.app_api.captchas.CaptahaManager;
import jcorechat.app_api.database.DatabaseHandler;
import jcorechat.app_api.database.DatabaseManager;
import jcorechat.app_api.security.Cription;
import jcorechat.app_api.security.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class API {

    public static final short API_VERSION = 1;

    public static final Logger logger = LogManager.getRootLogger();

    public static JwtService jwtService;

    public static Cription cription;

    public static AccountManager accountManager;

    public static CaptahaManager captahaManager;

    public static DatabaseManager databaseManager;

    public static DatabaseHandler databaseHandler;






    // user id : email
    public static HashMap<Long, String> emails = new HashMap<>();

    // user id : user name

    public static HashMap<Long, String> names = new HashMap<>();

    // user id : password

    public static HashMap<Long, String> passwords = new HashMap<>();


    // user ID : session ID
    public static HashMap<Long, Long> sessions = new HashMap<>();

    // user id : encryption key
    public static HashMap<Long, String> encryption_user_keys = new HashMap<>();



    // user id : sign key
    public static HashMap<Long, String> sign_user_keys = new HashMap<>();

    // captcha ID : Captcha answer
    public static HashMap<Long, HashSet<String>> captcha_results = new HashMap<>();


    // captcha ID : 30 seconds expire
    public static HashMap<Long, Short> captcha_expire = new HashMap<>();


    // captcha ID : 3 fails to remove captcha
    public static HashMap<Long, Short> captcha_fails = new HashMap<>();

    // session ID : expiration time
    public static HashMap<Long, Short> session_expire = new HashMap<>();






    // session ID : is it suspended
    public static HashMap<Long, Boolean> session_suspention = new HashMap<>();

    public static Random random = new Random();

    public static final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

    public static void main(String[] args) {

        // The client will handle all the hashing

        // It needs Java 17 or newer

        //  /api/v{VERSION}/friends

        //  /api/v{VERSION}/groups

        //  /api/v{VERSION}/chats

        //  /api/v{VERSION}/posts

        //  /api/v{VERSION}/profile

        //  /api/v{VERSION}/account

        // TODO add `shop` and refactor all functions
        /*
        * Map<String, Object> admin_role = new HashMap<>();
        admin_role.put("update_group_settings", true);
        admin_role.put("update_group_name", true);
        admin_role.put("update_group_logo", true);
        admin_role.put("update_group_banner", true);
        admin_role.put("update_group_settings", true);
        admin_role.put("update_group_events", true);
        admin_role.put("kick", true);
        admin_role.put("ban", true);
        admin_role.put("update_member_roles", true);
        admin_role.put("update_member_nickname", true);
        admin_role.put("create_channel", true);
        admin_role.put("delete_channel", true);
        admin_role.put("update_channel_name", true);
        admin_role.put("update_channel_permissions", true);
        admin_role.put("update_channel_type", true);
        admin_role.put("update_channel_category", true);
        admin_role.put("create_role", true);
        admin_role.put("delete_role", true);
        admin_role.put("update_role_name", true);
        admin_role.put("update_role_permissions", true);
        admin_role.put("update_role_type", true);
        admin_role.put("create_category", true);
        admin_role.put("delete_category", true);
        admin_role.put("update_category_name", true);
        admin_role.put("update_category_type", true);
        * */

        jwtService = new JwtService();
        cription = new Cription();
        accountManager = new AccountManager();
        captahaManager = new CaptahaManager();
        databaseManager = new DatabaseManager();

        databaseManager.setupMySQL();

        databaseHandler = new DatabaseHandler(databaseManager);

        long user_id = databaseHandler.createUser("John", "john@mail.com", "YCRTUVYIUHOIOugy",
                "TCVYBUOINPNHug76", "H7G86F8giuo");

        long user_id2 = databaseHandler.createUser("Boby", "boby@mail.com", "YCRTUVYIUHO2IOugy",
                "TCVYBUO2INPNHug76", "H7G826F8giuo");

        long user_id3 = databaseHandler.createUser("MC", "mc@mail.com", "YCR1TUVYIUHO2IOugy",
                "TCVYB1UO2INPNHug76", "H7G1826F8giuo");


        // account, messages, friends, reactions, captcha, posts, post comments, profiles,  - work
        // general groups, groups add/remove member, general role, group channels, group categories - work
        // all permissions, shop - work
        // mongo





        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            //databaseHandler.handleCaptchas();
            //databaseHandler.handleSessions();
        }, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            databaseManager.shutDown();
        }, "Shutdown-thread"));

        SpringApplication app = new SpringApplication(API.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", 25533));
        app.run(args);
    }

    public static String get_IP(HttpServletRequest request) {
        String IP = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(IP) ? "127.0.0.1" : IP;
    }

    public static String generateKey(int big) {
        StringBuilder sb = new StringBuilder(big);

        for (int i = 0; i < big; i++) {
            // Math.random();
            sb.append(AlphaNumericString.charAt((int) (AlphaNumericString.length() * random.nextDouble())));
        }

        return sb.toString();
    }

    public static String generateKey(List<String> toContain, int big) {
        String key = generateKey(big);
        while (toContain.contains(key)) {
            key = generateKey(big);
        }
        return key;
    }


    public static Map<String, Object> getDataFromMap(Map<String, List<Object>> originalMap, String key, Object toSeach) {
        Map<String, Object> resultMap = new HashMap<>();
        if (originalMap == null || toSeach == null || key == null) {
            return resultMap;
        }
        if (originalMap.containsKey(key)) {
            int index = originalMap.get(key).indexOf(toSeach);
            if (index != -1) {
                for (Map.Entry<String, List<Object>> entry : originalMap.entrySet()) {
                    try {
                        resultMap.put(entry.getKey(), entry.getValue().get(index));
                    } catch (Exception e) {
                    }
                }
            }
        }

        return resultMap;
    }


}