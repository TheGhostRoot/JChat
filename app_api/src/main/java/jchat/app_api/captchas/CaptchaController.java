package jchat.app_api.captchas;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/captcha")
public class CaptchaController {

    private String captcha_server = null;

    public CaptchaController(String given_captcha_server) {
        captcha_server = given_captcha_server;
    }

    @GetMapping()
    public String getCaptcha(HttpServletRequest request) {
        String captcha_code = API.generateKey(7);
        Long captcha_id = API.databaseHandler.startCaptcha(captcha_code);
        if (captcha_id == null) {
            return null;
        }

        String captcha_image_base64_encoded = getCaptchaImage(captcha_code);

        Map<String, Object> claims = new HashMap<>();
        claims.put("captcha_id", captcha_id);
        claims.put("captcha_image", captcha_image_base64_encoded);
        return API.jwtService.generateGlobalJwt(claims, true);
    }


    @PostMapping()
    public String solveCaptcha(HttpServletRequest request) {
        String GlobalEncodedCaptchaID = request.getHeader(API.REQ_HEADER_CAPTCHA);
        if (null == GlobalEncodedCaptchaID) { return null; }

        long captcha_id;
        try {
            captcha_id = Long.parseLong(API.criptionService.GlobalDecrypt(GlobalEncodedCaptchaID));
        } catch (Exception e) { return null; }

        String jwt = request.getHeader(API.REQ_HEADER_AUTH);
        if (jwt == null) { return null; }

        Map<String, Object> data = API.jwtService.getData(jwt, null, null);
        if (data == null || !data.containsKey("answer")) {
            return null;
        }

        if (API.databaseHandler.solveCaptcha(captcha_id, String.valueOf(data.get("answer")))) {
            // solved!
            Map<String, Object> c = new HashMap<>();
            c.put("stats", true);
            return API.jwtService.generateGlobalJwt(c, true);

        }

        return null;

    }

    private String getCaptchaImage(String code) {
        if (captcha_server == null) { return null; }

        try {
            URL url = new URL(captcha_server + code);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);

            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while (null != (inputLine = in.readLine())) {
                response.append(inputLine);
            }

            in.close();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }



    private BufferedImage decodeBase64Image(String base64Image) {
        try {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Image)));
        } catch (Exception e) {
            return null;
        }
    }


}
