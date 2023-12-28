package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/profile/banner")
public class ProfileBanner {


    @GetMapping
    public String getProfileBanner(HttpServletRequest request) {
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
        if (data == null || !data.containsKey("id") || !data.containsKey("name")) {
            return null;
        }

        long given_user_id;
        try {
            given_user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> profile = API.databaseHandler.getProfile(given_user_id);
        if (profile == null) {
            return null;
        }

        if (String.valueOf(profile.get("banner")).startsWith("video;")) {
            return "<html><head><head><body> <source type=\"video/mp4\" src=\"attachments/"+ given_user_id + "/" + data.get("name") +".mp4\">  <body><html>";

        } else {
            return "<html><head><head><body> <img src=\"attachments/"+given_user_id + "/" + data.get("name")+".jpeg\">  <body><html>";
        }
    }


    @PatchMapping
    public String uploadProfileBanner(HttpServletRequest request) {
        // upload the banner in file system
        String auth = request.getHeader(API.REQ_HEADER_AUTH);
        if (auth == null) {
            return "false";
        }
        Map<String, Object> data = API.jwtService.getData(auth, null, null);
        if (data == null || !data.containsKey("id") || !data.containsKey("banner") || !data.containsKey("name")) {
            return "false";
        }

        long given_user_id;
        try {
            given_user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return "false";
        }

        String banner = String.valueOf(data.get("banner"));
        String name = String.valueOf(data.get("name"));
        if (!name.equals("")) {
            API.fileSystemHandler.deleteFile(given_user_id, name);
        }
        return API.fileSystemHandler.saveFile(given_user_id, banner.startsWith("video;"), banner.substring(6, banner.length())) ? "true" : "false";
    }

}
