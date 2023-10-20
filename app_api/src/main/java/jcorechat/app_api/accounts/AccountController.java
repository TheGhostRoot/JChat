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


    @GetMapping
    public Map<Character, Object> getAccount(HttpServletRequest request) {

        String IP = API.get_IP(request);
        if (null == IP) { return null; }

        Map<String, Object> data = API.jwtService.getData(request.getHeader("Authorization"));
        if (null == data) { return null; }

        String key;
        String sign;
        long app_session_id;

        if (data.containsKey("u") && data.containsKey("p")) {
            String user = (String) data.get("u");
            String password = (String) data.get("p");

            Long user_id = API.accountManager.get_UserID_By_Username_and_Password(user, password);
            if (null == user_id) { return null; }
            if (null != API.sessions.get(user_id)) { return null; }

            key = API.accountManager.get_EncryptionKey_By_UserID(user_id);
            sign = API.accountManager.get_SignKey_By_UserID(user_id);
            app_session_id = API.accountManager.generate_Session(false);
            API.sessions.put(user_id, app_session_id);

        } else if (data.containsKey("i")) {
            // remember me
            long id = (Long) data.get("i");
            if (null != API.sessions.get(id)) { return null; }

            key = API.accountManager.get_EncryptionKey_By_UserID(id);
            sign = API.accountManager.get_SignKey_By_UserID(id);
            app_session_id = API.accountManager.generate_Session(false);
            API.sessions.put(id, app_session_id);

        } else { return null; }

        Map<Character, Object> respose_data = new HashMap<>();

        respose_data.put('k', key);
        respose_data.put('s', sign);
        respose_data.put('a', app_session_id);

        return respose_data;
    }
}
