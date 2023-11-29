package jchat.app_api.friends.chats;

import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/friend/chat")
public class ChatsController {

    @GetMapping
    public String getMessagesFromDM(HttpServletRequest request) {
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
        int amount;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            amount = Integer.parseInt(String.valueOf(data.get("amount")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("messages", API.databaseHandler.getMessages(channel_id, 0l, amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @PostMapping
    public String sendDM(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("message")) {
            return null;
        }

        long channel_id;
        long other_member;
        String message = String.valueOf(data.get("message"));
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            other_member = Long.parseLong(String.valueOf(data.get("resv_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.addMessage(channel_id, user_id, other_member, message, 0L)) {
            long message_id;
            try {
                if (API.databaseManager.isSQL()) {
                    message_id = Long.parseLong(String.valueOf(API.databaseHandler.getMessages(channel_id, 0L, 1)
                            .get("msg_id")));

                } else if (API.databaseManager.isMongo()) {
                    message_id = (long) ((List<Map<String, Object>>) API.databaseHandler
                            .getMessages(channel_id, 0l, 0).get("msgs").get(0)).get(0).get("msg_id");

                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);
            claims.put("message_id", message_id);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }

    @PatchMapping
    public String editMessageFromDM(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("message")) {
            return null;
        }

        long channel_id;
        long message_id;
        String message = String.valueOf(data.get("message"));
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.editMessage(message_id, channel_id, user_id, message, 0L)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }

    @DeleteMapping
    public String deleteMessageFromDM(HttpServletRequest request) {
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
        long actor_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
            actor_id = Long.parseLong(String.valueOf(data.get("actor_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteMessage(user_id, channel_id, message_id, actor_id, 0L)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }
}
