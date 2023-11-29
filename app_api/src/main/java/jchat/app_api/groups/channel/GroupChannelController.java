package jchat.app_api.groups.channel;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group/channel")
public class GroupChannelController {


    @GetMapping
    public String getChannels(HttpServletRequest request) {
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

        long group_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("channels", API.databaseHandler.getGroupChannels(group_id));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }



    @PostMapping
    public String createChannel(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("name") || !data.containsKey("type") ||
                !data.containsKey("permissions") || !data.containsKey("categories") || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        Long channel_id = API.databaseHandler.createGroupChannel(group_id, user_id, String.valueOf(data.get("type")),
                String.valueOf(data.get("name")), String.valueOf(data.get("permissions")),
                String.valueOf(data.get("log_message")), String.valueOf(data.get("categories")));

        if (channel_id == null) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("channel_id", channel_id);

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }



    @PatchMapping
    public String updateChannels(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("log_message") || !data.containsKey("modif")) {
            return null;
        }

        long group_id;
        long channel_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
        } catch (Exception e) {
            return null;
        }

        String log_message = String.valueOf(data.get("log_message"));

        switch (String.valueOf(data.get("modif"))) {
            case "name" -> {
                // update channel name
                if (!data.containsKey("name")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupChannelName(channel_id, group_id, user_id,
                        String.valueOf(data.get("name")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "permissions" -> {
                // update channel permissions
                if (!data.containsKey("permissions")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupChannelPermissions(channel_id, group_id, user_id,
                        String.valueOf(data.get("permissions")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "categories" -> {
                // update channel name
                if (!data.containsKey("categories")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupChannelCategory(channel_id, group_id, user_id,
                        String.valueOf(data.get("categories")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "type" -> {
                // update channel name
                if (!data.containsKey("type")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupChannelType(channel_id, group_id, user_id,
                        String.valueOf(data.get("type")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }




    @DeleteMapping
    public String deleteChannel(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        long channel_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            channel_id = Long.parseLong(String.valueOf(data.get("channel_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteGroupChannel(channel_id, group_id, user_id, String.valueOf(data.get("log_message")))) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }


}
