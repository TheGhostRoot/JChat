package jcorechat.app_api.accounts;


import jakarta.servlet.http.HttpServletRequest;
import jcorechat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+API.API_VERSION)
public class AccountController {


    // The only GET endpoint with Global Encryption
    @GetMapping("/account")
    public Map<Character, Object> getAccount(HttpServletRequest request) {

        Map<String, Object> data = API.jwtService.getData(request.getHeader("Authorization").substring(7));
        if (null == data) { return null; }

        Map<Character, Object> respose_data = new HashMap<>();

        String key = null;
        String sign = null;
;
        if (data.containsKey("u") && data.containsKey("p")) {
            String user = (String) data.get("u");
            String password = (String) data.get("p");

            long user_id = API.accountManager.getIDbyUser(user, password);
            key = API.accountManager.getEncryptionUserKeyByID(user_id);
            sign = API.accountManager.getSignUserKeyByID(user_id);

        } else if (data.containsKey("i")) {
            // remember me
            long id = (Long) data.get("i");

            key = API.accountManager.getEncryptionUserKeyByID(id);
            sign = API.accountManager.getSignUserKeyByID(id);

        } else { return null; }

        respose_data.put('k', key);
        respose_data.put('s', sign);

        return respose_data;
    }
}
