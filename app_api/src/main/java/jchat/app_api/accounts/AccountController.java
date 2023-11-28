package jchat.app_api.accounts;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/account")
public class AccountController {


    @PostMapping()
    public String createAccount(HttpServletRequest request) {
        // u -> user name
        // p -> password
        // e -> email
        // i -> id


        if (!API.checkIfSolvedCaptcha(request)) {
            return null;
        }

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (null == data) { return null; }

        if (data.containsKey("u") && data.containsKey("p") && data.containsKey("e")) {
            String user = null;
            String password = null;
            String email = null;

            try {
                user = (String) data.get("u");
                password = (String) data.get("p");
                email = (String) data.get("e");
            } catch (Exception e) { return null; }

            if (null == user || null == password || null == email) { return null; }

            Map<String, Object> map = new HashMap<>();

            Long user_id = API.databaseHandler.createUser(user, email, password,
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserEncryptionKey()),
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserSignKey()));

            map.put("i", user_id == null ? 0l : user_id);

            return API.jwtService.generateGlobalJwt(map, true);

        }

        return null;

    }


    @PatchMapping
    public String updateAccount(HttpServletRequest request) {
        // only session
        // m -> what will be modified
        // s -> server stats
        // n -> update name
        // e -> update email
        // p -> update password
        // k -> update encryption key
        // c -> update sign key
        // o -> update start sub
        // q -> update end sub
        // b -> update bookmarks

        if (!API.checkIfSolvedCaptcha(request)) {
            return null;
        }

        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        String user_encryp_key = String.valueOf(user_data.get(API.DB_ENCRYP_KEY));
        String user_sign_key = String.valueOf(user_data.get(API.DB_SIGN_KEY));

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH),
                user_encryp_key, user_sign_key);
        if (null == data || !data.containsKey("m")) { return null; }

        switch (String.valueOf(data.get("m"))) {
            case "n" -> {
                // update name
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserName(user_id, String.valueOf(data.get("n"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "e" -> {
                // update email
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserEmail(user_id, String.valueOf(data.get("e"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "p" -> {
                // update password
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserPassword(user_id, String.valueOf(data.get("p"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "k" -> {
                // update encryption key
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserEncryptionKey(user_id));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "c" -> {
                // update sign key
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserSignKey(user_id));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "o" -> {
                // update start sub
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserStartsSub(user_id,
                        LocalDateTime.parse(String.valueOf(data.get("o")))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "q" -> {
                // update end sub
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserEndsSub(user_id,
                        LocalDateTime.parse(String.valueOf(data.get("q")))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "b" -> {
                // update bookmarks
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateUserBookmarks(user_id, String.valueOf(data.get("b"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }

    @GetMapping()
    public String getAccount(HttpServletRequest request) {
        // u -> user name
        // p -> password
        // e -> email
        // i -> ID
        // k -> encryption key
        // s -> jwt sign key
        // a -> session id

        if (!API.checkIfSolvedCaptcha(request)) {
            return null;
        }

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (null == data) { return null; }

        long id;
        if (data.containsKey("p") && data.containsKey("e")) {
            String email = null;
            String password = null;

            try {
                email = (String) data.get("e");
                password = (String) data.get("p");
            } catch (Exception e) { return null; }

            if (email == null || password == null) {
                return null;
            }

            // get user id by username, password and email
            Long user_id = API.databaseHandler.getUserByDetails(email, password);
            id = user_id == null ? 0l : user_id;

        } else if (data.containsKey("i")) {
            // remember me
            try {
                id = Long.parseLong(data.get("i").toString());
            } catch (Exception e) { return null; }

        } else { return null; }

        if (!API.databaseHandler.checkIfUserExists(id)) {
            return null;
        }

        Long sess_id = API.databaseHandler.startUserSessionID(id, API.get_IP(request));
        if (sess_id != null) {
            Map<String, Object> user_data = API.databaseHandler.getUserByID(id);
            Map<String, Object> respose_data = new HashMap<>();

            // encryption key
            respose_data.put("k", user_data.get(API.DB_ENCRYP_KEY));
            // sign key
            respose_data.put("s", user_data.get(API.DB_SIGN_KEY));
            // app session id
            respose_data.put("a", sess_id);

            return API.jwtService.generateGlobalJwt(respose_data, true);

        }

        return null;
    }
}
