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
        // friends like ",12412,1245235,555"

        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("friends", user_data.get("friends"));

        return API.jwtService.generateUserJwt(claims, String.valueOf(user_data.get(API.DB_SIGN_KEY)),
                String.valueOf(user_data.get(API.DB_ENCRYP_KEY)));
    }

    @PatchMapping
    public String handleFriendRequest(HttpServletRequest request) {
        // only session
        // current friends like ",12,2141,45235"

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
        if (data == null || !data.containsKey("modif") || !data.containsKey("friend_name")) {
            return null;
        }

        long user_id1 = API.databaseHandler.getUserIDByName(String.valueOf(data.get("friend_name")));
        if (user_id1 == 0) {
            return null;
        }

        String modification = String.valueOf(data.get("modif"));

        switch (modification) {
            case "accept" -> {
                // the request was accepted
                if (!data.containsKey("friends")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.deleteFriendRequest(user_id, user_id1) &&
                        API.databaseHandler.addUserFriend(user_id, user_id1, String.valueOf(data.get("friends"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

            }
            case "deny" -> {
                // the request was denied
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.deleteFriendRequest(user_id, user_id1));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

            }
            case "new" -> {
                // send friend request
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.createFriendRequest(user_id, user_id1));

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

        // current friends like ",1,1231241,35256"

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
        if (data == null || !data.containsKey("friends")) {
            return null;
        }

        long user_id1;
        try {
            user_id1 = Long.parseLong((String) data.get("friend_id"));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("stats", API.databaseHandler.removeUserFriend(user_id1, user_id, String.valueOf(data.get("friends"))));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }
}
