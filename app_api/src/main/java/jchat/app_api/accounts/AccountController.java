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
        if (!API.checkIfSolvedCaptcha(request)) {
            return null;
        }

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (null == data) { return null; }

        if (data.containsKey("username") && data.containsKey("password") && data.containsKey("email") && data.containsKey("settings")) {
            Map<String, Object> map = new HashMap<>();
            Long user_id = API.databaseHandler.createUser(String.valueOf(data.get("username")),
                    String.valueOf(data.get("email")), String.valueOf(data.get("password")),
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserEncryptionKey()),
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserSignKey()),
                    String.valueOf(data.get("settings")));

            map.put("id", null == user_id ? 0l : user_id);

            return API.jwtService.generateGlobalJwt(map, true);

        }

        return null;

    }


    @PatchMapping
    public String updateAccount(HttpServletRequest request) {
        // only session

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
        if (null == data || !data.containsKey("modif")) { return null; }

        switch (String.valueOf(data.get("modif"))) {
            case "settings" -> {
                if (!data.containsKey("settings")) {
                    return null;
                }
                // update name
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserSettings(user_id, String.valueOf(data.get("settings"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "name" -> {
                if (!data.containsKey("name")) {
                    return null;
                }
                // update name
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserName(user_id, String.valueOf(data.get("name"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "email" -> {
                // update email
                if (!data.containsKey("email")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserEmail(user_id, String.valueOf(data.get("email"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "password" -> {
                // update password
                if (!data.containsKey("password")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserPassword(user_id, String.valueOf(data.get("password"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "encry_key" -> {
                // update encryption key
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserEncryptionKey(user_id));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "sig_key" -> {
                // update sign key
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserSignKey(user_id));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "start_sub" -> {
                // update start sub
                if (!data.containsKey("start_sub")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserStartsSub(user_id,
                        LocalDateTime.parse(String.valueOf(data.get("start_sub")))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "end_sub" -> {
                // update end sub
                if (!data.containsKey("end_sub")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateUserEndsSub(user_id,
                        LocalDateTime.parse(String.valueOf(data.get("end_sub")))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "bookmarks" -> {
                // update bookmarks
                if (!data.containsKey("bookmarks")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stast", API.databaseHandler.updateUserBookmarks(user_id, String.valueOf(data.get("bookmarks"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }


    @GetMapping()
    public String getAccount(HttpServletRequest request) {
        if (!API.checkIfSolvedCaptcha(request)) {
            return null;
        }

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (null == data) { return null; }

        long id;
        if (data.containsKey("password") && data.containsKey("email")) {
            // get user id by username, password and email
            Long user_id = API.databaseHandler.getUserByDetails(String.valueOf(data.get("email")),
                    String.valueOf(data.get("password")));
            id = null == user_id ? 0l : user_id;

        } else if (data.containsKey("id")) {
            // remember me
            try {
                id = Long.parseLong(data.get("id").toString());
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
            respose_data.put("encry_key", user_data.get(API.DB_ENCRYP_KEY));
            // sign key
            respose_data.put("sig_key", user_data.get(API.DB_SIGN_KEY));
            // app session id
            respose_data.put("sess_id", sess_id);
            // user id
            respose_data.put("id", id);

            return API.jwtService.generateGlobalJwt(respose_data, true);

        }

        return null;
    }
}
