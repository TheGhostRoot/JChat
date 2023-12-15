package jchat.app_api.groups.chat;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group/chat")
public class GroupChatController {

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
        long group_id;
        int amount;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            amount = Integer.parseInt(String.valueOf(data.get("amount")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("messages", API.databaseHandler.getGroupChannelMessages(channel_id, group_id, amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @PostMapping
    public String sendGroupMessage(HttpServletRequest request) {
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
        long group_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.addMessage(channel_id, user_id, 0l, String.valueOf(data.get("message")),
                group_id)) {

            long message_id;
            try {
                if (API.databaseManager.isSQL()) {
                    message_id = Long.parseLong(String.valueOf(API.databaseHandler.getMessages(channel_id, group_id, 1)
                            .get("msg_id")));

                } else if (API.databaseManager.isMongo()) {
                    message_id = (long) ((List<Map<String, Object>>) API.databaseHandler
                            .getMessages(channel_id, group_id, 1).get("msgs").get(0)).get(0).get("msg_id");

                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("message_id", message_id);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }


    @PatchMapping
    public String updateGroupMessage(HttpServletRequest request) {
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
        long group_id;
        long message_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.editMessage(message_id, channel_id, user_id,
                String.valueOf(data.get("message")), group_id)) {

            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }


    @DeleteMapping
    public String deleteGroupMessage(HttpServletRequest request) {
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
        long group_id;
        long message_id;
        long sender_id;
        try {
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
            sender_id = Long.parseLong(String.valueOf(data.get("sender_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteMessage(sender_id, channel_id, message_id, user_id, group_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }

}
