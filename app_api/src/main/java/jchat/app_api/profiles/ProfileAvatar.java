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
@RequestMapping(path = "/api/v"+ API.API_VERSION+ "/profile/avatar")
public class ProfileAvatar {


    @GetMapping
    public Object getProfileAvatar(HttpServletRequest request, @RequestParam("redirected") boolean redirected,
                                   @RequestParam("type") String type) {


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

        if (redirected && !type.isBlank()) {
            File avatarVideo = new File("./attachments/" +user_id+"/pfp.mp4");
            File avatarImg = new File("./attachments/" +user_id+"/pfp.jpg");
            if (type.equals("video") && avatarVideo.exists()) {
                try {
                    return "video;" + (new String(Base64.encodeBase64(FileUtils.readFileToByteArray(avatarVideo)), StandardCharsets.US_ASCII));

                } catch (Exception e) {
                    return null;
                }

            } else if (avatarImg.exists()) {
                try {
                    return new String(Base64.encodeBase64(FileUtils.readFileToByteArray(avatarImg)), StandardCharsets.US_ASCII);

                } catch (Exception e) {
                    return null;
                }

            } else {
                try {
                    return new String(Base64.encodeBase64(FileUtils.readFileToByteArray(new File("profile/avatar/pfp.jpg"))), StandardCharsets.US_ASCII);

                } catch (Exception e) {
                    return null;
                }
            }
            /*
            if (type.equals("video") && file.exists()) {
                try {
                    byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
                    return new String(encoded, StandardCharsets.UTF_8);

                } catch (Exception e) {
                    return null;
                }

                return """
                <html><head></head>
                <body>

                <video controls autoplay muted name="media">
                    <source type="video/mp4" src="/attachments/"""+ given_user_id + """
                 /avatar.mp4">

                 </video>

                 </body></html>""";



            } else if (new File("/attachments/" +given_user_id+"/pfp.jpg").exists()) {


                return """
                        <html><head></head>
                        <body>

                                <img src="/attachments/"""+ given_user_id + """
                         /black.jpg">

                         </body></html>""";




            }
            return """
        <html><head></head><body>

        <img src="/profile/avatar/pfp.jpg">

        </body></html>""";*/

        } else {
            Map<String, Object> profile = API.databaseHandler.getProfile(user_id);
            if (profile == null) {
                return null;
            }

            String server = String.valueOf(profile.get("pfp"));
            if (server.isBlank() || server.equals("null")) {
                server = API.upload_server.get(API.random.nextInt(0, API.upload_server.size()));
            }

            server += "/avatar";

            boolean isVideo = server.startsWith("video;");

            String base64Avatar = API.sendRequestToUploads((isVideo ? server.substring(6) : server),
                    request.getHeader(API.REQ_HEADER_AUTH),"GET", isVideo);


            if (base64Avatar == null) {
                return null;
            }

            if (base64Avatar.startsWith("video;")) {

                return Base64.decodeBase64(base64Avatar.substring(6));

                /*
                return """
                        <html><head></head><body>
                        
                        <video controls autoplay muted> 
                            <source type="video/mp4" src="data:video/mp4;base64, """ + base64Avatar.substring(6) + """
                            ">
                        </video>
                         
                                            
                        </body>
                        </html>
                        """;

                 */

            } else {

                return base64Avatar;
            }


        }
    }


    @PostMapping()
    public String uploadProfileAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                      @RequestParam("video") boolean isVideo, @RequestParam("id") long given_user_id) {

        byte[] files;
        try {
            if (file.isEmpty()) {
                return null;
            }
            files = file.getBytes();
        } catch (Exception e) {
            return null;
        }

        return API.fileSystemHandler.saveFile(given_user_id, isVideo, files, "pfp") ? "true" : "false";
    }
}
