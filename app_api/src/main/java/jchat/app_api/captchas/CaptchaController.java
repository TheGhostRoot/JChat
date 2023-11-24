package jchat.app_api.captchas;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/captcha")
public class CaptchaController {


    @GetMapping()
    public String getCaptcha(HttpServletRequest request) {
        final String auth = request.getHeader("Authorization");
        if (null == API.jwtService.getData(auth)) { return null; }
        return CaptahaManager.GlobalEncoded_get_and_start_Captcha_Session();
    }


    @PostMapping()
    public String solveCaptcha(HttpServletRequest request) {
        final String GlobalEncodedCaptchaID = request.getHeader("CapctchaID");
        if (null == GlobalEncodedCaptchaID) { return null; }

        Long captcha_id = null;
        try {
            captcha_id = Long.valueOf(API.cription.GlobalDecrypt(GlobalEncodedCaptchaID));
        } catch (Exception e) { return null; }

        HashSet<String> answers = null;
        try {
            answers = API.captcha_results.get(captcha_id);
        } catch (Exception e) { return CaptahaManager.handleFaildCaptcha(captcha_id); }

        if (null == answers) { return CaptahaManager.handleFaildCaptcha(captcha_id); }

        final String jwt = request.getHeader("Authorization");
        if (null == jwt) { return CaptahaManager.handleFaildCaptcha(captcha_id); }

        Map<String, Object> data = API.jwtService.getData(jwt);

        if (null == data) { return CaptahaManager.handleFaildCaptcha(captcha_id); }

        final HashSet<String> given_answers = new HashSet<>((List<String>) data.get("c"));
        return CaptahaManager.solvedCaptcha(answers, given_answers, captcha_id);

    }


}
