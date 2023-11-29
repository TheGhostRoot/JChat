package jchat.app_api.captchas;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/captcha")
public class CaptchaController {


    @GetMapping()
    public String getCaptcha(HttpServletRequest request) {
        Long captcha_id = API.databaseHandler.startCaptcha("123");
        if (captcha_id == null) {
            return null;
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("cap_id", captcha_id);
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
        if (data == null || !data.containsKey("answ")) {
            return null;
        }

        if (API.databaseHandler.solveCaptcha(captcha_id, String.valueOf(data.get("answ")))) {
            // solved!
            Map<String, Object> c = new HashMap<>();
            c.put("stats", true);
            return API.jwtService.generateGlobalJwt(c, true);

        }

        return null;

    }


}
