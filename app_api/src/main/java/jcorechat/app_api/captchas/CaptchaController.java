package jcorechat.app_api.captchas;


import jakarta.servlet.http.HttpServletRequest;
import jcorechat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/captcha")
public class CaptchaController {


    @GetMapping
    public Map<Character, Object> getCaptcha(HttpServletRequest request) {
        final String GlobalEncodedSessID = request.getHeader("SessionID");
        final String jwt = request.getHeader("Authorization");

        if (null != GlobalEncodedSessID) {

            String sessID_str = API.cription.GlobalDecrypt(GlobalEncodedSessID);
            if (null == sessID_str) { return null; }

            long appSessID;
            try {
                appSessID = Long.valueOf(sessID_str);
            } catch (Exception e) { return null; }

            if (!API.sessions.containsValue(appSessID)) { return null; }

            Long user_id = API.accountManager.get_UserID_By_AppSessionID(appSessID);
            if (null == user_id) { return null; }

            Map<String, Object> data = API.jwtService.getData(jwt,
                    API.accountManager.get_EncryptionKey_By_UserID(user_id),
                    API.accountManager.get_SignKey_By_UserID(user_id));
            if (null == data) { return null; }

            return API.captahaManager.get_and_start_Captcha_Session();
        }

        Map<String, Object> data = API.jwtService.getData(jwt);
        if (null == data) { return null; }

        if (data.containsKey("u") && data.containsKey("p")) {
            String user = (String) data.get("u");
            String password = (String) data.get("p");

            Long user_id = API.accountManager.get_UserID_By_Username_and_Password(user, password);
            if (null == user_id) { return null; }
            if (null != API.sessions.get(user_id)) { return null; }

            return API.captahaManager.get_and_start_Captcha_Session();

        } else if (data.containsKey("i")) {
            long user_id = (Long) data.get("i");
            if (null != API.sessions.get(user_id)) { return null; }

            return API.captahaManager.get_and_start_Captcha_Session();

        } else { return null; }

    }


    @PostMapping
    public void solveCaptcha(HttpServletRequest request) {

    }


}
