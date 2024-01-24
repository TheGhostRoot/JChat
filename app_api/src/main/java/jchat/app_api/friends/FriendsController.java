package jchat.app_api.friends;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import jchat.app_api.profiles.ProfilesController;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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



        if (request.getHeader("Friend_requests") != null) {
            // get friend requests
            Map<String, Object> claims = new HashMap<>();
            claims.put("friend_requests", API.databaseHandler.getFriendRequestsForUser(user_id));

            return API.jwtService.generateUserJwt(claims, String.valueOf(user_data.get(API.DB_SIGN_KEY)),
                    String.valueOf(user_data.get(API.DB_ENCRYP_KEY)));

        } else if (request.getHeader("Pending_requests") != null) {
            // get pending requests
            Map<String, Object> claims = new HashMap<>();
            claims.put("pending_requests", API.databaseHandler.getPendingRequestsForUser(user_id));

            return API.jwtService.generateUserJwt(claims, String.valueOf(user_data.get(API.DB_SIGN_KEY)),
                    String.valueOf(user_data.get(API.DB_ENCRYP_KEY)));
        }

        Map<String, Object> claims = new HashMap<>();
        List<Map<String, Object>> allFriends = new ArrayList<>();
        for (String friend_id_text : user_data.get("friends").toString().split(",")) {
            long friend_id;
            try {
                friend_id = Long.parseLong(friend_id_text);
            } catch (Exception e) {
                continue;
            }

            Map<String, Object> uploadAuthClaims = new HashMap<>();
            uploadAuthClaims.put("id", friend_id);

            String auth = API.jwtService.generateGlobalJwt(uploadAuthClaims, true);
            if (auth == null) {
                return null;
            }

            Map<String, Object> friend_profile = API.databaseHandler.getProfile(friend_id);
            if (friend_profile == null) {
                return null;
            }

            String pfpServer = friend_profile.get("pfp").toString();
            if (pfpServer.isBlank() || pfpServer.equals("null")) {
                pfpServer = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));
            }

            pfpServer += "/avatar";

            boolean isVideo = pfpServer.startsWith("video;");

            String pfpRes = API.sendRequestToUploads(isVideo ? pfpServer.substring(6) : pfpServer, auth,"GET", isVideo);
            if (pfpRes == null) {
                return null;
            }

            Map<String, Object> friend = new HashMap<>();
            friend.put("name", API.databaseHandler.getUserNameByID(friend_id));
            friend.put("pfpBase64", isVideo ? "video;" : pfpRes);
            friend.put("id", friend_id);
            friend.put("channel_id", 0);

            allFriends.add(friend);
        }

        claims.put("friends", allFriends);

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
        if (data == null || !data.containsKey("modif")) {
            return null;
        }

        long user_id1;

        if (data.containsKey("friend_id")) {
            try {
                user_id1 = Long.parseLong(String.valueOf(data.get("friend_id")));
            } catch (Exception e) {
                return null;
            }

        } else if (data.containsKey("friend_name")) {
            user_id1 = API.databaseHandler.getUserIDByName(String.valueOf(data.get("friend_name")));

        } else {
            return null;
        }

        if (user_id1 == 0) {
            return null;
        }

        String modification = String.valueOf(data.get("modif"));

        switch (modification) {
            case "accept" -> {
                // the request was accepted
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.deleteFriendRequest(user_id, user_id1) &&
                        API.databaseHandler.addUserFriend(user_id, user_id1) &&
                        API.databaseHandler.addUserFriend(user_id1, user_id));

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
        claims.put("stats", API.databaseHandler.removeUserFriend(user_id1, user_id));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }
}
