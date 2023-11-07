package jcorechat.app_api.captchas;

import jcorechat.app_api.API;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class CaptahaManager {

    public static HashSet<String> generateCapctcha() {
        return new HashSet<>(Collections.singleton("123"));
    }

    public static Long startCaptchaSession(HashSet<String> answer) {
        long captcha_session_id = 0L;
        while (API.captcha_results.containsKey(captcha_session_id)) {
            captcha_session_id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        API.captcha_results.put(captcha_session_id, answer);
        API.captcha_fails.put(captcha_session_id, (short) 3);
        API.captcha_expire.put(captcha_session_id, (short) 10);
        return captcha_session_id;
    }

    public static String solvedCaptcha(HashSet<String> need_to_answer, HashSet<String> given_answer, long captcha_id) {
        if (!given_answer.isEmpty() && need_to_answer.containsAll(given_answer)) {
            need_to_answer.clear();
            return API.jwtService.generateGlobalJwt(Collections.singletonMap("r", "t"), true);
        }
        return handleFaildCaptcha(captcha_id);
    }

    public static String GlobalEncoded_get_and_start_Captcha_Session() {
        HashSet<String> captcha = API.captahaManager.generateCapctcha();

        HashSet<String> answers = new HashSet<>();
        answers.add("123");
        long captcha_session_id = API.captahaManager.startCaptchaSession(answers);

        HashMap<String, Object> map = new HashMap<>();
        map.put("c", captcha);
        map.put("s", captcha_session_id);
        return API.jwtService.generateGlobalJwt(map, true);
    }


    public static String handleFaildCaptcha(long captcha_id) {
        final Short failed = API.captcha_fails.get(captcha_id);
        if (null == failed || 1 > failed) {
            API.captcha_results.remove(captcha_id);
            API.captcha_expire.remove(captcha_id);
            API.captcha_fails.remove(captcha_id);
        } else {
            API.captcha_fails.put(captcha_id, (short) (failed - 1));
        }
        return API.jwtService.generateGlobalJwt(Collections.singletonMap("r", "f"), true);
    }

}
