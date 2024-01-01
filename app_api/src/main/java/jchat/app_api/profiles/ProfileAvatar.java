package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import jchat.app_api.JChatRequestBody;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/profile/avatar")
public class ProfileAvatar {


    @GetMapping
    public String getProfileAvatar(HttpServletRequest request) {
        String auth = request.getHeader(API.REQ_HEADER_AUTH);
        if (auth == null) {
            return null;
        }
        Map<String, Object> data = API.jwtService.getData(auth, null, null);
        if (data == null || !data.containsKey("id")) {
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

        if (String.valueOf(profile.get("pfp")).startsWith("video;")) {
            return "<html><head><head><body> <source type=\"video/mp4\" src=\"attachments/" + given_user_id + "/avatar.mp4\">  <body><html>";

        } else {
            return "<html><head><head><body> <img src=\"attachments/"+ given_user_id +"/avatar.jpg\">  <body><html>";
        }
    }


    @PostMapping()
    public String uploadProfileAvatar(HttpServletRequest request, @RequestBody JChatRequestBody bodyRequest) {
        Map<String, Object> body = bodyRequest.getData();
        String auth = request.getHeader(API.REQ_HEADER_AUTH);
        if (auth == null) {
            return "false";
        }
        Map<String, Object> data = API.jwtService.getData(auth, null, null);
        if (data == null || body == null) {
            return "false";
        }

        long given_user_id;
        try {
            given_user_id = Long.parseLong(String.valueOf(body.get("id")));
        } catch (Exception e) {
            return "false";
        }

        String pfp = String.valueOf(body.get("pfp"));
        return API.fileSystemHandler.saveFile(given_user_id, pfp.startsWith("video;"), pfp.startsWith("video;") ? pfp.substring(6, pfp.length()) : pfp, "avatar") ? "true" : "false";
    }
}
