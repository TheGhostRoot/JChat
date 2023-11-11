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

    public static Random random = new Random();

    public static void main(String[] args) {

        // The client will handle all the hashing

        // It needs Java 17 or newer

        //  /api/v{VERSION}/friends

        //  /api/v{VERSION}/groups

        //  /api/v{VERSION}/chats

        //  /api/v{VERSION}/posts

        //  /api/v{VERSION}/profile

        // /api/v{VERSION}/account

        jwtService = new JwtService();
        cription = new Cription();
        accountManager = new AccountManager();
        captahaManager = new CaptahaManager();
        databaseManager = new DatabaseManager();

        databaseManager.setupMySQL();

        databaseHandler = new DatabaseHandler(databaseManager);

        long user_id = databaseHandler.createUser("John", "fwfewfw@text.com", "fwfwc3gwr",
                "wfwfwfwg3q23tg24fG", "FYGIUHOI8YT76R75E4");

        long user_id2 = databaseHandler.createUser("Bob", "fwfe22wfw@text.com", "fwf22wc3gwr",
                "wfwfwfw22g3q23tg24fG", "FYGIUH22OI8YT76R75E4");


        /*
        databaseHandler.addMessage(0, user_id, user_id2, "Hello");

        long channel_id = databaseHandler.getDMChannelID(user_id, user_id2);
        Map<String, List<Object>> all_messages = databaseHandler.getMessages(channel_id, 10);
        long msg_id = Long.parseLong(String.valueOf(all_messages.get("msg_id").get(0)));

        databaseHandler.addReaction(channel_id, msg_id, "Wow", user_id2, 0L);
        databaseHandler.addReaction(channel_id, msg_id, "Yaaa", user_id, 0L);

        databaseHandler.removeReaction(channel_id, msg_id, "Wow", user_id2, 0L);

        databaseHandler.createPost(user_id, "My first Post", "No background");

        Map<String, List<Object>> posts = databaseHandler.getLatestPosts(1);
        long post_id = Long.parseLong(String.valueOf(posts.get("id").get(0)));

        databaseHandler.addCommentToPost(user_id, post_id, "My comment");

        Map<String, List<Object>> all_comments = databaseHandler.getLatestCommentsOnPost(post_id, 1);
        Long msg_id = null;
        int i = 0;
        for (Object sender_id : all_comments.get("send_by")) {
            if (Long.valueOf(String.valueOf(sender_id)) == user_id) {
                msg_id = Long.valueOf(String.valueOf(all_comments.get("msg_id").get(i)));
            }
            i++;
        }

        databaseHandler.updateProfilePfp(user_id, "new pfp");
        databaseHandler.updateProfileBadges(user_id, "new Badges");
        databaseHandler.updateProfileBanner(user_id, "new banner");
        databaseHandler.updateProfileAnimations(user_id, "new animations");
        databaseHandler.updateProfileCoins(user_id, 100);
        databaseHandler.updateProfilePets(user_id, "My God");*/










        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            //databaseManager.handleCaptchas();
            //databaseManager.handleSessions();
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

}