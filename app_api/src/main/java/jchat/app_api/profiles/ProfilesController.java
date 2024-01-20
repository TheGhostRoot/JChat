package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+ "/profile")
public class ProfilesController {



    @GetMapping
    public String getProfile(HttpServletRequest request) {
        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (data == null || !data.containsKey("id")) {
            return null;
        }

        long user_id;
        try {
            user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> profile_data = API.databaseHandler.getProfile(user_id);
        if (profile_data == null) {
            return null;
        }

        return API.jwtService.generateGlobalJwt(profile_data, true);
    }


    // @RequestParam("file") MultipartFile file
    // @RequestBody JChatRequestBody bodyRequest

    @PatchMapping()
    public String updateUser(HttpServletRequest request) {
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
        if (data.containsKey("name")) {
            claims.put("stats", API.databaseHandler.updateUserName(user_id, String.valueOf(data.get("name"))));
        }

        if (data.containsKey("animations")) {
            boolean suess = API.databaseHandler.updateProfileAnimations(user_id, String.valueOf(data.get("animations")));
            if (claims.containsKey("stats")) {
                claims.put("stats", suess && Boolean.valueOf(claims.get("stats").toString()));

            } else {
                claims.put("stats", suess);
            }
        }

        if (data.containsKey("about_me")) {
            boolean suess = API.databaseHandler.updateProfileAboutMe(user_id, String.valueOf(data.get("about_me")));
            if (claims.containsKey("stats")) {
                claims.put("stats", suess && Boolean.valueOf(claims.get("stats").toString()));

            } else {
                claims.put("stats", suess);
            }
        }

        if (data.containsKey("stats")) {
            boolean suess = API.databaseHandler.updateProfileStats(user_id, String.valueOf(data.get("stats")));
            if (claims.containsKey("stats")) {
                claims.put("stats", suess && Boolean.valueOf(claims.get("stats").toString()));

            } else {
                claims.put("stats", suess);
            }
        }

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }

    @PostMapping()
    public String updateProfile(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                @RequestParam("video") boolean isVideo, @RequestParam("pfp") boolean isPfp,
                                @RequestParam("id") String user_id_str) {
        // only session
        //Map<String, Object> given_body = bodyRequest.getData();

        String selectedUploadServer = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));

        byte[] files;
        long user_id;
        try {
            if (file.isEmpty()) {
               return null;
            }
            user_id = Long.parseLong(user_id_str);
            files = file.getBytes();
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();

        if (isPfp) {
                /*
                Map<String, Object> body = new HashMap<>();
                body.put("id", user_id);
                body.put("pfp", given_body.get("pfp")); */

                boolean a = API.databaseHandler.updateProfilePfp(user_id,
                        (isVideo ? "video;" : "") + selectedUploadServer);

                boolean g = API.uploadFile(selectedUploadServer + "/avatar", isVideo, user_id, files);

                claims.put("stats", a && g);

        } else {
                /*
                Map<String, Object> body = new HashMap<>();
                body.put("id", user_id);
                body.put("banner", given_body.get("banner")); */

                boolean f = API.databaseHandler.updateProfileBanner(user_id,
                        (isVideo ? "video;" : "") + selectedUploadServer);
                boolean d = API.uploadFile(selectedUploadServer + "/banner", isVideo, user_id, files);
                claims.put("stats", d && f);
        }

        return API.jwtService.generateGlobalJwt(claims, true);
    }

}
