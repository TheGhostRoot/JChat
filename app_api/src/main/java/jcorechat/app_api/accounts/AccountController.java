package jcorechat.app_api.accounts;


import jakarta.servlet.http.HttpServletRequest;
import jcorechat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+API.API_VERSION+"/account")
public class AccountController {


    @GetMapping()
    public String getAccount(HttpServletRequest request) {

        final Map<String, Object> data = API.jwtService.getData(request.getHeader("Authorization"));
        if (null == data) { return null; }

        final String captcha_id_str = request.getHeader("CapctchaID");
        Long captch_id = null;

        try {
            captch_id = Long.parseLong(API.cription.GlobalDecrypt(captcha_id_str));
        } catch (Exception e) { return null; }

        try {
            if (!API.captcha_results.get(captch_id).isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        long id;

        if (data.containsKey("u") && data.containsKey("p")) {
            String user = null;
            String password = null;

            try {
                user = (String) data.get("u");
                password = (String) data.get("p");
            } catch (Exception e) { return null; }

            Long user_id = API.accountManager.get_UserID_By_Username_and_Password(user, password);
            if (null == user_id || null != API.sessions.get(user_id)) { return null; }

            id = user_id;

        } else if (data.containsKey("i")) {
            // remember me
            Long user_id = null;
            try {
                user_id = Long.parseLong(data.get("i").toString());
            } catch (Exception e) { return null; }

            if (null != API.sessions.get(user_id)) { return null; }

            id = user_id;

        } else { return null; }

        long app_session_id = API.accountManager.generate_Session(false);

        API.sessions.put(id, app_session_id);

        Map<String, Object> respose_data = new HashMap<>();

        // encryption key
        respose_data.put("k", API.accountManager.get_EncryptionKey_By_UserID(id));
        // sign key
        respose_data.put("s", API.accountManager.get_SignKey_By_UserID(id));
        // app session id
        respose_data.put("a", app_session_id);

        API.captcha_fails.remove(captch_id);
        API.captcha_results.remove(captch_id);
        API.captcha_expire.remove(captch_id);

        return API.jwtService.generateGlobalJwt(respose_data, true);
    }
}
