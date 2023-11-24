package jchat.app_api.accounts;

import jchat.app_api.API;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {


    public Long get_UserID_By_Username_and_Password(String user, String passoword) {
        Object user_id_by_name = get_Key_By_Value(API.names, user);
        if (null == user_id_by_name) { return null; }
        if (null == get_Key_By_Value(API.passwords, passoword)) { return null; }

        return (Long) user_id_by_name;
    }

    public Long get_UserID_By_AppSessionID(Long sessID) {
        return (Long) get_Key_By_Value(API.sessions, sessID);
    }

    public Object get_Key_By_Value(HashMap<?, ?> map, Object val) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue().equals(val)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public String get_EncryptionKey_By_UserID(Long id) { return API.encryption_user_keys.get(id); }

    public String get_SignKey_By_UserID(Long id) { return API.sign_user_keys.get(id); }

    public String get_Password_By_UserID(Long id) { return API.passwords.get(id); }

    public String get_Username_By_UserID(Long id) { return API.names.get(id); }

    public String get_Email_By_UserID(Long id) { return API.emails.get(id); }

    public long generate_Session(boolean captcha) {
        long session_id = 0L;
        while (captcha ? API.captcha_results.containsKey(session_id) : API.sessions.containsValue(session_id)) {
            session_id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return session_id;
    }

    public long generate_UserID() {
        long user_id = 0L;
        while (API.names.containsKey(user_id)) {
            user_id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return user_id;
    }

}
