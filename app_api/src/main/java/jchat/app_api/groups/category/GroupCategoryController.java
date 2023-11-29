package jchat.app_api.groups.category;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/group/category")
public class GroupCategoryController {


    @GetMapping
    public String getCategories(HttpServletRequest request) {
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
        claims.put("categories", API.databaseHandler.getGroupCategories(group_id));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }




    @PostMapping
    public String createCategory(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("name") || !data.containsKey("type") || !data.containsKey("log_message")) {
            return null;
        }

        long group_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.createGroupCategory(user_id, group_id, String.valueOf(data.get("name")),
                String.valueOf(data.get("type")), String.valueOf(data.get("log_message")))) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }




    @PatchMapping
    public String updateGategory(HttpServletRequest request) {
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
        long category_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            category_id = Long.parseLong(String.valueOf(data.get("category_id")));
        } catch (Exception e) {
            return null;
        }

        String log_message = String.valueOf(data.get("log_message"));

        switch (String.valueOf(data.get("modif"))) {
            case "name" -> {
                // update name
                if (!data.containsKey("name")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupCategoryName(category_id, group_id, user_id,
                        String.valueOf(data.get("name")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "type" -> {
                // update type
                if (!data.containsKey("type")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateGroupCategoryType(category_id, group_id, user_id,
                        String.valueOf(data.get("type")), log_message));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }




    @DeleteMapping
    public String deleteGategory(HttpServletRequest request) {
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
        long category_id;
        try {
            group_id = Long.parseLong(String.valueOf(data.get("group_id")));
            category_id = Long.parseLong(String.valueOf(data.get("category_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteGroupCategory(user_id, group_id, category_id,
                String.valueOf(data.get("log_message")))) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }



}
