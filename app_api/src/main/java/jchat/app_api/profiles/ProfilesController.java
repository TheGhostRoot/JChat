package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/profile")
public class ProfilesController {



    @GetMapping
    public String getProfile(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("id")) {
            return null;
        }

        long given_user_id;
        try {
            given_user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> profile_data = API.databaseHandler.getProfile(given_user_id);
        if (profile_data == null) {
            return null;
        }

        return API.jwtService.generateUserJwt(profile_data, user_sign_key, user_encryp_key);
    }


    @PostMapping
    public String uploadProfileAttachments(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("pfp")) {
            return null;
        }

        String selectedUploadServer = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user_id);
        claims.put("pfp", data.get("pfp"));

        String authHeader = API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        if (authHeader == null) {
            return null;
        }

        if (API.databaseHandler.updateProfilePfp(user_id, selectedUploadServer) &&
                API.uploadAttachments(selectedUploadServer, authHeader)) {
            // try to upload it.

        }

        return null;
    }

    @DeleteMapping
    public String deleteProfileAttachments(HttpServletRequest request) {

    }


    @PatchMapping
    public String updateProfile(HttpServletRequest request) {
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

        Map<String, Object> claims = new HashMap<>();
        if (data.containsKey("pfp")) {
            claims.put("stats", API.databaseHandler.updateProfilePfp(user_id, String.valueOf(data.get("pfp"))));
        }

        if (data.containsKey("banner")) {
            claims.put("stats", API.databaseHandler.updateProfileBanner(user_id, String.valueOf(data.get("banner"))));
        }

        if (data.containsKey("badges")) {
            claims.put("stats", API.databaseHandler.updateProfileBadges(user_id, String.valueOf(data.get("badges"))));
        }

        if (data.containsKey("name")) {
            claims.put("stats", API.databaseHandler.updateUserName(user_id, String.valueOf(data.get("name"))));
        }

        if (data.containsKey("animations")) {
            claims.put("stats", API.databaseHandler.updateProfileAnimations(user_id, String.valueOf(data.get("animations"))));
        }

        if (data.containsKey("about_me")) {
            claims.put("stats", API.databaseHandler.updateProfileAboutMe(user_id, String.valueOf(data.get("about_me"))));
        }

        if (data.containsKey("stats")) {
            claims.put("stats", API.databaseHandler.updateProfileStats(user_id, String.valueOf(data.get("stats"))));
        }

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

}
