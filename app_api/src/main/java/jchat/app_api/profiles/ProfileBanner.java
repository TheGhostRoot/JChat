package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/profile/banner")
public class ProfileBanner {


    @GetMapping
    public String getProfileBanner(HttpServletRequest request,
                                   @RequestParam("redirected") boolean redirected, @RequestParam("type") String type) {
        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (data == null || !data.containsKey("id")) {
            return null;
        }

        long given_user_id;
        try {
            given_user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

        if (redirected && (type == null || type.isBlank())) {
            if (type.equals("video") && new File("/attachments/"+given_user_id+"/banner.mp4").exists()) {
                return """
                <html><head></head> 
                <body> 
                
                <video controls autoplay muted name="media">
                    <source type="video/mp4" src="/attachments/"""+ given_user_id + """
                 /banner.mp4"> 
                 
                 </video>
                 
                 </body></html>""";

            } else if (new File("/attachments/"+given_user_id+"/banner.jpg").exists()) {
                return """
                        <html><head></head> 
                        <body> 
                                        
                                <img src="/attachments/"""+ given_user_id + """
                         /black.jpg">  
                         
                         </body></html>""";


            }
            return """
        <html><head></head><body> 
        
        <img src="/black.jpg">
        
        </body></html>""";

        } else {
            Map<String, Object> profile = API.databaseHandler.getProfile(given_user_id);
            if (profile == null) {
                return null;
            }

            String server = String.valueOf(profile.get("banner"));
            if (server.isBlank()) {
                server = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));
            }
            server += "/banner";

            boolean isVideo = server.startsWith("video;");

            String res = API.sendRequestToUploads((isVideo ? server.substring(6) : server),
                    request.getHeader(API.REQ_HEADER_AUTH), "GET", isVideo);
            return res == null ? """
        <html><head><head><body> 
        
        <img src="/black.jpg">  
        
        </body></html>""" : res;
        }

    }


    // @RequestParam("file") MultipartFile file
    // @RequestBody JChatRequestBody bodyRequest

    @PostMapping()
    public String uploadProfileBanner(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                      @RequestParam("video") boolean isVideo, @RequestParam("id") long given_user_id) {
        // upload the banner in file system
        // Map<String, Object> body = bodyRequest.getData();
        byte[] files;
        try {
            if (file.isEmpty()) {
                return null;
            }
            files = file.getBytes();
        } catch (Exception e) {
            return null;
        }


        return API.fileSystemHandler.saveFile(given_user_id, isVideo, files, "banner") ? "true" : "false";
    }

}
