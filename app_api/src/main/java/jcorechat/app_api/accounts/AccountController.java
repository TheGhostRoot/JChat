package jcorechat.app_api.accounts;


import jcorechat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController()
@RequestMapping(path = "/api/v"+API.API_VERSION)
public class AccountController {

    @GetMapping("/account")
    public String test() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("username", "Idk");
        map.put("password", "123");
        return API.jwtService.generateEncryptJwt(map, "Creatig Account");
    }
}
