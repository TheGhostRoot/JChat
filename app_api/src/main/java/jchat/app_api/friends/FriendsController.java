package jchat.app_api.friends;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/friend")
public class FriendsController {

    @GetMapping()
    public String getFriends(HttpServletRequest request) {
        // only session

        // f -> list of all friends like ",12412,1245235,555"

        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("f", user_data.get("friends"));

        return API.jwtService.generateUserJwt(claims, (String) user_data.get(API.DB_SIGN_KEY),
                (String) user_data.get(API.DB_ENCRYP_KEY));
    }

    @PatchMapping
    public String handleFriendRequest(HttpServletRequest request) {
        // only session

        // m  -> modification in the friend request like accept, deny or create
        // c  -> current friends like ",12,2141,45235"  the list of IDs in that format
        // f  -> the friend ID
        // i  -> issuer AKA who send it
        // s  -> server stats

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

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), user_encryp_key,
                user_sign_key);
        if (data == null || !data.containsKey("m") || !data.containsKey("c")) {
            return null;
        }

        long user_id1;
        long user_id2;
        try {
            user_id1 = Long.parseLong((String) data.get("f"));
            user_id2 = Long.parseLong((String) data.get("i"));
        } catch (Exception e) {
            return null;
        }

        String modification = String.valueOf(data.get("m"));

        switch (modification) {
            case "a" -> {
                // the request was accepted
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.deleteFriendRequest(user_id1, user_id2) &&
                        API.databaseHandler.addUserFriend(user_id1, user_id2, String.valueOf(data.get("c"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

            }
            case "d" -> {
                // the request was denied
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.deleteFriendRequest(user_id1, user_id2));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

            }
            case "s" -> {
                // send friend request
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.createFriendRequest(user_id1, user_id2));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

            }
            default -> {
                return null;
            }
        }
    }

    @DeleteMapping
    public String removeFriend(HttpServletRequest request) {
        // only session

        // c -> list of IDs of current friends like ",1,1231241,35256"   'c' like current
        // f -> the friend id    'f' like friend
        // i -> who sends it?    'i' issuer
        // s -> server response with stats  's'  stats

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

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), user_encryp_key,
                user_sign_key);
        if (data == null || !data.containsKey("c")) {
            return null;
        }

        long user_id1;
        long user_id2;
        try {
            user_id1 = Long.parseLong((String) data.get("f"));
            user_id2 = Long.parseLong((String) data.get("i"));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("s", API.databaseHandler.removeUserFriend(user_id1, user_id2, String.valueOf(data.get("c"))));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }
}
