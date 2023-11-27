package jchat.app_api.accounts;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/account")
public class AccountController {


    @PostMapping()
    private String createAccount(HttpServletRequest request) {
        Map<String, Object> data = API.jwtService.getData(request.getHeader("Authorization"));
        if (null == data) { return null; }

        String captcha_id_str = request.getHeader("CapctchaID");
        Long captch_id = null;

        try {
            captch_id = Long.parseLong(API.criptionService.GlobalDecrypt(captcha_id_str));
        } catch (Exception e) { return null; }


        if (!API.databaseHandler.checkIfSolvedCaptcha(captch_id)) {
            return null;
        }

        if (data.containsKey("u") && data.containsKey("p") && data.containsKey("e")) {
            String user = null;
            String password = null;
            String email = null;

            try {
                user = (String) data.get("u");
                password = (String) data.get("p");
                email = (String) data.get("e");
            } catch (Exception e) { return null; }

            if (null == user || null == password || null == email) { return null; }

            Map<String, Object> map = new HashMap<>();

            Long user_id = API.databaseHandler.createUser(user, email, password,
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserEncryptionKey()),
                    API.criptionService.GlobalEncrypt(API.databaseHandler.generateUserSignKey()));

            map.put("i", user_id == null ? 0l : user_id);

            return API.jwtService.generateGlobalJwt(map, true);

        }

        return null;

    }


    @GetMapping()
    public String getAccount(HttpServletRequest request) {
        Map<String, Object> data = API.jwtService.getData(request.getHeader("Authorization"));
        if (null == data) { return null; }

        String captcha_id_str = request.getHeader("CapctchaID");
        Long captch_id = null;

        try {
            captch_id = Long.parseLong(API.criptionService.GlobalDecrypt(captcha_id_str));
        } catch (Exception e) { return null; }

        if (!API.databaseHandler.checkIfSolvedCaptcha(captch_id)) {
            return null;
        }

        long id;

        if (data.containsKey("p") && data.containsKey("e")) {
            String email = null;
            String password = null;

            try {
                email = (String) data.get("e");
                password = (String) data.get("p");
            } catch (Exception e) { return null; }

            if (email == null || password == null) {
                return null;
            }

            // get user id by username, password and email
            Long user_id = API.databaseHandler.getUserByDetails(email, password);
            id = user_id == null ? 0l : user_id;

        } else if (data.containsKey("i")) {
            // remember me
            try {
                id = Long.parseLong(data.get("i").toString());
            } catch (Exception e) { return null; }

        } else { return null; }

        if (!API.databaseHandler.checkIfUserExists(id)) {
            return null;
        }

        Long sess_id = API.databaseHandler.startUserSessionID(id, API.get_IP(request));
        if (sess_id != null) {
            Map<String, Object> user_data = API.databaseHandler.getUserByID(id);
            Map<String, Object> respose_data = new HashMap<>();

            // encryption key
            respose_data.put("k", user_data.get("encryption_key"));
            // sign key
            respose_data.put("s", user_data.get("sign_key"));
            // app session id
            respose_data.put("a", sess_id);

            return API.jwtService.generateGlobalJwt(respose_data, true);

        }

        return null;
    }
}
