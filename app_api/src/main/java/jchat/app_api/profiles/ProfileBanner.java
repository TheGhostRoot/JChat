package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+ "/profile/banner")
public class ProfileBanner {


    @GetMapping
    public Object getProfileBanner(HttpServletRequest request,
                                   @RequestParam("redirected") boolean redirected,
                                   @RequestParam("type") String type, @RequestParam("user_id") String uid) {

        /*
        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), null, null);
        if (data == null) {
            return null;
        }

        long user_id;
        try {
            user_id = Long.parseLong(String.valueOf(data.get("id")));
        } catch (Exception e) {
            return null;
        }

         */

        uid = uid.substring(1, uid.length() -1);
        type = type.substring(1, type.length() -1);
        long user_id;
        try {
            user_id = Long.parseLong(uid);
        } catch (Exception e) {
            return null;
        }

        if (redirected && !type.isBlank()) {
            File bannerVideo = new File("./attachments/" +user_id+"/banner.mp4");
            File bannerImg = new File("./attachments/" +user_id+"/banner.jpg");
            if (type.equals("video") && bannerVideo.exists()) {
                try {
                    return "video;" + (new String(Base64.encodeBase64(FileUtils.readFileToByteArray(bannerVideo)), StandardCharsets.US_ASCII));

                } catch (Exception e) {
                    return null;
                }

            } else if (bannerImg.exists()) {
                try {
                    return new String(Base64.encodeBase64(FileUtils.readFileToByteArray(bannerImg)), StandardCharsets.US_ASCII);

                } catch (Exception e) {
                    return null;
                }

            } else {
                try {
                    return new String(Base64.encodeBase64(FileUtils.readFileToByteArray(new File("profile/banner/black.jpg"))), StandardCharsets.US_ASCII);

                } catch (Exception e) {
                    return null;
                }
            }

            /*
            if (type.equals("video") && new File("/attachments/" +given_user_id+"/banner.mp4").exists()) {
                return """
                <html><head></head> 
                <body> 
                
                <video controls autoplay muted name="media">
                    <source type="video/mp4" src="/attachments/"""+ given_user_id + """
                 /banner.mp4"> 
                 
                 </video>
                 
                 </body></html>""";

            } else if (new File("/attachments/" +given_user_id+"/banner.jpg").exists()) {
                return """
                        <html><head></head> 
                        <body> 
                                        
                                <img src="/attachments/"""+ given_user_id + """
                         /black.jpg">  
                         
                         </body></html>""";


            }
            return """
        <html><head></head><body> 
        
        <img src="/profile/banner/black.jpg">
        
        </body></html>""";*/

        } else {
            Map<String, Object> profile = API.databaseHandler.getProfile(user_id);
            if (profile == null) {
                return null;
            }

            String server = String.valueOf(profile.get("banner"));
            if (server.isBlank() || server.equals("null")) {
                server = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));
            }
            server += "/banner";

            boolean isVideo = server.startsWith("video;");

            String base64Banner = API.sendRequestToUploads((isVideo ? server.substring(6) : server),"GET", isVideo, user_id);

            if (base64Banner == null) {
                return null;
            }


            if (base64Banner.startsWith("video;")) {

                return Base64.decodeBase64(base64Banner.substring(6));

                /*
                return """
                        <html><head></head><body>
                        
                        <video controls autoplay muted> 
                            <source type="video/mp4" src="data:video/mp4;base64, """ + base64Banner.substring(6) + """
                            ">
                        </video>
                         
                                            
                        </body>
                        </html>
                        """;

                 */

            } else {
                return base64Banner;
                //return "<html><head></head><body> <img src=\"data:image/jpg;base64, " + base64Banner + "\"/></body></html>";
            }
        }

    }


    // @RequestParam("file") MultipartFile file
    // @RequestBody JChatRequestBody bodyRequest

    @PostMapping()
    public String uploadProfileBanner(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                      @RequestParam("video") boolean isVideo, @RequestParam("id") String given_user_id_str) {
        // upload the banner in file system
        // Map<String, Object> body = bodyRequest.getData();

        byte[] files;
        long given_user_id;
        try {
            if (file.isEmpty()) {
                return null;
            }
            given_user_id = Long.parseLong(given_user_id_str);
            files = file.getBytes();
        } catch (Exception e) {
            return null;
        }


        return API.fileSystemHandler.saveFile(given_user_id, isVideo, files, "banner") ? "true" : "false";
    }

}
