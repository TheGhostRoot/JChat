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
        if (data == null || !data.containsKey("name")) {
            return null;
        }

        String selectedUploadServer = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user_id);
        claims.put("name", data.get("name"));

        if (data.containsKey("pfp")) {
            claims.put("pfp", data.get("pfp"));
            String authHeader = API.jwtService.generateGlobalJwt(claims, true);
            if (authHeader == null) {
                return null;
            }

            if (API.databaseHandler.updateProfilePfp(user_id, selectedUploadServer) &&
                    API.uploadAttachments(selectedUploadServer, authHeader, "PATCH")) {
                // try to upload it.

            }

        }

        if (data.containsKey("banner")) {
            claims.put("banner", data.get("banner"));
            String authHeader = API.jwtService.generateGlobalJwt(claims, true);
            if (authHeader == null) {
                return null;
            }

            if (API.databaseHandler.updateProfileBanner(user_id, selectedUploadServer) &&
                    API.uploadAttachments(selectedUploadServer, authHeader, "PATCH")) {
                // try to upload it.

            }
        }

        return null;
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

        String selectedUploadServer = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));

        if (data.containsKey("pfp") && data.containsKey("pfp_name")) {
            Map<String, Object> pfpClaims = new HashMap<>();
            pfpClaims.put("id", user_id);
            pfpClaims.put("name", data.get("pfp_name"));
            pfpClaims.put("pfp", data.get("pfp"));

            String authHeader = API.jwtService.generateGlobalJwt(pfpClaims, true);
            if (authHeader == null) {
                claims.put("stats", false);

            } else {
                claims.put("stats",
                        API.databaseHandler.updateProfilePfp(user_id,
                                (String.valueOf(data.get("pfp")).startsWith("video;") ? "video;" : "") + selectedUploadServer) &&
                        API.uploadAttachments(selectedUploadServer, authHeader, "PATCH"));
            }

        }

        if (data.containsKey("banner") && data.containsKey("banner_name")) {
            Map<String, Object> bannerClaims = new HashMap<>();
            bannerClaims.put("id", user_id);
            bannerClaims.put("name", data.get("banner_name"));
            bannerClaims.put("banner", data.get("banner"));

            String authHeader = API.jwtService.generateGlobalJwt(bannerClaims, true);
            if (authHeader == null) {
                claims.put("stats", false);

            } else {
                claims.put("stats",
                        API.databaseHandler.updateProfileBanner(user_id,
                                (String.valueOf(data.get("banner")).startsWith("video;") ? "video;" : "") + selectedUploadServer) &&
                        API.uploadAttachments(selectedUploadServer, authHeader, "PATCH"));
            }
        }

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

}
