package jcorechat.app_api.captchas;

import jcorechat.app_api.API;

import java.util.*;

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
        API.captcha_expire.put(captcha_session_id, (short) 3);
        return captcha_session_id;
    }

    public static Character solvedCaptcha(HashSet<String> need_to_answer, HashSet<String> given_answer, long captcha_id) {
        if (need_to_answer.containsAll(given_answer)) {
            need_to_answer.clear();
            return 't';
        }
        return handleFaildCaptcha(captcha_id);
    }

    public static String GlobalEncoded_get_and_start_Captcha_Session() {
        HashSet<String> captcha = API.captahaManager.generateCapctcha();

        HashSet<String> answers = new HashSet<>();
        answers.add("123");
        long captcha_session_id = API.captahaManager.startCaptchaSession(answers);

        HashMap<Character, Object> map = new HashMap<>();
        map.put('c', captcha);
        map.put('s', captcha_session_id);
        return API.cription.GlobalEncrypt(map.toString());
    }


    public static Character handleFaildCaptcha(long captcha_id) {
        final Short failed = API.captcha_fails.get(captcha_id);
        if (null == failed || 1 > failed) {
            API.captcha_results.remove(captcha_id);
            API.captcha_expire.remove(captcha_id);
            API.captcha_fails.remove(captcha_id);
        }
        return 'f';
    }

}
