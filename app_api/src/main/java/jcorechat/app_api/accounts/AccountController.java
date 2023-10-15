package jcorechat.app_api.accounts;


import jcorechat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(path = "/api/v"+API.API_VERSION)
public class AccountController {


    @GetMapping
    public String hi() {
        return "API v1";
    }

    @GetMapping("/account")
    public String test() {
        return "Your Account";
    }
}
