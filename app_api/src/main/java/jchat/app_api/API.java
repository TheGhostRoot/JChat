package jchat.app_api;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.database.DatabaseHandler;
import jchat.app_api.database.DatabaseManager;
import jchat.app_api.security.CriptionService;
import jchat.app_api.security.JwtService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    public static FileSystemHandler fileSystemHandler;

    public static List<String> captcha_server;
    public static List<String> upload_server;
    public static int captcha_time;



    public static String DB_SIGN_KEY = "sign_key";
    public static String DB_ENCRYP_KEY = "encryption_key";
    public static String REQ_HEADER_AUTH = "Authorization";
    public static String REQ_HEADER_SESS = "SessionID";
    public static String REQ_HEADER_CAPTCHA = "CaptchaID";

    private static File tempDir;


    public static String secret;

    public static Random random = new Random();

    private static Yaml yaml = new Yaml();

    public static final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

    public static ObjectMapper objectMapper;

    public static void main(String[] args) {
        objectMapper = new ObjectMapper();

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

        secret = readSecretFromConfig();

        jwtService = new JwtService();
        criptionService = new CriptionService();
        databaseManager = new DatabaseManager();
        fileSystemHandler = new FileSystemHandler();

        String db = readDatabase();
        if (db.equalsIgnoreCase("mongo")) {
            databaseManager.setupMongoDB();

        } else if (db.equalsIgnoreCase("mysql")) {
            databaseManager.setupMySQL();

        } else if (db.equalsIgnoreCase("postgres")) {
            databaseManager.setupPostgresSQL();

        } else {
            System.exit(500);
        }

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
        * captcha_server: ["http://localhost:1111/captcha/"]
        * secret: "D6FT7GY8HUJIOhuygt"
        * db: "mongo"
        * captcha_time: 20
        * upload_servers: ["http://195.168.0.215/api/v1/profile"]
        * */





        captcha_server = readCaptchaServersFromConfig();
        upload_server = readUploadServersFromConfig();
        captcha_time = readCaptchaTimeConfig();


        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            databaseHandler.handleCaptchas();
            //databaseHandler.handleSessions();
        }, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            databaseManager.shutDown();
        }, "Shutdown-thread"));

        tempDir = new File("/temp/");
        tempDir.mkdirs();

        String settings = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwZnAiOiIiLCJiYW5uZXIiOltdLCJiaW8iOiIiLCJzdGF0cyI6Im9mZmxpbmUiLCJjaGFuZ2VfcGFzc3dvcmQiOnRydWUsInN0YXJ0X3N1YiI6dHJ1ZSwiZW5kX3N1YiI6dHJ1ZSwibmV3X21lc3NhZ2UiOnRydWUsImVkaXRlZF9tZXNzYWdlIjp0cnVlLCJuZXdfY29pbnMiOnRydWUsIm5ld19iYWRnZXMiOnRydWUsImRlbGV0ZWRfZ3JvdXAiOnRydWUsIm5ld19vd25lciI6dHJ1ZSwibGVhdmUiOnRydWUsImZyaWVuZF9yZXF1ZXN0cyI6dHJ1ZX0.rEdysn9gyAy40zkcxe5fbG1MEEtv89QV3aSNX4MIwjQ";
        String password = "dNA4MOFIqWy67mJCYYo3b25fIw8JsXUig.brHz42Dz1";

        long user_id1 = databaseHandler.createUser("Hello",
                "kriskata5000@gmail.com", password,
                criptionService.GlobalEncrypt(databaseHandler.generateUserEncryptionKey()),
                criptionService.GlobalEncrypt(databaseHandler.generateUserSignKey()),
                settings);

        long user_id2 = databaseHandler.createUser("Hi",
                "kriskata50@gmail.com", password,
                criptionService.GlobalEncrypt(databaseHandler.generateUserEncryptionKey()),
                criptionService.GlobalEncrypt(databaseHandler.generateUserSignKey()),
                settings);

        long user_id3 = databaseHandler.createUser("Hello2",
                "thegoldenmineplugin@gmail.com", password,
                criptionService.GlobalEncrypt(databaseHandler.generateUserEncryptionKey()),
                criptionService.GlobalEncrypt(databaseHandler.generateUserSignKey()),
                settings);

        /*
        long user_id3 = databaseHandler.createUser("Hello",
                "kriskata5000@gmail.com", "",
                criptionService.GlobalEncrypt(databaseHandler.generateUserEncryptionKey()),
                criptionService.GlobalEncrypt(API.databaseHandler.generateUserSignKey()),
                "");
        password: dNA4MOFIqWy67mJCYYo3b25fIw8JsXUig.brHz42Dz1
        password = 1

        settings = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwZnAiOiIiLCJiYW5uZXIiOltdLCJiaW8iOiIiLCJzdGF0cyI6Im9mZmxpbmUiLCJjaGFuZ2VfcGFzc3dvcmQiOnRydWUsInN0YXJ0X3N1YiI6dHJ1ZSwiZW5kX3N1YiI6dHJ1ZSwibmV3X21lc3NhZ2UiOnRydWUsImVkaXRlZF9tZXNzYWdlIjp0cnVlLCJuZXdfY29pbnMiOnRydWUsIm5ld19iYWRnZXMiOnRydWUsImRlbGV0ZWRfZ3JvdXAiOnRydWUsIm5ld19vd25lciI6dHJ1ZSwibGVhdmUiOnRydWUsImZyaWVuZF9yZXF1ZXN0cyI6dHJ1ZX0.rEdysn9gyAy40zkcxe5fbG1MEEtv89QV3aSNX4MIwjQ
         */

        SpringApplication app = new SpringApplication(API.class);
        Map<String, Object> config = new HashMap<>();
        config.put("server.port", readPortFromConfig());
        config.put("server.max-http-header-size", "10MB");
        config.put("spring.servlet.multipart.max-file-size", "10GB");
        config.put("spring.servlet.multipart.max-request-size", "10GB");
        //config.put("spring.web.resources.static-locations", "file:D:\\ToolBox\\Tools\\Kit\\JChat\\app_api\\public");


        app.setDefaultProperties(config);
        app.run(args);
    }

    public static String get_IP(HttpServletRequest request) {
        String IP = request.getRemoteAddr();
        String forwardedIp = request.getHeader("Forwarded_IP");
        String forwardedSecret = request.getHeader("Forwarded_Secret");
        if (forwardedIp != null &&
                forwardedSecret != null &&
                forwardedSecret.equals(secret)) {
            IP = forwardedIp;
        }
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

    public static int readCaptchaTimeConfig() {
        try {
            return (int) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("captcha_time");

        } catch (Exception e) {
            return 20;
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


    public static String readDatabase() {
        try {
            String key = (String) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("db");

            if (key == null) {
                return "mongo";
            }

            return key;

        } catch (Exception e) {
            return "mongo";
        }
    }

    public static String readGlobalSignFromConfig() {
        try {
            String key = (String) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("jwt_sign_key");

            if (key == null) {
                return "sE1MHHQ/R3LsxNeb3+Lr/xHHQAI83VvXk+YEsTqiNhsfNV7ihj+FcFvQW3pvieZtPKaMQw60vADIPEP0bM16WtycxtWTH0bevIXwWk/Kw+rCnI/mrOGKjSy9wFymceHCMwk03GNSWqBwzOLMrVCXIbFTZ8wNj1nQHHvrEU5Ihx3M=";
            }

            return key;

        } catch (Exception e) {
            return "sE1MHHQ/R3LsxNeb3+Lr/xHHQAI83VvXk+YEsTqiNhsfNV7ihj+FcFvQW3pvieZtPKaMQw60vADIPEP0bM16WtycxtWTH0bevIXwWk/Kw+rCnI/mrOGKjSy9wFymceHCMwk03GNSWqBwzOLMrVCXIbFTZ8wNj1nQHHvrEU5Ihx3M=";
        }
    }


    public static List<String> readCaptchaServersFromConfig() {
        try {
            List<String> servs = (List<String>) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("captcha_servers");

            if (servs == null) {
                return new ArrayList<>();
            }

            return servs;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static String readSecretFromConfig() {
        try {
            String secr =  (String) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("secret");

            if (secr == null) {
                return "345E6FT7g65fv5D6f687T75rtufgF587DFg86xruycTF74S6u5dfog78D7U5F88d6urtifudr6t7if675dtfuhi";
            }

            return secr;

        } catch (Exception e) {
            return "345E6FT7g65fv5D6f687T75rtufgF587DFg86xruycTF74S6u5dfog78D7U5F88d6urtifudr6t7if675dtfuhi";
        }
    }

    public static List<String> readUploadServersFromConfig() {
        try {
            List<String> servs =  (List<String>) ( (Map<String, Object>) yaml.load(new FileInputStream("config.yml")))
                    .get("upload_servers");

            if (servs == null) {
                return new ArrayList<>();
            }

            return servs;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public static boolean uploadBody(String server, String authHeader, String method, Map<String, Object> body) {
        if (upload_server == null || upload_server.isEmpty()) { return false; }

        try {
            URL url = new URL(server);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.addRequestProperty(REQ_HEADER_AUTH, authHeader);
            con.addRequestProperty("Content-Type", "application/json");
            con.addRequestProperty("Host", url.getHost());
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                osw.write(objectMapper.writeValueAsString(body));
                osw.flush();
            }

            // global encrypted

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while (null != (inputLine = in.readLine())) {
                response.append(inputLine);
            }

            in.close();
            return response.toString().equals("true");

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean uploadFile(String server, boolean video, long user_id, byte[] file) {
        if (upload_server == null || upload_server.isEmpty()) { return false; }

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(server + "?video="+video+"&id="+user_id);

            File f = new File(FileSystemHandler.generateName(tempDir) + (video ? ".mp4" : ".jpg"));
            Files.write(f.toPath(), file);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file", new FileBody(f));
            httpPost.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(httpPost);

            f.delete();

            return response.getStatusLine().getStatusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }



    public static String sendRequestToUploads(String server, String method, boolean isVideo, long user_id) {
        if (upload_server == null || upload_server.isEmpty()) { return null; }

        try {
            URL url = new URL(server + "?redirected=true&type='" + (isVideo ? "video'" : "image'") + "&user_id='" + user_id + "'");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.addRequestProperty("Accept", "*/*");
            con.addRequestProperty("Host", url.getHost());
            con.setDoOutput(true);

            // global encrypted

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while (null != (inputLine = in.readLine())) {
                response.append(inputLine);
            }

            in.close();
            return response.toString();

        } catch (Exception e) {
            return null;
        }
    }

}