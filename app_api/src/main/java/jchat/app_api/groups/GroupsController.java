package jchat.app_api.groups;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group")
public class GroupsController {

    @GetMapping
    public String getGroups(HttpServletRequest request) {
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

        int amount;
        try {
            amount = Integer.parseInt(String.valueOf(data.get("amount")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", API.databaseHandler.getAllGroupsWithUser(user_id, amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }


    @PostMapping
    public String createGroup(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("name") || !data.containsKey("banner") || !data.containsKey("logo")
                || !data.containsKey("anim")) {
            return null;
        }

        String name = String.valueOf(data.get("name"));
        String logo = String.valueOf(data.get("logo"));
        String banner = String.valueOf(data.get("banner"));
        String animations = String.valueOf(data.get("anim"));

        if (API.databaseHandler.createGroup(user_id, name, logo, banner, animations)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("group_id", API.databaseHandler.getLatestCreatedGroupByOwner(user_id));

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }


    @PatchMapping
    public String updateGroup(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("modif") || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        String log_message = String.valueOf(data.get("log_message"));

        switch (String.valueOf(data.get("modif"))) {
            case "name" -> {
                // update the name
                if (!data.containsKey("name")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupName(user_id, group_id,
                        String.valueOf(data.get("name")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "logo" -> {
                // update the logo
                if (!data.containsKey("logo")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupLogo(String.valueOf(data.get("logo")), group_id,
                        user_id, log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "banner" -> {
                // update the banner
                if (!data.containsKey("banner")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupBanner(String.valueOf(data.get("banner")), group_id,
                        user_id, log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "owner" -> {
                // update the owner
                if (!data.containsKey("owner")) {
                    return null;
                }
                long new_owner_id;
                try {
                    new_owner_id = Long.parseLong(String.valueOf(data.get("owner")));
                } catch (Exception e) {
                    return null;
                }

                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupOwner(new_owner_id, group_id, user_id, log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "settings" -> {
                // update the settings
                if (!data.containsKey("settings")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupSettings(user_id, group_id,
                        String.valueOf(data.get("settings")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "events" -> {
                // update the settings
                if (!data.containsKey("events")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupEvents(String.valueOf(data.get("events")), group_id,
                        user_id, log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }


    @DeleteMapping
    public String deleteGroup(HttpServletRequest request) {
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

        if (API.databaseHandler.deleteGroup(group_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }

}
