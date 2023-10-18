package jcorechat.app_api.accounts;

import jcorechat.app_api.API;

import java.util.Map;

public class AccountManager {


    public Long getIDbyUser(String user, String passoword) {
        // For example. Only tests. Later on will be replaced
        boolean findUser = false;
        for (Map.Entry<Long, String> entry : API.names.entrySet()) {
            findUser = entry.getValue().equals(user);
            if (findUser) {
                break;
            }
        }
        for (Map.Entry<Long, String> entry : API.passwords.entrySet()) {
            if (entry.getValue().equals(passoword) && findUser) {
                return entry.getKey();
            }
        }
        return 0L;
    }

    public String getEncryptionUserKeyByID(Long id) { return API.encryption_user_keys.get(id); }

    public String getSignUserKeyByID(Long id) { return API.sign_user_keys.get(id); }

    public String getSessionUserByID(Long id) { return API.sessions.get(id); }

    public String getCodeUserKeyByID(Long id) { return API.code.get(id); }

    public String getPasswordUserKeyByID(Long id) { return API.passwords.get(id); }

    public String getNameUserKeyByID(Long id) { return API.names.get(id); }

    public String getEmailUserKeyByID(Long id) { return API.emails.get(id); }
}
