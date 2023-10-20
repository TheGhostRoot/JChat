package jcorechat.app_api.captchas;

import jcorechat.app_api.API;

import java.util.*;

public class CaptahaManager {

    public HashSet<String> generateCapctcha() {
        return new HashSet<>(Collections.singleton("123"));
    }

    public Long startCaptchaSession(HashSet<String> answer) {
        long captcha_session_id = 0L;
        while (API.captcha_results.containsKey(captcha_session_id)) {
            captcha_session_id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        API.captcha_results.put(captcha_session_id, answer);
        return captcha_session_id;
    }

    public boolean solvedCaptcha(Long captcha_session, HashSet<String> answer) {
        HashSet<String> need_to_answer = API.captcha_results.get(captcha_session);
        if (null == need_to_answer) { return false; }
        if (need_to_answer.isEmpty() || need_to_answer.containsAll(answer)) {
            need_to_answer.clear();
            return true;
        }
        return false;
    }

    public boolean hasCaptchaSession(Long captcha_session) {
        return API.captcha_results.containsKey(captcha_session);
    }

    public static HashMap<Character, Object> get_and_start_Captcha_Session() {
        HashSet<String> captcha = API.captahaManager.generateCapctcha();

        HashSet<String> answers = new HashSet<>();
        answers.add("123");
        long captcha_session_id = API.captahaManager.startCaptchaSession(answers);

        HashMap<Character, Object> map = new HashMap<>();
        map.put('c', captcha);
        map.put('s', captcha_session_id);
        return map;
    }

}
