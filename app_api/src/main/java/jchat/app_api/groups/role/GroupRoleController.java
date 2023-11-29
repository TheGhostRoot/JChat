package jchat.app_api.groups.role;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group/role")
public class GroupRoleController {


    @GetMapping
    public String getRoles(HttpServletRequest request) {
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
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", API.databaseHandler.getGroupRoles(group_id));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }


    @PostMapping
    public String createRole(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("name") || !data.containsKey("permissions") || !data.containsKey("type")
                 || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        Long role_id = API.databaseHandler.createGroupRole(user_id, group_id, String.valueOf(data.get("name")),
                String.valueOf(data.get("permissions")), String.valueOf(data.get("type")),
                String.valueOf(data.get("log_message")));

        if (role_id == null) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role_id", role_id);

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }


    @PatchMapping
    public String updateRole(HttpServletRequest request) {
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
        long role_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
            role_id = Integer.parseInt(String.valueOf(data.get("role_id")));
        } catch (Exception e) {
            return null;
        }

        String log_message = String.valueOf(data.get("log_message"));

        switch (String.valueOf(data.get("modif"))) {
            case "name" -> {
                // update role name
                if (!data.containsKey("name")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupRoleName(role_id, group_id, user_id,
                        String.valueOf(data.get("name")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "permissions" -> {
                // update role permissions
                if (!data.containsKey("permissions")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupRolePermissions(role_id, group_id, user_id,
                        String.valueOf(data.get("permissions")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "type" -> {
                // update role type
                if (!data.containsKey("type")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupRoleType(role_id, group_id, user_id,
                        String.valueOf(data.get("type")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }


    @DeleteMapping
    public String deleteRole(HttpServletRequest request) {
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
        long role_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
            role_id = Integer.parseInt(String.valueOf(data.get("role_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteGroupRole(role_id, group_id, user_id, String.valueOf(data.get("log_message")))) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }
}
