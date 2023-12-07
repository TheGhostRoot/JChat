package jchat.app_api.posts;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/posts")
public class PostsController {

    @GetMapping
    public String getLatestPosts(HttpServletRequest request) {
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
        claims.put("posts", API.databaseHandler.getLatestPosts(amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @PostMapping
    public String createPost(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("message") || !data.containsKey("background")) {
            return null;
        }

        if (API.databaseHandler.createPost(user_id, String.valueOf(data.get("message")),
                String.valueOf(data.get("background")))) {

            long post_id;
            try {
                post_id = Long.parseLong(String.valueOf(API.databaseHandler.getLatestPosts(1).get("id").get(0)));
            } catch (Exception e) {
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", post_id);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }


    @PatchMapping
    public String updatePost(HttpServletRequest request) {
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

        int post_id;
        try {
            post_id = Integer.parseInt(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        String message = null;
        String background = null;
        if (data.containsKey("message")) {
            message = String.valueOf(data.get("message"));
        }
        if (data.containsKey("background")) {
            background = String.valueOf(data.get("background"));
        }

        if (API.databaseHandler.editPost(user_id, post_id, message, background)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }

        return null;
    }


    @DeleteMapping
    public String deletePost(HttpServletRequest request) {
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

        int post_id;
        try {
            post_id = Integer.parseInt(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deletePost(user_id, post_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }
}
