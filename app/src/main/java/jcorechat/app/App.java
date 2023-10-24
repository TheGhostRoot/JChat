package jcorechat.app;


import jcorechat.app.security.Cription;
import jcorechat.app.security.JwtSecurity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static Cription cription;

    public static JwtSecurity jwtSecurity;

    public static void main(String[] args) {

        cription = new Cription();
        jwtSecurity = new JwtSecurity();


        Map<String, Object> user_data = new HashMap<>();
        user_data.put("u", "My name is");
        user_data.put("p", "123");
        user_data.put("e", "test@mail.example");

        jwtSecurity.generateGlobalJwt(user_data, true);

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", jwtSecurity.generateGlobalJwt(user_data, true));

        String res = RequestsHandler.get("http://localhost:25533/api/v1/captcha", headers);
        System.out.println("Captcha responce: "+res);


        Map<String, Object> data = jwtSecurity.getData(res);
        System.out.println("Captcha responce Data: "+data);


        Map<String, Object> captcha_solve = new HashMap<>();
        captcha_solve.put("c", Collections.singletonList("123"));

        headers.clear();
        headers.put("Authorization", jwtSecurity.generateGlobalJwt(captcha_solve, true));
        headers.put("CapctchaID", cription.GlobalEncrypt(data.get("s").toString()));

        String res_solve = RequestsHandler.post("http://localhost:25533/api/v1/captcha", headers);
        System.out.println("Captcha solve responce: "+res_solve);

        Map<String, Object> data_solve = jwtSecurity.getData(res_solve);
        System.out.println("Captcha solve responce Data: "+data_solve);

        headers.clear();
        headers.put("Authorization", jwtSecurity.generateGlobalJwt(user_data, true));
        headers.put("CapctchaID", cription.GlobalEncrypt(data.get("s").toString()));

        String account_create_res = RequestsHandler.post("http://localhost:25533/api/v1/account", headers);
        System.out.println("Account Create responce: "+account_create_res);

        data.clear();
        data = jwtSecurity.getData(account_create_res);
        System.out.println("Account Create responce Data: "+data);


        String account_res = RequestsHandler.get("http://localhost:25533/api/v1/account", headers);
        System.out.println("Account Get responce: "+account_res);

        data.clear();
        data = jwtSecurity.getData(account_res);
        System.out.println("Account Get responce Data: "+data);

    }
}

