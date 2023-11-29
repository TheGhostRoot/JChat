package jchat.app_api.posts.comments;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/posts/comment")
public class PostCommentController {


    @GetMapping
    public String getCommentsOnPost(HttpServletRequest request) {
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
        long post_id;
        try {
            amount = Integer.parseInt(String.valueOf(data.get("amount")));
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("comments", API.databaseHandler.getCommentsOnPost(post_id, amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @PostMapping
    public String createCommentOnPost(HttpServletRequest request) {
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

        long post_id;
        long message_id;
        try {
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
        } catch (Exception e) {
            return null;
        }

        if (!data.containsKey("message") || !data.containsKey("repl")) {
            return null;
        }

        String message = String.valueOf(data.get("message"));
        String repl = String.valueOf(data.get("repl"));
        if (API.databaseHandler.addCommentToPost(user_id, post_id, message, repl)) {
            Long msg_id = API.databaseHandler.getCommentID(post_id, user_id, message, repl);
            if (msg_id == null) {
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("message_id", msg_id);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }
        return null;

    }


    @PatchMapping
    public String updateCommentOnPost(HttpServletRequest request) {
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

        long post_id;
        long message_id;
        try {
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
        } catch (Exception e) {
            return null;
        }

        if (!data.containsKey("message")) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("stats", API.databaseHandler.updateCommentMessage(post_id, user_id, message_id,
                String.valueOf(data.get("message"))));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @DeleteMapping
    public String deleteCommentOnPost(HttpServletRequest request) {
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

        long post_id;
        long message_id;
        try {
            post_id = Long.parseLong(String.valueOf(data.get("post_id")));
            message_id = Long.parseLong(String.valueOf(data.get("message_id")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.deleteCommentFromPost(post_id, message_id, user_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("stats", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);

        }

        return null;
    }
}
