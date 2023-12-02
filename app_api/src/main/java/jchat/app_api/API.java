package jchat.app_api;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.database.DatabaseHandler;
import jchat.app_api.database.DatabaseManager;
import jchat.app_api.security.CriptionService;
import jchat.app_api.security.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class API {

    public static final short API_VERSION = 1;

    public static final Logger logger = LogManager.getRootLogger();

    public static JwtService jwtService;

    public static CriptionService criptionService;

    public static DatabaseManager databaseManager;

    public static DatabaseHandler databaseHandler;



    public static String DB_SIGN_KEY = "sign_key";
    public static String DB_ENCRYP_KEY = "encryption_key";
    public static String REQ_HEADER_AUTH = "Authorization";
    public static String REQ_HEADER_SESS = "SessionID";
    public static String REQ_HEADER_CAPTCHA = "CapctchaID";


    public static String stats = "online";


    

    public static Random random = new Random();

    private static Yaml yaml = new Yaml();

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

        // the post channel is 0
        // the first ever post will have 1 comment to avoid msg_id: 1 post_id: 1
        // group's animations and rules will be in the settings

        /*
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
        admin_role.put("react", true);
        admin_role.put("delete_others_message", true);
        admin_role.put("delete_own_message", true);
        admin_role.put("send_message", true);
        admin_role.put("edit_own_message", true);


        user settings types:
        change_email
        change_password
        start_sub
        end_sub
        new_message
        edited_message
        new_coins
        new_badges
        deleted_group
        new_owner
        leave
        friend_requests
        * */

        jwtService = new JwtService();
        criptionService = new CriptionService();
        databaseManager = new DatabaseManager();

        databaseManager.setupMongoDB();

        databaseHandler = new DatabaseHandler(databaseManager);

        /*
        long user_id = databaseHandler.createUser("John", "john@mail.com", "YCRTUVYIUHOIOugy",
                "TCVYBUOINPNHug76", "H7G86F8giuo");

        long user_id2 = databaseHandler.createUser("Boby", "boby@mail.com", "YCRTUVYIUHO2IOugy",
                "TCVYBUO2INPNHug76", "H7G826F8giuo");

        long user_id3 = databaseHandler.createUser("MC", "mc@mail.com", "YCR1TUVYIUHO2IOugy",
                "TCVYB1UO2INPNHug76", "H7G1826F8giuo");

        databaseHandler.addMessage(0l, user_id, user_id2, "Yes", 0L);
        long dm_channel_id = databaseHandler.getDMChannelID(user_id, user_id2);
        long msg_id = (long) ((List<Map<String, Object>>) databaseHandler.getMessages(dm_channel_id, 1).get("msgs").get(0)).get(0).get("msg_id");
        long msg_id = (long) databaseHandler.getMessages(dm_channel_id, 1).get("msg_id").get(0);
        databaseHandler.addReaction(dm_channel_id, msg_id, 0l, "Cool", user_id2, 0l);
        databaseHandler.deleteMessage(user_id, dm_channel_id, msg_id, user_id, 0l);

        Map<String, Object> channel_override = new HashMap<>();

        Map<String, Object> member_override = new HashMap<>();
        member_override.put("react", false);
        member_override.put("delete_others_message", false);
        member_override.put("delete_own_message", false);
        member_override.put("send_message", true);

        channel_override.put(String.valueOf(admin_role_id), member_override);

        long channel_id_overrides = databaseHandler.createGroupChannel(group_id, user_id, "default", "Override channel",
                jwtService.generateJwtForDB(channel_override), "Owner created channel", ","+category_id);



        databaseHandler.createGroup(user_id, "My group", "logo", "banner", "animations");
        long group_id = (long) databaseHandler.getAllGroupsWithUser(user_id, 1).get("id").get(0);

        Map<String, Object> admin_role = new HashMap<>();
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
        admin_role.put("react", true);
        admin_role.put("delete_others_message", true);
        admin_role.put("delete_own_message", true);
        admin_role.put("send_message", true);

        Map<String, Object> member_role = new HashMap<>();
        member_role.put("react", false);
        member_role.put("delete_others_message", false);
        member_role.put("delete_own_message", false);
        member_role.put("send_message", false);

        Map<String, Object> group_settings = new HashMap<>();
        group_settings.put("join_group", true);

        databaseHandler.updateGroupSettings(user_id, group_id, jwtService.generateJwtForDB(group_settings),
                "Owner updated settings");


        databaseHandler.addGroupMember(user_id2, group_id, "", null, "Joined the server");
        databaseHandler.addGroupMember(user_id3, group_id, "", null, "Joined the server");

        long admin_role_id = databaseHandler.createGroupRole(user_id, group_id, "Admin",
                jwtService.generateJwtForDB(admin_role),
                "admin", "Owner created admin role");

        long member_role_id = databaseHandler.createGroupRole(user_id, group_id, "Member",
                jwtService.generateJwtForDB(member_role),
                "default", "Owner created member role");

        databaseHandler.updateMemberRoles(user_id2, group_id, admin_role_id, user_id, true, "Owner gave admin");
        databaseHandler.updateMemberRoles(user_id3, group_id, member_role_id, user_id2, true, "Admin gave member");

        // user - owner
        // user2 - admin
        // user3 - member

        databaseHandler.createGroupCategory(user_id2, group_id, "My category", "default",
                "Admin created category");

        long category_id = (long) ((List<Map<String, Object>>) databaseHandler.getAllGroupsWithUser(user_id3,
                1).get("categories").get(0)).get(0).get("category_id");

        //long category_id = (long) databaseHandler.getAllStuffFromGroupSQL(group_id, 1).get("category_category_id").get(0);


        long channel_id = databaseHandler.createGroupChannel(group_id, user_id, "default", "My channel",
                "", "Owner created channel", ","+category_id);
        databaseHandler.addMessage(channel_id, user_id, 0L,"Hello :)", group_id);

        long msg_id = (long) ((List<Map<String, Object>>) databaseHandler.getMessages(channel_id, 1).get("msgs").get(0)).get(0).get("msg_id");
        //long msg_id = (long) databaseHandler.getMessages(channel_id, 1).get("msg_id").get(0);

        databaseHandler.addReaction(channel_id, msg_id, 0l, "Lol", user_id2, group_id);
        databaseHandler.deleteGroup(group_id);

        databaseHandler.createFriendRequest(user_id, user_id2);

        logger.info(databaseHandler.checkFriendRequest(user_id, user_id2));

        databaseHandler.deleteFriendRequest(user_id2, user_id);

        logger.info(databaseHandler.checkFriendRequest(user_id, user_id2)); */

        /* config.yml
        *
        * port: 123141
        * encryption_key: "DR6TYFUVYBIunohg86"
        * jwt_sign_key: "46DT7FYBIUNOIMPijhuyg86f5"
        * */


        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            databaseHandler.handleCaptchas();
            databaseHandler.handleSessions();
        }, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            databaseManager.shutDown();
        }, "Shutdown-thread"));

        SpringApplication app = new SpringApplication(API.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", readPortFromConfig()));
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

    public Object get_Key_By_Value(HashMap<?, ?> map, Object val) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue().equals(val)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Long getUserID_SessionOnly(HttpServletRequest request) {
        // only session
        String header_sess_id = request.getHeader(REQ_HEADER_SESS);
        if (header_sess_id == null) {
            return null;
        }

        return databaseHandler.getUserIDbySessionID(Long.parseLong(criptionService.GlobalDecrypt(header_sess_id)),
                get_IP(request));
    }


    public static boolean checkIfSolvedCaptcha(HttpServletRequest request) {
        long captch_id;
        try {
            captch_id = Long.parseLong(criptionService.GlobalDecrypt(request.getHeader(REQ_HEADER_CAPTCHA)));
        } catch (Exception e) { return false; }

        return API.databaseHandler.checkIfSolvedCaptcha(captch_id);
    }

    public static int readPortFromConfig() {
        try {
            return (int) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("port");

        } catch (Exception e) {
            return 25533;
        }
    }

    public static String readGlobalEncryptionKeyFromConfig() {
        try {
            String key = (String) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("encryption_key");

            if (key == null) {
                return "P918nfQtYhbUzJVbmSQfZw==";
            }

            return key;

        } catch (Exception e) {
            return "P918nfQtYhbUzJVbmSQfZw==";
        }
    }

    public static String readGlobalSignFromConfig() {
        try {
            String key = (String) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("jwt_sign_key");

            if (key == null) {
                return "hGqlbRo8IbgSh24eblzVZWnOk9Iue9cXKegLhnHAGyKV9HkKhmYQPE2QBpxfJmfri9UO7iAj9mZhJhm6E4Fx4Wxv5m/cHaxKASn0duiwBMHYt0ZEa6ViOFr2b62hVBfSQS3xvC0XDqRx+5rAG+vDwvoAUTSsT9Owhd9KJnrWEmJv0rrpY0+4qQbcRKbPhWJrB3ULWjnQuRvJS2Hwr7P/AvIrnFngC9QtNDOvLj/lzG9gHA5MSHws+/a2ZAe2mAI0AAvfYEPwemZy0r9JhHhqi+zcpFTarRqTEP51fXtjwRSoLgcbXxIbh5awM6h05+83NQV8L3cMfpANOyNATO/bBqzg+nU+y69AtVmpjXZpMaqXFAhUqVoVsuHP2Nc6UhPfjkps5Pt6Ho2kjEJotf1cDBXX6RTTxhJ95aL/lHKpNVw/sEBuzwyOqFwp1BMNuzED";
            }

            return key;

        } catch (Exception e) {
            return "hGqlbRo8IbgSh24eblzVZWnOk9Iue9cXKegLhnHAGyKV9HkKhmYQPE2QBpxfJmfri9UO7iAj9mZhJhm6E4Fx4Wxv5m/cHaxKASn0duiwBMHYt0ZEa6ViOFr2b62hVBfSQS3xvC0XDqRx+5rAG+vDwvoAUTSsT9Owhd9KJnrWEmJv0rrpY0+4qQbcRKbPhWJrB3ULWjnQuRvJS2Hwr7P/AvIrnFngC9QtNDOvLj/lzG9gHA5MSHws+/a2ZAe2mAI0AAvfYEPwemZy0r9JhHhqi+zcpFTarRqTEP51fXtjwRSoLgcbXxIbh5awM6h05+83NQV8L3cMfpANOyNATO/bBqzg+nU+y69AtVmpjXZpMaqXFAhUqVoVsuHP2Nc6UhPfjkps5Pt6Ho2kjEJotf1cDBXX6RTTxhJ95aL/lHKpNVw/sEBuzwyOqFwp1BMNuzED";
        }
    }
}