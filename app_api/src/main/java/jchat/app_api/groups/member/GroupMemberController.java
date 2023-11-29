package jchat.app_api.groups.member;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group/member")
public class GroupMemberController {


    @GetMapping
    public String getMembers(HttpServletRequest request) {
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
        claims.put("members", API.databaseHandler.getGroupMembers(group_id));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }



    @PostMapping
    public String addMemberToGroup(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("roles") || !data.containsKey("nickname") ||
                !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.addGroupMember(user_id, group_id, String.valueOf(data.get("roles")),
                String.valueOf(data.get("nickname")), String.valueOf(data.get("log_message")))) {

            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key ,user_encryp_key);
        }

        return null;
    }


    @PatchMapping
    public String updateMemberInGroup(HttpServletRequest request) {
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
        long member_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
            member_id = Integer.parseInt(String.valueOf(data.get("member_id")));
        } catch (Exception e) {
            return null;
        }

        String log_message = String.valueOf(data.get("log_message"));

        switch (String.valueOf(data.get("modif"))) {
            case "nickname" -> {
                // update member nickname
                if (!data.containsKey("nickname")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateMemberNickname(member_id, group_id, user_id,
                        String.valueOf(data.get("nickname")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "role" -> {
                // update member roles
                if (!data.containsKey("role")) {
                    return null;
                }
                long role_id;
                try {
                    role_id = Long.parseLong(String.valueOf(data.get("role")));
                } catch (Exception e) {
                    return null;
                }
                if (!data.containsKey("to_add")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                try {
                    claims.put("stats", API.databaseHandler.updateMemberRoles(member_id, group_id, role_id, user_id,
                            Boolean.getBoolean(String.valueOf(data.get("to_add"))), log_message));

                    return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
                } catch (Exception e) {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }


    @DeleteMapping
    public String removeMemberFromGroup(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("type") || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        long member_id;
        try {
            group_id = Integer.parseInt(String.valueOf(data.get("group_id")));
            member_id = Integer.parseInt(String.valueOf(data.get("member_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.removeGroupMember(member_id, group_id, String.valueOf(data.get("type")),
                String.valueOf(data.get("log_message")), user_id)) {

            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key ,user_encryp_key);
        }

        return null;
    }
}
