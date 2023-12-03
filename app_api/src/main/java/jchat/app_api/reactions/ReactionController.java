package jchat.app_api.reactions;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/reaction")
public class ReactionController {


    @GetMapping
    public String getReactions(HttpServletRequest request) {
        // only session
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
        if (data == null) {
            return null;
        }

        long channel_id;
        long message_id;
        long post_id;
        int amount;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            message_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
            amount = Integer.parseInt(String.valueOf(data.get("amount")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> react = API.databaseHandler.getReactions(channel_id, message_id, post_id, amount);
        if (react == null) {
            return null;
        }

        return API.jwtService.generateUserJwt(react, user_sign_key, user_encryp_key);
    }



    @DeleteMapping
    public String removeReaction(HttpServletRequest request) {
        // only session
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
        if (data == null || !data.containsKey("reaction")) {
            return null;
        }

        long channel_id;
        long message_id;
        long post_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            message_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.removeReaction(channel_id, message_id, post_id,
                String.valueOf(data.get("reaction")), user_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }



    @PostMapping
    public String addReaction(HttpServletRequest request) {
        // only session
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
        if (data == null || !data.containsKey("reaction")) {
            return null;
        }

        long channel_id;
        long message_id;
        long post_id;
        long group_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            message_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.addReaction(channel_id, message_id, post_id, String.valueOf(data.get("reaction")),
                user_id, group_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }

}
