package jchat.app_api.database;


import jchat.app_api.API;
import org.bson.Document;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatabaseHandler {

    private DatabaseManager databaseManager;

    public DatabaseHandler(DatabaseManager given_databaseManager) {
        databaseManager = given_databaseManager;
    }


    public boolean addNotification(long id, Object update, String notifi_type) {
        if (databaseManager.isSQL()) {
            List<Object> content = new ArrayList<>();
            content.add(id);

            Map<String, List<Object>> settings = databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "settings", "id = ?", content, null, "", 0);

            Map<String, Boolean> user_settings = null;

            try {
                for (Map.Entry<String, Object> entry : API.jwtService.getDataNoEncryption(String.valueOf(settings
                        .get("settings").get(0))).entrySet()) {
                    user_settings.put(entry.getKey(), Boolean.parseBoolean(String.valueOf(entry.getValue())));
                }
            } catch (Exception e) {
                return false;
            }

            if (user_settings == null || !user_settings.containsKey(notifi_type) || !user_settings.get(notifi_type)) {
                return false;
            }

            content.add(String.valueOf(update));

            return databaseManager.addDataSQL(DatabaseManager.table_notifications, "id, notification",
                    "?, ?", content);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_notifications,
                    new Document("id", id).append("notification", update), null);
        }
        return false;
    }

    public boolean removeNotifications(long id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_notifications, "id = ?", condition);

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_notifications,
                    new Document("id", id));
        }

        return false;
    }

    public Map<String, List<Object>> getNotifications(long id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(id);

            return databaseManager.getDataSQL(DatabaseManager.table_notifications,
                    "notification", "id = ?", condition, null, "", amount);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_notifications = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_notifications,
                    new Document("id", id), false, amount, "notification");

            if (all_notifications == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("notification", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, all_notifications, res);

        }

        return null;
    }

    public Long createUser(String name, String email, String password, String encryption_key, String sign_key,
                           String settings) {
        if (databaseManager.isSQL()) {
            List<Object> account_details = new ArrayList<>();
            account_details.add(name);
            account_details.add(email);
            account_details.add(password);
            account_details.add(encryption_key);
            account_details.add(sign_key);
            account_details.add(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
            account_details.add(settings);

            if (!databaseManager.addDataSQL(DatabaseManager.table_accounts,
                    "name, email, password, encryption_key, sign_key, session_id, session_expire, last_edit_time, created_at, friends,  starts_sub, ends_sub, bookmarks, settings",
                    "?, ?, ?, ?, ?, NULL, NULL, NULL, ?, '', NULL, NULL, '', ?", account_details)) {
                return null;
            }


            List<Object> search_condition = new ArrayList<>();
            search_condition.add(email);
            search_condition.add(encryption_key);
            search_condition.add(sign_key);

            Map<String, List<Object>> account_data = databaseManager.getDataSQL(
                    databaseManager.table_accounts, "id",
                    "email = ? AND encryption_key = ? AND sign_key = ?", search_condition,
                    null, "", 0);

            if (account_data == null || account_data.isEmpty()) {
                return null;
            }

            List<Object> profile_details = new ArrayList<>();
            Long id;
            try {
                id = Long.parseLong(String.valueOf(account_data.get("id").get(0)));
            } catch (Exception e) {
                return null;
            }
            profile_details.add(id);

            if (!databaseManager.addDataSQL(DatabaseManager.table_profiles, "id, pfp, banner, badges, animations, about_me, stats",
                    "?, '', '', '', NULL, '', '0'", profile_details)) {

                databaseManager.deleteDataSQL(DatabaseManager.table_accounts, "id = ?", profile_details);
                return null;
            }

            return id;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> accounts_id_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null,
                    false, 0, "id", "name", "email", "sign_key", "encryption_key");

            Map<String, Object> values = new HashMap<>();
            values.put("name", name);
            values.put("email", email);
            values.put("encryption_key", encryption_key);
            values.put("sign_key", sign_key);

            if (accounts_id_data == null || !databaseManager.checkUnique(accounts_id_data, values)) {
                return null;
            }

            long account_ID = databaseManager.generateID(databaseManager.extract_all_content(accounts_id_data,
                    "id"));

            if (!databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", account_ID).append("name", name)
                            .append("email", email).append("password", password)
                            .append("encryption_key", encryption_key).append("sign_key", sign_key)
                            .append("session_id", null).append("session_expire", null).append("last_edit_time", null)
                            .append("created_at", LocalDateTime.now())
                            .append("starts_sub", null).append("ends_sub", null).append("bookmarks", "")
                            .append("friends", "").append("settings", settings)
                    , null)) {

                return null;
            }

            if (!databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", account_ID)
                    .append("pfp", "").append("banner", "")
                    .append("badges", "")
                    .append("animations", null)
                            .append("about_me", "").append("stats", "0"), null)) {


                databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_accounts,
                        new Document("id", account_ID));
                return null;
            }


            return account_ID;

        }
        return null;
    }

    public boolean updateUserSettings(long id, String new_settings) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_settings);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "settings = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("settings", new_settings));

        }
        return false;
    }

    public Long getUserByDetails(String email, String password) {
        // password can be null
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(email);
            if (password != null) {
                condition.add(password);
            }

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_accounts,
                        "id", password == null ? "email = ?" : "email = ? AND password = ?", condition,
                        null, "" ,0).get("id").get(0)));
            } catch (Exception e) {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                        password == null ? new Document("email", email) :
                                new Document("email", email).append("password", password),
                        true, 0, "id").get(0).get("id")));
            } catch (Exception e) {
                return null;
            }

        }
        return null;
    }

    public boolean checkIfUserExists(long id) {
        return databaseManager.checkIDExists(id, DatabaseManager.table_accounts);
    }

    public Long getUserIDbySessionID(long sess_id, String ip) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(sess_id);
            condition.add(ip);

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_accounts,
                        "id", "session_id = ? AND ip_address = ?", condition, null,
                        "", 0).get("id").get(0)));
            } catch (Exception e) {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                        new Document("session_id", sess_id).append("ip_address", ip), true, 0, "id")
                        .get(0).get("id")));
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }


    public long getUserIDByName(String name) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(name);

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_accounts,
                        "id", "name = ?", condition, null, "", 0).get("id").get(0)));

            } catch (Exception e) {
                return 0;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                        new Document("name", name), true, 0, "id").get(0).get("id")));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
    public Map<String, Object> getUserByID(long id) {
        if (!databaseManager.checkIDExists(id, DatabaseManager.table_accounts)) {
            return null;
        }

        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(id);

            Map<String, List<Object>> user_data = databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "*", "id = ?", condition, null, "", 0);

            if (user_data == null) {
                return null;
            }

            Map<String, Object> res = new HashMap<>();

            for (Map.Entry<String, List<Object>> entry : user_data.entrySet()) {
                res.put(entry.getKey(), entry.getValue().get(0));
            }

            return res;

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id), true, 0).get(0);
        }
        return null;
    }

    public boolean updateUserEmail(long id, String new_email) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_email);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            addNotification(id, Collections.singletonMap("new_email", new_email), "change_email");

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "email = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_emails = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "email");

            if (all_emails == null || databaseManager.checkNotUniqueWithStream(all_emails, "email", new_email)) {
                return false;
            }

            addNotification(id, Collections.singletonMap("new_email", new_email), "change_email");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("email", new_email));

        }
        return false;
    }

    public boolean updateUserName(long id, String new_name) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_name);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "name = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_names = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "name");

            if (all_names == null || databaseManager.checkNotUniqueWithStream(all_names, "name", new_name)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("name", new_name));

        }
        return false;
    }

    public boolean updateUserPassword(long id, String new_password) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_password);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            addNotification(id, Collections.singletonMap("new_password", new_password), "change_password");

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "password = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {

            addNotification(id, Collections.singletonMap("new_password", new_password), "change_password");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts, new Document("id", id),
                    new Document("password", new_password));

        }
        return false;
    }

    public boolean updateUserStartsSub(long id, LocalDateTime new_starts) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_starts);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            addNotification(id, Collections.singletonMap("start_sub", new_starts), "start_sub");

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "starts_sub = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {

            addNotification(id, Collections.singletonMap("start_sub", new_starts), "start_sub");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("starts_sub", new_starts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        }
        return false;
    }

    public boolean updateUserEndsSub(long id, LocalDateTime new_stops) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_stops);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            addNotification(id, Collections.singletonMap("end_sub", new_stops), "end_sub");

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "ends_sub = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {

            addNotification(id, Collections.singletonMap("end_sub", new_stops), "end_sub");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("ends_sub", new_stops.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        }
        return false;
    }

    public boolean updateUserBookmarks(long id, String new_bookmarks) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_bookmarks);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "bookmarks = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("bookmarks", new_bookmarks));

        }
        return false;
    }

    public boolean updateUserEncryptionKey(long id, String new_encryptino_key) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_encryptino_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "encryption_key = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "encryption_key");

            if (all_keys == null || databaseManager.checkNotUniqueWithStream(all_keys, "encryption_key", new_encryptino_key)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("encryption_key", new_encryptino_key));

        }
        return false;
    }

    public boolean updateUserEncryptionKey(long id) {
        String generateUserEncryptionKey = generateUserEncryptionKey();
        if (generateUserEncryptionKey == null) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(generateUserEncryptionKey);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "encryption_key = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "encryption_key");

            if (all_keys == null) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("encryption_key", generateUserEncryptionKey));

        }
        return false;
    }

    public boolean updateUserSignKey(long id, String new_sign_key) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_sign_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "sign_key = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "sign_key");

            if (all_keys == null || databaseManager.checkNotUniqueWithStream(all_keys, "sign_key", new_sign_key)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("sign_key", new_sign_key));

        }
        return false;
    }

    public boolean updateUserSessionIP(long id, String ip) {
        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(ip);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "ip_address = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "ip_address");

            if (all_keys == null || databaseManager.checkNotUniqueWithStream(all_keys, "ip_address", ip)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("ip_address", ip));

        }
        return false;
    }

    public boolean updateUserSignKey(long id) {
        String new_sign_key = generateUserSignKey();
        if (new_sign_key == null) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_sign_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "sign_key = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "sign_key");

            if (all_keys == null || databaseManager.checkNotUniqueWithStream(all_keys, "sign_key", new_sign_key)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("sign_key", new_sign_key));

        }
        return false;
    }

    public Long startUserSessionID(long id, String ip) {
        Long session_id = generateSessionID();
        if (session_id == null) { return null; }

        if (databaseManager.isSQL()) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(session_id);
            account_set.add(ip);
            account_set.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            if (databaseManager.editDataSQL(DatabaseManager.table_accounts,
                    "session_id = ?, ip_address = ?, session_expire = 3, last_edit_time = ?", account_set,
                    "id = ?", account_where)) {
                return session_id;
            }
            return null;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "session_id");

            if (all_keys == null || databaseManager.checkNotUniqueWithStream(all_keys, "session_id", session_id)) {
                return null;
            }

            if (databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("session_id", session_id).append("session_expire", 3)
                            .append("last_edit_time", LocalDateTime.now()).append("ip_address", ip))) {
                return session_id;
            }
            return null;

        }
        return null;
    }

    public Long generateSessionID() {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> sess_ids =  databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "session_id", "", null, null, "", 0);

            if (sess_ids == null || sess_ids.isEmpty() || sess_ids.get("session_id") == null) {
                return null;
            }

            return databaseManager.generateID(sess_ids.get("session_id"));

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "session_id");

            if (all_keys == null) {
                return null;
            }

            return databaseManager.generateID(databaseManager.extract_all_content(all_keys, "session_id"));

        }
        return null;
    }

    public String generateUserEncryptionKey() {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> sess_ids =  databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "encryption_key", "", null, null, "", 0);

            if (sess_ids == null || sess_ids.isEmpty() || sess_ids.get("encryption_key") == null) {
                return null;
            }

            List<String> toContain = new ArrayList<>();
            for (Object key : sess_ids.get("encryption_key")) {
                toContain.add(String.valueOf(key));
            }

            return API.generateKey(toContain, 24);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "encryption_key");

            if (all_keys == null) {
                return null;
            }

            List<String> toContain = new ArrayList<>();
            for (Object key : databaseManager.extract_all_content(all_keys, "encryption_key")) {
                toContain.add(String.valueOf(key));
            }

            return API.generateKey(toContain, 24);

        }
        return null;
    }

    public String generateUserSignKey() {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> sess_ids =  databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "sign_key", "", null, null, "", 0);

            if (sess_ids == null || sess_ids.isEmpty() || sess_ids.get("sign_key") == null) {
                return null;
            }

            List<String> toContain = new ArrayList<>();
            for (Object key : sess_ids.get("sign_key")) {
                toContain.add(String.valueOf(key));
            }

            return API.generateKey(toContain, 50);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "sign_key");

            if (all_keys == null) {
                return null;
            }

            List<String> toContain = new ArrayList<>();
            for (Object key : databaseManager.extract_all_content(all_keys, "sign_key")) {
                toContain.add(String.valueOf(key));
            }

            return API.generateKey(toContain, 50);

        }
        return null;
    }

    public boolean updateUserSessionExpire(long user_id) {
        if (databaseManager.isSQL()) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(user_id);

            Map<String, List<Object>> sess_data = databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "last_edit_time, session_expire",
                    "id = ?", account_where, null, "", 0);


            if (sess_data == null || sess_data.get("last_edit_time") == null ||
                    sess_data.get("last_edit_time").isEmpty() || sess_data.get("session_expire") == null ||
                    sess_data.get("session_expire").isEmpty()) {
                return false;
            }

            String last_edit_time_text = String.valueOf(sess_data.get("last_edit_time").get(0)); // if null: no such session
            String session_expire_text = String.valueOf(sess_data.get("session_expire").get(0)); // if null: no such session

            if (last_edit_time_text.equals("null") || session_expire_text.equals("null") ||
                    !databaseManager.isOneSecondAgo(Timestamp.valueOf(last_edit_time_text))) {
                return false;
            }

            Short session_expire;
            try {
                session_expire = Short.parseShort(session_expire_text);
            } catch (Exception e) {
                return false;
            }

            if (session_expire <= 0) {
                // session expired. END IT
                return databaseManager.editDataSQL(DatabaseManager.table_accounts,
                        "session_expire = NULL, ip_address = NULL, session_id = NULL, last_edit_time = NULL",
                        null, "id = ?", account_where);
            }

            List<Object> account_set = new ArrayList<>();
            account_set.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return databaseManager.editDataSQL(DatabaseManager.table_accounts,
                    "last_edit_time = ?, session_expire = session_expire - 1", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", user_id);

            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    filter, true, 0,
                    "last_edit_time", "session_expire");

            if (account_data == null || account_data.get(0) == null ||
                    account_data.get(0).get("last_edit_time") == null ||
                    account_data.get(0).get("session_expire") == null ||
                    !databaseManager.isOneSecondAgo(Timestamp.valueOf(String.valueOf(account_data.get(0).get("last_edit_time"))))) {

                return false;
            }

            Short session_expire;
            try {
                session_expire = Short.parseShort(String.valueOf(account_data.get(0).get("session_expire")));
            } catch (Exception e) {
                return false;
            }

            if (session_expire <= 0) {
                // remove the session
                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts, filter,
                        new Document("session_expire", null).append("last_edit_time", null)
                                .append("session_id", null).append("ip_address", null));
            }

            session_expire--;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts, filter,
                    new Document("session_expire", session_expire)
                            .append("last_edit_time", LocalDateTime.now()));

        }
        return false;
    }

    public boolean addUserFriend(long id, long friend_id, String current_friends) {
        if (current_friends.contains("," + friend_id) ||
                !databaseManager.checkIDExists(friend_id, DatabaseManager.table_accounts)) {
            return false;
        }


        String friends_value = new StringBuilder(current_friends).append(",").append(friend_id).toString();
        if (databaseManager.isSQL()) {

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(friends_value);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "friends = ?",
                    account_friends, "id = ?", account_where);

            /*
            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    databaseManager.postgressql_connection != null ? "friends = friends || ?" :
                            "friends = CONCAT(friends, ?)", account_friends, "id = ?", account_where);*/

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", friends_value));

        }
        return false;
    }

    public boolean removeUserFriend(long id, long friend_id, String current_friends) {
        String friend_text = "," + friend_id;
        if (!current_friends.contains(friend_text) ||
                !databaseManager.checkIDExists(friend_id, DatabaseManager.table_accounts)) {
            return false;
        }

        String friends_value = current_friends.replace(friend_text, "");
        if (databaseManager.isSQL()) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(friends_value);

            return databaseManager.editDataSQL(DatabaseManager.table_accounts,
                    "friends = ?", account_friends, "id = ?",
                    account_where);

            /*
            return databaseManager.editDataSQL(DatabaseManager.table_accounts,
                    "friends = REPLACE(friends, ?, '')", account_friends, "id = ?",
                    account_where);

             */

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", friends_value));

        }
        return false;
    }

    public void handleSessions() {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> account_data = databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "id", "",
                    null, null, "", 0);

            if (account_data == null) {
                return;
            }

            for (Object account_id : account_data.get("id")) {
                try {
                    updateUserSessionExpire(Long.parseLong(String.valueOf(account_id)));
                } catch (Exception e) {
                }
            }


        } else if (databaseManager.isMongo()) {

            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, 0, "id");

            if (account_data == null) {
                return;
            }

            for (Map<String, Object> ids : account_data) {
                try {
                    updateUserSessionExpire(Long.parseLong(String.valueOf(ids.get("id"))));
                } catch (Exception e) {
                }
            }

        }
    }


    public boolean addMessage(long channel_id, long sender_id, long resiver_id, String message, long group_id) {
        if (message.isBlank()) {
            return false;
        }

        if ((databaseManager.isSQL() || databaseManager.isMongo()) &&
                        databaseManager.checkIDExists(sender_id, DatabaseManager.table_accounts)) {

            LocalDateTime now = LocalDateTime.now();
            if (group_id == 0L && resiver_id != 0L &&
                    databaseManager.checkIDExists(resiver_id, DatabaseManager.table_accounts)) {

                Map<String, Object> update = new HashMap<>();
                update.put("channel_id", channel_id);
                update.put("sender_id", sender_id);
                update.put("resiver_id", resiver_id);
                update.put("message", message);
                update.put("group_id", group_id);

                addNotification(sender_id, update, "new_message");

                return databaseManager.handleMessage(channel_id, sender_id, message, now, resiver_id, group_id);

            } else if (databaseManager.doesUserHavePermissionsInChannel(channel_id,
                        List.of("send_message"), sender_id, group_id)) {

                Map<String, Object> update = new HashMap<>();
                update.put("channel_id", channel_id);
                update.put("sender_id", sender_id);
                update.put("resiver_id", resiver_id);
                update.put("message", message);
                update.put("group_id", group_id);

                addNotification(sender_id, update, "new_message");

                return databaseManager.handleMessage(channel_id, sender_id, message, now, resiver_id, group_id);
            }
        }
        return false;
    }

    public boolean editMessage(long message_id, long channel_id, long sender_id, String new_message, long group_id) {
        if (new_message.isBlank()) {
            return deleteMessage(sender_id, channel_id, message_id, sender_id, group_id);
        }

        if ((databaseManager.isSQL() || databaseManager.isMongo()) &&
                databaseManager.checkIDExists(sender_id, DatabaseManager.table_accounts)) {

            if (group_id == 0L) {

                Map<String, Object> update = new HashMap<>();
                update.put("channel_id", channel_id);
                update.put("sender_id", sender_id);
                update.put("edited_message", new_message);
                update.put("group_id", group_id);

                addNotification(sender_id, update, "edited_message");

                return databaseManager.handleEditMessage(message_id, new_message, channel_id, group_id);

            } else if (databaseManager.doesUserHavePermissionsInChannel(channel_id,
                    List.of("edit_own_message"), sender_id, group_id)) {

                Map<String, Object> update = new HashMap<>();
                update.put("channel_id", channel_id);
                update.put("sender_id", sender_id);
                update.put("edited_message", new_message);
                update.put("group_id", group_id);

                addNotification(sender_id, update, "edited_message");

                return databaseManager.handleEditMessage(message_id, new_message, channel_id, group_id);
            }
        }
        return false;
    }

    public Long getDMChannelID(long user_id, long user_id2) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(user_id);
            condition_data.add(user_id2);

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_chats,
                        "channel_id", "send_by = ? OR send_by = ?", condition_data,
                        null, "", 0).get("channel_id").get(0)));

            } catch (Exception e) {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                        new Document("user1", user_id).append("user2", user_id2), true, 0, "channel_id")
                        .get(0).get("channel_id")));

            } catch (Exception e) {
                return null;
            }

        }

        return null;
    }

    public Map<String, List<Object>> getMessages(long channel_id, long group_id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> where_values = new ArrayList<>();
            where_values.add(channel_id);
            where_values.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_chats, "*",
                    "channel_id = ? AND group_id = ?", where_values, null,
                    "send_at DESC", amount);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                    new Document("channel_id", channel_id).append("group_id", group_id), true, amount);

            Map<String, List<Object>> result = new HashMap<>();

            result.put("msgs", new ArrayList<>());
            result.put("user1", new ArrayList<>());
            result.put("user2", new ArrayList<>());
            result.put("channel_id", new ArrayList<>());

            Map<String, List<Object>> transformed = databaseManager.transformMongoToSQL(amount, mongoData, result);
            List<Map<String, Object>> messages = (List<Map<String, Object>>) transformed.get("msgs").get(0);

            messages.sort((map1, map2) -> ((LocalDateTime) map2.get("send_at")).compareTo(((LocalDateTime) map1.get("send_at"))));
            transformed.put("msgs", Collections.singletonList(messages));

            return transformed;
        }
        return null;
    }

    public boolean deleteMessage(long sender_id, long channel_id, long message_id, long actor_id, long group_id) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(channel_id);
            condition_data.add(message_id);
            condition_data.add(sender_id);

            if (group_id == 0L) {
                // dm
                if (sender_id == actor_id) {
                    return databaseManager.messageDeletionSQL(channel_id, message_id, sender_id);

                } else {
                    return false;

                }

            } else {
                Map<String, List<Object>> message_data = databaseManager.getDataSQL(DatabaseManager.table_chats,
                        "msg_id", "channel_id = ? AND msg_id = ? AND send_by = ?", condition_data,
                        null, "", 0);

                if (message_data == null || message_data.isEmpty() || message_data.get("msg_id") == null ||
                        message_data.get("msg_id").isEmpty()) {
                    return false;
                }

                if ((sender_id != actor_id &&
                        databaseManager.doesUserHavePermissionsInChannel(channel_id,
                                List.of("delete_others_message"), actor_id, group_id)) ||
                        (sender_id == actor_id &&
                                databaseManager.doesUserHavePermissionsInChannel(channel_id,
                                        List.of("delete_own_message"), actor_id, group_id))) {

                    return databaseManager.messageDeletionSQL(channel_id, message_id, sender_id);

                }
            }

        } else if (databaseManager.isMongo()) {
            Document channelId = new Document("channel_id", channel_id);
            if (group_id == 0L) {
                List<Map<String, Object>> msgs_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                        channelId, true, 0 ,"msgs");

                if (msgs_data == null || msgs_data.isEmpty()) {
                    return false;
                }

                List<Map<String, Object>> the_messages = (List<Map<String, Object>>) msgs_data.get(0).get("msgs");

                boolean toUpdate = false;
                try {
                    for (int i2 = 0; i2 < the_messages.size(); i2++) {
                        Map<String, Object> msg = the_messages.get(i2);
                        if (Long.valueOf(String.valueOf(msg.get("send_by"))) == sender_id &&
                                Long.valueOf(String.valueOf(msg.get("msg_id"))) == message_id &&
                                actor_id == sender_id) {
                            the_messages.remove(i2);
                            toUpdate = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }

                if (toUpdate) {
                    return databaseManager.messageDeletionMongo(channel_id, the_messages, message_id);

                } else {
                    return false;
                }


            } else if ((sender_id != actor_id &&
                    databaseManager.doesUserHavePermissionsInChannel(channel_id,
                            List.of("delete_others_message"), actor_id, group_id)) ||
                    (sender_id == actor_id &&
                            databaseManager.doesUserHavePermissionsInChannel(channel_id,
                                    List.of("delete_own_message"), actor_id, group_id))) {

                List<Map<String, Object>> msgs_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                        channelId, true, 0, "msgs");

                if (msgs_data == null || msgs_data.isEmpty()) {
                    return false;
                }

                List<Map<String, Object>> the_messages = (List<Map<String, Object>>) msgs_data.get(0).get("msgs");

                try {
                    for (int i2 = 0; i2 < the_messages.size(); i2++) {
                        Map<String, Object> msg = the_messages.get(i2);
                        if (Long.valueOf(String.valueOf(msg.get("send_by"))) == sender_id &&
                                Long.valueOf(String.valueOf(msg.get("msg_id"))) == message_id) {
                            the_messages.remove(i2);
                            break;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }

                return databaseManager.messageDeletionMongo(channel_id, the_messages, message_id);
            }

        }
        return false;
    }

    public boolean addReaction(long channel_id, long message_id, long post_id, String reaction, long actor_id,
                               long group_id) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            if (group_id == 0L) {
                // dm
                return databaseManager.handleReactionsSQL(channel_id, message_id, post_id, reaction, actor_id);

            } else if (databaseManager.doesUserHavePermissionsInChannel(channel_id, List.of("react"),
                        actor_id, group_id)) {

                return databaseManager.handleReactionsSQL(channel_id, message_id, post_id, reaction, actor_id);
            }

        } else if (databaseManager.isMongo() &&
                (group_id == 0 || databaseManager.doesUserHavePermissionsInChannel(channel_id, List.of("react"),
                        actor_id, group_id))) {

            return databaseManager.handleReactionsMongo(channel_id, message_id, post_id, reaction, actor_id);
        }

        return false;

    }


    public Map<String, Object> getReactions(long channel_id, long message_id, long post_id, int amount) {
        Map<String, Object> result = new HashMap<>();
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(channel_id);
            condition.add(message_id);
            condition.add(post_id);

            Map<String, List<Object>> map = databaseManager.getDataSQL(DatabaseManager.table_reactions,
                    "*", "channel_id = ? AND msg_id = ? AND post_id = ?", condition,
                    null, "", amount);

            if (map == null) {
                return null;
            }

            for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }

            return result;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> prof = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_reactions,
                    new Document("channel_id", channel_id).append("msg_id", message_id)
                            .append("post_id", post_id), true, amount);

            for (Map.Entry<String, List<Object>> entry : databaseManager.transformMongoToSQL(amount, prof, new HashMap<>())
                    .entrySet()) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }

            return result;
        }
        return null;
    }

    public boolean removeReaction(long channel_id, long message_id, long post_id, String reaction, long actor_id) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> condition_data3 = new ArrayList<>();
            condition_data3.add(channel_id);
            condition_data3.add(reaction);
            condition_data3.add(message_id);
            condition_data3.add(post_id);
            condition_data3.add(actor_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_reactions,
                    "channel_id = ? AND reaction = ? AND msg_id = ? AND post_id = ? AND member_id = ?",
                    condition_data3);

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                    new Document("channel_id", channel_id).append("reaction", reaction)
                            .append("msg_id", message_id).append("member_id", actor_id).append("post_id", post_id));

        }
        return false;
    }


    public Long startCaptcha(String answer) {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> data = databaseManager.getDataSQL(DatabaseManager.table_captchas, "id", "",
                    null, null, "", 0);

            if (data == null) {
                return null;
            }

            long id = databaseManager.generateID(data.get("id"));

            List<Object> values = new ArrayList<>();
            values.add(id);
            values.add(answer);
            values.add(LocalDateTime.now());

            return databaseManager.addDataSQL(DatabaseManager.table_captchas,
                    "id, answer, time, last_edit_time",
                    "?, ?, "+ API.captcha_time +", ?", values) ? id : null;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_captchas,
                    null, false, 0, "id");

            if (data == null) {
                return null;
            }

            long id = databaseManager.generateID(databaseManager.extract_all_content(data, "id"));

            return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_captchas, new Document("id", id)
                            .append("answer", answer).append("time", 10).append("last_edit_time", LocalDateTime.now()),
                    null) ? id : null;

        }
        return null;
    }

    public boolean solveCaptcha(long id, String given_answer) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> captcha_data = databaseManager.getDataSQL(DatabaseManager.table_captchas,
                    "answer, time",
                    "id = ?", condition_data, null, "", 0);

            if (captcha_data == null) {
                return false;
            }

            try {
                if ((!captcha_data.get("time").isEmpty() &&
                        0 >= Short.valueOf(String.valueOf(captcha_data.get("time").get(0)))) ||
                        (String.valueOf(captcha_data.get("answer").get(0)).equals(given_answer))) {

                    //return databaseManager.deleteDataSQL(DatabaseManager.table_captchas, "id = ?", condition_data);
                    return databaseManager.editDataSQL(DatabaseManager.table_captchas,
                            "answer = ''", new ArrayList<>(), "id = ?", condition_data);

                }
            } catch (Exception e) {
                return false;
            }

        } else if (databaseManager.isMongo()) {
            Document captcha_id = new Document("id", id);
            List<Map<String, Object>> captcha_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_captchas,
                    captcha_id,true, 0, "answer", "time");

            if (captcha_data == null || captcha_data.isEmpty()) {
                return false;
            }

            Map<String, Object> data = captcha_data.get(0);
            short time;
            try {
                time = Short.parseShort(String.valueOf(data.get("time")));
            } catch (Exception e) {
                return false;
            }

            if ((time > 0) && (String.valueOf(data.get("answer")).equals(given_answer))) {
                // expired
                //databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_captchas, captcha_id);
                //return false;
                // solved!
                //return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_captchas, captcha_id);
                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_captchas,
                        captcha_id, new Document("answer", ""));

            }//else {
                // the captcha was not solved
                //databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_captchas, captcha_id);
               // return false;

            //}

        }
        return false;
    }

    public boolean checkIfSolvedCaptcha(long id) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            try {
                return String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_captchas,
                        "answer", "id = ?", condition_data, null, "", 0)
                        .get("answer").get(0)).isBlank();
            } catch (Exception e) {
                return false;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return String.valueOf(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_captchas,
                        new Document("id", id), true, 0, "answer").get(0).get("answer")).isBlank();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean updateCaptchaTime(long id) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> sess_data = databaseManager.getDataSQL(DatabaseManager.table_captchas,
                    "last_edit_time, time",
                    "id = ?", condition_data, null, "", 0);

            if (sess_data == null || sess_data.get("last_edit_time").isEmpty() || sess_data.get("time").isEmpty()) {
                return false;
            }

            String editTime = String.valueOf(sess_data.get("last_edit_time").get(0));

            if (!editTime.equals("null") && !databaseManager.isOneSecondAgo(Timestamp.valueOf(editTime))) {
                return false;
            }

            try {
                if (Short.valueOf(String.valueOf(sess_data.get("time").get(0))) <= 0) {
                    // time expire
                    return databaseManager.deleteDataSQL(DatabaseManager.table_captchas, "id = ?", condition_data);
                }
            } catch (Exception e) {
                return false;
            }

            List<Object> set_data = new ArrayList<>();
            set_data.add(LocalDateTime.now());

            return databaseManager.editDataSQL(DatabaseManager.table_captchas,
                    "last_edit_time = ?, time = time - 1", set_data,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {
            Document captcha_id = new Document("id", id);
            List<Map<String, Object>> captcha_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_captchas,
                    captcha_id,true, 0, "last_edit_time", "time");

            if (captcha_data == null || captcha_data.get(0).isEmpty()) {
                return false;
            }

            try {

                Map<String, Object> map = captcha_data.get(0);
                if (!databaseManager.isOneSecondAgo(Timestamp.valueOf(String.valueOf(map.get("last_edit_time"))))) {
                    return false;
                }

                short time = Short.valueOf(String.valueOf(map.get("time")));

                if (time <= 0) {
                    // expired
                    return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_captchas, captcha_id);

                }

                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_captchas, captcha_id,
                        new Document("last_edit_time", LocalDateTime.now()).append("time", --time));

            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }

    public void handleCaptchas() {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> captcha_data = databaseManager.getDataSQL(DatabaseManager.table_captchas,
                    "id", "",
                    null, null, "", 0);

            if (captcha_data == null) {
                return;
            }

            for (Object captchas_id : captcha_data.get("id")) {
                try {
                    updateCaptchaTime(Long.parseLong(String.valueOf(captchas_id)));
                } catch (Exception e) {
                }
            }


        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> captcha_ids = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_captchas, null,
                    false, 0,"id");

            if (captcha_ids == null) {
                return;
            }

            for (Map<String, Object> ids : captcha_ids) {
                try {
                    updateCaptchaTime(Long.parseLong(String.valueOf(ids.get("id"))));
                } catch (Exception e) {
                }
            }

        }
    }


    public boolean createPost(long sender_id, String msg, String background) {
        if (!databaseManager.checkIDExists(sender_id, DatabaseManager.table_accounts)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            // there is no custom background if empty
            List<Object> data = new ArrayList<>();
            data.add(sender_id);
            data.add(LocalDateTime.now());
            data.add(msg);
            data.add(background);

            if (databaseManager.checkIDExists(1l, DatabaseManager.table_posts)) {
                return databaseManager.addDataSQL(DatabaseManager.table_posts, "send_by, send_at, msg, background",
                        "?, ?, ?, ?", data);
            } else {
                databaseManager.addDataSQL(DatabaseManager.table_posts, "send_by, send_at, msg, background",
                        "?, ?, ?, ?", data);

                return addCommentToPost(sender_id, 1l, "Welcome to the comment section", "");

            }

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> post_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_posts,
                    null, false, 0, "id");

            if (post_data == null) {
                return false;
            }

            if (databaseManager.checkIDExists(1l, DatabaseManager.table_posts)) {

                return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_posts,
                        new Document("id", databaseManager.MongoGenerateID(post_data, "id"))
                                .append("send_by", sender_id)
                                .append("msg", msg).append("send_at", LocalDateTime.now())
                                .append("background", background).append("comments", Arrays.asList()), null);
            } else {
                databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_posts,
                        new Document("id", databaseManager.MongoGenerateID(post_data, "id"))
                                .append("send_by", sender_id)
                                .append("msg", msg).append("send_at", LocalDateTime.now())
                                .append("background", background).append("comments", Arrays.asList()), null);

                return addCommentToPost(sender_id, 1l, "Welcome to the comment section", "");
            }

        }
        return false;
    }

    public Map<String, List<Object>> getLatestPosts(int amount) {
        if (databaseManager.isSQL()) {
            Map<String, List<Object>> posts = databaseManager.getDataSQL(DatabaseManager.table_posts,
                    "*", "", null, null, "send_at DESC", amount);

            return posts == null || posts.isEmpty() ? null : posts;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_posts,
                    null, false, amount);
            Map<String, List<Object>> result = new HashMap<>();

            result.put("id", new ArrayList<>());
            result.put("send_by", new ArrayList<>());
            result.put("msg", new ArrayList<>());
            result.put("send_at", new ArrayList<>());
            result.put("background", new ArrayList<>());
            result.put("comments", new ArrayList<>());

            return databaseManager.transformMongoToSQL(amount, mongoData, result);

        }
        return null;
    }

    public boolean deletePost(long sender_id, long post_id) {
        if (databaseManager.isSQL()) {
            List<Object> idk = new ArrayList<>();
            idk.add(post_id);

            databaseManager.deleteDataSQL(DatabaseManager.table_reactions, "channel_id = 0 AND post_id = ?", idk);
            databaseManager.deleteDataSQL(DatabaseManager.table_post_comments, "post_id = ?", idk);

            idk.add(sender_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_posts,
                    "id = ? AND send_by = ?", idk);

        } else if (databaseManager.isMongo() &&
                databaseManager.checkIDExists(sender_id, DatabaseManager.table_accounts) &&
                databaseManager.checkIDExists(post_id, DatabaseManager.table_posts)) {

            databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                            new Document("post_id", post_id));

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id).append("send_by", sender_id));

        }
        return false;
    }

    public boolean editPost(long sender_id, long post_id,
                            String edited_msg, String given_background) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            List<Object> set_data = new ArrayList<>();
            set_data.add(edited_msg);
            set_data.add(given_background);

            return databaseManager.editDataSQL(DatabaseManager.table_posts, "msg = ?, background = ?",
                    set_data,
                    "id = ? AND send_by = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id)
                            .append("send_by", sender_id), new Document("msg", edited_msg)
                            .append("background", given_background));

        }
        return false;
    }


    public boolean addCommentToPost(long sender_id, long post_id, String message, String reply_to) {
        if (!databaseManager.checkIDExists(sender_id, DatabaseManager.table_accounts) ||
                !databaseManager.checkIDExists(post_id, DatabaseManager.table_posts) ||
                reply_to.contains(","+post_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> addData = new ArrayList<>();
            addData.add(post_id);
            addData.add(sender_id);
            addData.add(now);
            addData.add(message);
            addData.add(reply_to);

            return databaseManager.addDataSQL(DatabaseManager.table_post_comments,
                    "post_id, send_by, send_at, msg, repl_to", "?, ?, ?, ?, ?", addData);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (collection == null) {
                return false;
            }

            Map<String, Object> comment = new HashMap<>();
            comment.put("post_id", post_id);
            comment.put("send_by", sender_id);
            comment.put("send_at", now);
            comment.put("msg", message);
            comment.put("msg_id", databaseManager.generateID(databaseManager.extract_all_content(collection,
                    "msg_id")));
            comment.put("reply_to", reply_to);

            collection.add(comment);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", collection));
        }
        return false;
    }

    public Map<String, List<Object>> getCommentsOnPost(long post_id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);

            return databaseManager.getDataSQL(DatabaseManager.table_post_comments, "*",
                    "post_id = ?", condition_data, null, "send_at DESC", amount);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> result = new HashMap<>();
            result.put("post_id", new ArrayList<>());
            result.put("send_by", new ArrayList<>());
            result.put("send_at", new ArrayList<>());
            result.put("msg", new ArrayList<>());
            result.put("msg_id", new ArrayList<>());
            result.put("reply_to", new ArrayList<>());

            return databaseManager.transformMongoToSQL(amount, mongoData, result);


        }
        return null;
    }

    public Long getCommentID(long post_id, long sender_id, String message, String repl) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(post_id);
            condition.add(sender_id);
            condition.add(message);
            condition.add(repl);

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_post_comments,
                        "msg_id", "post_id = ? AND send_by = ? AND msg = ? AND repl_to = ?",
                                condition, null, "", 0)
                        .get("msg_id").get(0)));
            } catch (Exception e) {
                return null;
            }


        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                        "comments", new Document("id", post_id)).stream()
                        .filter(map ->
                                ((long) map.get("send_by")) == sender_id &&
                                        ((String) map.get("msg")).equals(message) &&
                                        ((String) map.get("reply_to")).equals(repl))
                        .map(map -> map.get("msg_id"))
                        .findFirst()
                        .orElse(null)));

            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean deleteCommentFromPost(long post_id, long sender_id, long message_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);


            Map<String, List<Object>> comment_repl = databaseManager.getDataSQL(DatabaseManager.table_post_comments,
                    "repl_to, msg_id", "post_id = ?", condition_data, null, "", 0);

            if (comment_repl == null) {
                return false;
            }


            List<Long> msg_ids_repl = new ArrayList<>();
            List<Object> replies_list = comment_repl.get("repl_to");
            List<Object> message_ids = comment_repl.get("msg_id");
            for (int i = 0; i < replies_list.size(); i++) {
                if (String.valueOf(replies_list.get(i)).contains(String.valueOf(message_id))) {
                    msg_ids_repl.add(Long.parseLong(String.valueOf(message_ids.get(i))));
                }
            }

            if (!msg_ids_repl.isEmpty()) {
                // this comment have replies
                try {
                    List<Object> idk_cond = new ArrayList<>();
                    for (Long repl_comment_id : msg_ids_repl) {
                        idk_cond.clear();
                        idk_cond.add(post_id);
                        idk_cond.add(repl_comment_id);

                        databaseManager.deleteDataSQL(DatabaseManager.table_reactions,
                                "post_id = ? AND msg_id = ?", idk_cond);

                        databaseManager.deleteDataSQL(DatabaseManager.table_post_comments,
                                "post_id = ? AND msg_id = ?", idk_cond);
                    }
                } catch (Exception e) {
                    return false;
                }
            }

            condition_data.add(message_id);

            databaseManager.deleteDataSQL(DatabaseManager.table_reactions, "post_id = ? AND msg_id = ?",
                    condition_data);

            condition_data.add(sender_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_post_comments,
                    "post_id = ? AND msg_id = ? AND send_by = ?", condition_data);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> comment_data = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (comment_data == null) {
                return false;
            }

            try {
                for (int i = 0; i < comment_data.size(); i++) {

                    Map<String, Object> map = comment_data.get(i);
                    if (Long.valueOf(String.valueOf(map.get("msg_id"))) == message_id) {

                        databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                                new Document("channel_id", 0l)
                                        .append("msg_id", message_id)
                                        .append("post_id", post_id));

                        comment_data.remove(i);
                        //List<String> repl_comm_id = Arrays.stream(String.valueOf(map.get("reply_to")).split(",")).toList();
                        for (int i2 = 0; i2 < comment_data.size(); i2++) {

                            if (String.valueOf(comment_data.get(i2).get("reply_to")).contains(","+message_id)) {
                                databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                                        new Document("channel_id", 0l)
                                                .append("msg_id", Long.parseLong(String.valueOf(comment_data.get(i2)
                                                        .get("msg_id"))))
                                                .append("post_id", post_id));

                                comment_data.remove(i2);
                            }
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", comment_data));

        }
        return false;
    }

    public boolean updateCommentMessage(long post_id, long sender_id, long message_id, String new_message) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);
            condition_data.add(message_id);

            List<Object> set_data = new ArrayList<>();
            set_data.add(new_message);

            return databaseManager.editDataSQL(DatabaseManager.table_post_comments,
                    "msg = ?", set_data,
                    "post_id = ? AND send_by = ? AND msg_id = ?", condition_data);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> comment_data = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (comment_data == null) {
                return false;
            }

            try {
                comment_data = databaseManager.MongoUpdateValueInCollection(comment_data, "msg_id", message_id,
                        "msg", new_message, false);
            } catch (Exception e) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", comment_data));

        }
        return false;
    }


    public boolean updateProfilePfp(long id, String given_pfp) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_pfp);

            return databaseManager.editDataSQL(DatabaseManager.table_profiles,
                    "pfp = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("pfp", given_pfp));

        }
        return false;
    }

    public boolean updateProfileBanner(long id, String given_banner) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_banner);

            return databaseManager.editDataSQL(DatabaseManager.table_profiles,
                    "banner = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("banner", given_banner));

        }
        return false;
    }

    public boolean updateProfileBadges(long id, String given_badges) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_badges);

            addNotification(id, Collections.singletonMap("new_badges", given_badges), "new_badges");

            return databaseManager.editDataSQL(DatabaseManager.table_profiles, "badges = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            addNotification(id, Collections.singletonMap("new_badges", given_badges), "new_badges");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("badges", given_badges));

        }
        return false;
    }

    public boolean updateProfileAnimations(long id, String given_animations) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_animations);

            return databaseManager.editDataSQL(DatabaseManager.table_profiles,
                    "animations = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("animations", given_animations));

        }
        return false;
    }

    public boolean updateProfileAboutMe(long id, String given_about_me) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_about_me);

            return databaseManager.editDataSQL(DatabaseManager.table_profiles,
                    "about_me = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("about_me", given_about_me));

        }
        return false;
    }

    public boolean updateProfileStats(long id, String given_stats) {
        // can be jwt token
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_stats);

            return databaseManager.editDataSQL(DatabaseManager.table_profiles,
                    "stats = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_profiles,
                    new Document("id", id),
                    new Document("stats", given_stats));

        }
        return false;
    }

    public Map<String, Object> getProfile(long id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(id);

            Map<String, List<Object>> map = databaseManager.getDataSQL(DatabaseManager.table_profiles,
                    "*", "id = ?", condition, null, "", 0);
            if (map == null) {
                return null;
            }

            Map<String, Object> result = new HashMap<>();

            for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }

            return result;

        } else if (databaseManager.isMongo()) {
            try {
                return databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_profiles,
                        new Document("id", id), true, 0).get(0);
            } catch (Exception e) {
                return  null;
            }
        }
        return null;
    }






    public Map<String, List<Object>> getAllGroupsWithUser(long user_id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(user_id);

            Map<String, List<Object>> groups = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "group_id", "member_id = ?", condition_data,
                    null, "", amount);

            if (groups == null || !groups.containsKey("group_id")) {
                return null;
            }

            groups.put("id", groups.get("group_id"));
            return groups;

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    null, false, amount);

            if (mongoData == null) {
                return null;
            }

            List<Map<String, Object>> groups = new ArrayList<>();
            for (Map<String, Object> group : mongoData) {
                List<Map<String, Object>> all_members = (List<Map<String, Object>>) group.get("members");
                for (Map<String, Object> member : all_members) {
                    if (Long.parseLong(String.valueOf(member.get("member_id"))) == user_id) {
                        groups.add(group);
                        break;
                    }
                }
            }

            //result.put("roles", new ArrayList<>());
            //result.put("members", new ArrayList<>());
            //result.put("logs", new ArrayList<>());
            //result.put("channels", new ArrayList<>());
            //result.put("categories", new ArrayList<>());
            //result.put("settings", new ArrayList<>());
            Map<String, List<Object>> result = new HashMap<>();
            result.put("owner_id", new ArrayList<>());
            result.put("group_events", new ArrayList<>());
            result.put("name", new ArrayList<>());
            result.put("id", new ArrayList<>());
            result.put("logo", new ArrayList<>());
            result.put("banner", new ArrayList<>());
            result.put("animations", new ArrayList<>());
            result.put("created_at", new ArrayList<>());

            return databaseManager.transformMongoToSQL(amount, groups, result);

        }

        return null;
    }

    public Long getLatestCreatedGroupByOwner(long id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(id);

            try {
                return Long.parseLong(String.valueOf(databaseManager.getDataSQL(DatabaseManager.table_groups, "id", "owner_id = ?",
                        condition, null, "created_at DESC", 1).get("id").get(0)));

            } catch (Exception e) {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            try {
                return Long.parseLong(String.valueOf(databaseManager.MongoReadCollectionNoSQL(
                        DatabaseManager.table_groups, new Document("owner_id", id), false, 0, "id")
                        .stream()
                        .sorted((m1, m2) -> ((LocalDateTime) m2.get("created_at")).compareTo((LocalDateTime) m1.get("created_at")))
                                .findFirst()
                                .orElse(null)));

            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean createGroup(long owner_id, String name, String logo, String banner, String animations) {
        if (!databaseManager.checkIDExists(owner_id, DatabaseManager.table_accounts)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> group_data = new ArrayList<>();
            group_data.add(name);
            group_data.add(owner_id);
            group_data.add(logo);
            group_data.add(banner);
            group_data.add(animations);
            group_data.add(now);

            if (!databaseManager.addDataSQL(DatabaseManager.table_groups,
                    "name, owner_id, logo, banner, animations, settings, created_at, group_events",
                    "?, ?, ?, ?, ?, '', ?, ''",
                    group_data)) {
                return false;
            }

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(owner_id);

            Map<String, List<Object>> group_id = databaseManager.getDataSQL(DatabaseManager.table_groups,
                    "id", "owner_id = ?", condition_data, null,
                    "created_at DESC", 1);

            if (group_id == null || group_id.isEmpty() || group_id.get("id") == null ||
                    group_id.get("id").isEmpty()) {
                return false;
            }

            List<Object> values = new ArrayList<>();
            values.add(Long.parseLong(String.valueOf(group_id.get("id").get(0))));
            values.add(owner_id);

            return databaseManager.addDataSQL(DatabaseManager.table_group_members,
                    "group_id, member_id, roles_id, nickname", "?, ?, '', NULL", values);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> all_groups_id = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    null, false, 0, "id");

            if (all_groups_id == null) {
                return false;
            }

            return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", databaseManager.MongoGenerateID(all_groups_id, "id"))
                            .append("name", name).append("owner_id", owner_id)
                            .append("logo", logo).append("banner", banner)
                            .append("animations", animations)
                            .append("settings", "").append("members",
                                    Arrays.asList(new Document("member_id", owner_id)
                                            .append("roles_id", "").append("nickname", null)))
                            .append("group_events", "")
                            .append("roles", Arrays.asList()).append("categories", Arrays.asList())
                            .append("channels", Arrays.asList()).append("created_at", now), null);

        }
        return false;
    }

    public boolean deleteGroup(long group_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> all_channels = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "channel_id", "group_id = ?", condition_data, null, "", 0);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            List<Object> all_channel_ids = all_channels.get("channel_id");
            if (!all_channel_ids.isEmpty()) {
                for (Object channel : all_channel_ids) {
                    long channel_id = Long.parseLong(String.valueOf(channel));
                    List<Object> condition_data2 = new ArrayList<>();
                    condition_data2.add(channel_id);

                    Map<String, List<Object>> messages = databaseManager.getDataSQL(DatabaseManager.table_chats,
                            "msg_id, send_by", "channel_id = ?", condition_data2, null, "", 0);

                    if (messages == null || messages.get("msg_id").isEmpty() || messages.get("send_by").isEmpty()) {
                        return false;
                    }

                    try {
                        for (int i = 0; i < messages.get("msg_id").size(); i++) {
                            databaseManager.messageDeletionSQL(channel_id,
                                    Long.parseLong(String.valueOf(messages.get("msg_id").get(i))),
                                    Long.parseLong(String.valueOf(messages.get("send_by").get(i))));
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    condition_data2.add(group_id);

                    databaseManager.deleteDataSQL(DatabaseManager.table_group_channels,
                            "channel_id = ? AND group_id = ?",
                            condition_data2);
                }
            }

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_logs,
                    "group_id = ?", condition_data)) {
                return false;
            }


            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_category,
                    "group_id = ?", condition_data)) {
                return false;
            }

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_roles,
                    "group_id = ?", condition_data)) {
                return false;
            }

            Map<String, List<Object>> members = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "member_id", "group_id = ?", condition_data, null, "", 0);

            Map<String, List<Object>> group_info = databaseManager.getDataSQL(DatabaseManager.table_groups,
                    "*", "id = ?", condition_data, null, "", 0);

            if (members == null || group_info == null || group_info.get("id").isEmpty()) {
                return false;
            }

            try {
                for (Object mem_id : members.get("member_id")) {
                    addNotification(Long.parseLong(String.valueOf(mem_id)), group_info, "deleted_group");
                }
            } catch (Exception e) {
                return false;
            }

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_members,
                    "group_id = ?", condition_data)) {
                return false;
            }

            return databaseManager.deleteDataSQL(DatabaseManager.table_groups,
                    "id = ?", condition_data);

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null) {
                return false;
            }

            if (!all_channels.isEmpty()) {
                for (Object channel : databaseManager.extract_all_content(all_channels, "channel_id")) {
                    long channel_id = Long.parseLong(String.valueOf(channel));

                    databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                            new Document("channel_id", channel_id).append("post_id", 0l));

                    databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_chats,
                            new Document("channel_id", channel_id));
                }
            }

            List<Map<String, Object>> all_members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            List<Map<String, Object>> group_info = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    filter, true, 0, "owner_id", "name", "id", "logo", "banner", "created_at", "animations");

            if (all_members == null || group_info == null) {
                return false;
            }

            try {
                for (Map<String, Object> mem : all_members) {
                    addNotification(Long.parseLong(String.valueOf(mem.get("member_id"))), group_info, "deleted_group");
                }
            } catch (Exception e) {
                return false;
            }

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_groups,
                    filter);

        }
        return false;
    }

    public boolean updateGroupSettings(long actor_id, long group_id, String new_settings, String log_message) {
        // can be jwt token
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_group_settings"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_settings);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "settings = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Settings");

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("settings", new_settings)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Settings");
        }
        return false;
    }

    public boolean updateGroupName(long actor_id, long group_id, String new_name, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_group_name"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_name);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "name = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Name");

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("name", new_name)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Name");
        }
        return false;
    }

    public boolean updateGroupOwner(long new_owner_id, long group_id, long actor_id, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.checkIDExists(new_owner_id, DatabaseManager.table_accounts) ||
                !databaseManager.checkOwner(actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> check_data = new ArrayList<>();
            check_data.add(new_owner_id);
            check_data.add(group_id);

            Map<String, List<Object>> owner_data = databaseManager.getDataSQL(DatabaseManager.table_group_members, "member_id",
                    "member_id = ? AND group_id = ?", check_data, null, "", 0);

            if (owner_data == null || owner_data.get("member_id").isEmpty()) {
                return false;
            }

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_owner_id);

            addNotification(actor_id, Collections.singletonMap("new_owner", new_owner_id), "new_owner");

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "owner_id = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Changed Owner");

        } else if (databaseManager.isMongo()) {
            Document group_filter = new Document("id", group_id);
            List<Map<String, Object>> owner_data = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", group_filter);

            if (owner_data == null || databaseManager.checkNotUniqueWithStream(owner_data, "member_id", new_owner_id)) {
                return false;
            }

            addNotification(actor_id, Collections.singletonMap("new_owner", new_owner_id), "new_owner");

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    group_filter, new Document("owner_id", new_owner_id)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Changed Owner");

        }
        return false;
    }

    public boolean updateGroupLogo(String new_logo, long group_id, long actor_id, String log_message) {
        // can be jwt token
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_group_logo"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_logo);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "logo = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Logo");

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("logo", new_logo)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Logo");

        }
        return false;
    }

    public boolean updateGroupBanner(String new_banner, long group_id, long actor_id, String log_message) {
        // can be jwt token
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_group_banner"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_banner);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "banner = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Banner");


        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("banner", new_banner)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Banner");
        }
        return false;
    }

    public boolean updateGroupEvents(String new_events, long group_id, long actor_id, String log_message) {
        // can be jwt token
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_group_events"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_events);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "group_events = ?",
                    updatedData, "id = ?", search_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Events");


        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("group_events", new_events)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Events");
        }
        return false;
    }

    public Map<String, List<Object>> getAllStuffFromGroupSQL(long group_id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> group_data = databaseManager.getDataSQL(DatabaseManager.table_groups,
                    "*",
                    "id = ?", condition_data, null, "", 0);

            if (group_data == null) {
                return null;
            }

            Map<String, List<Object>> members_data = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "*",
                    "group_id = ?", condition_data, null, "", amount);

            if (members_data == null) {
                return null;
            }

            Map<String, List<Object>> channels_data = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "*",
                    "group_id = ?", condition_data, null, "", amount);

            if (channels_data == null) {
                return null;
            }

            Map<String, List<Object>> category_data = databaseManager.getDataSQL(DatabaseManager.table_group_category,
                    "*",
                    "group_id = ?", condition_data, null, "", amount);

            if (category_data == null) {
                return null;
            }

            Map<String, List<Object>> roles_data = databaseManager.getDataSQL(DatabaseManager.table_group_roles,
                    "*",
                    "group_id = ?", condition_data, null, "", amount);

            if (roles_data == null) {
                return null;
            }

            Map<String, List<Object>> logs_data = databaseManager.getDataSQL(DatabaseManager.table_group_logs,
                    "*",
                    "group_id = ?", condition_data, null, "", amount);

            if (logs_data == null) {
                return null;
            }

            for (Map.Entry<String, List<Object>> mem : members_data.entrySet()) {
                group_data.put("member_" + mem.getKey(), mem.getValue());
            }

            for (Map.Entry<String, List<Object>> ch : channels_data.entrySet()) {
                group_data.put("channel_" + ch.getKey(), ch.getValue());
            }

            for (Map.Entry<String, List<Object>> cate : category_data.entrySet()) {
                group_data.put("category_" + cate.getKey(), cate.getValue());
            }

            for (Map.Entry<String, List<Object>> role : roles_data.entrySet()) {
                group_data.put("role_" + role.getKey(), role.getValue());
            }

            for (Map.Entry<String, List<Object>> log : logs_data.entrySet()) {
                group_data.put("log_" + log.getKey(), log.getValue());
            }

            return group_data.isEmpty() ? null : group_data;

        }
        return null;
    }


    public boolean addGroupMember(long member_id, long group_id, String roles_id, String nickname, String log_message) {
        if (!databaseManager.checkIDExists(member_id, DatabaseManager.table_accounts) ||
                !databaseManager.checkIDExists(group_id, DatabaseManager.table_groups) ||
                !databaseManager.checkGroupAllows(databaseManager.calculateGroupSettings(group_id),
                        List.of("join_group"))) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> member_data = new ArrayList<>();
            member_data.add(group_id);
            member_data.add(member_id);
            member_data.add(roles_id);
            member_data.add(nickname);

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> all_members = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "member_id", "group_id = ?", condition_data, null, "", 0);

            if (all_members == null || !all_members.containsKey("member_id")) {
                return false;
            }

            try {
                for (Object mem_id : all_members.get("member_id")) {
                    if (Long.parseLong(String.valueOf(mem_id)) == member_id) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return databaseManager.addDataSQL(DatabaseManager.table_group_members,
                    "group_id, member_id, roles_id, nickname", "?, ?, ?, ?", member_data) ?
                    databaseManager.updateGroupLogs(member_id, group_id, log_message, now, "Joined The Group") : false;

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            if (all_members == null) {
                return false;
            }

            try {
                for (Map<String, Object> mem : all_members) {
                    if (Long.parseLong(String.valueOf(mem.get("member_id"))) == member_id) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            Map<String, Object> member_data = new HashMap<>();
            member_data.put("member_id", member_id);
            member_data.put("roles_id", roles_id);
            member_data.put("nickname", nickname);

            all_members.add(member_data);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("members", all_members)) &&
                    databaseManager.updateGroupLogs(member_id, group_id, log_message, now, "Joined The Group");
        }
        return false;
    }

    public Map<String, List<Object>> getGroupMembers(long group_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_group_members, "*",
                    "group_id = ?", condition, null, "", 0);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", new Document("id", group_id));

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("member_id", new ArrayList<>());
            res.put("roles_id", new ArrayList<>());
            res.put("nickname", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, mongoData, res);
        }
        return null;
    }

    public boolean removeGroupMember(long member_id, long group_id, String leave_type, String log_message,
                                     long actor_id) {
        LocalDateTime now = LocalDateTime.now();
        final String determent_needed_permissions = leave_type.equals("ban") ? "ban" :
                (leave_type.equals("kick") ? "kick" : "");

        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> all_members = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "member_id", "group_id = ?", condition_data, null, "", 0);

            if (all_members == null) {
                return false;
            }

            if (all_members.get("member_id").isEmpty() && leave_type.equals("leave")
                    && actor_id == 0L) {
                // the group is empty | delete the group
                return deleteGroup(group_id);

            } else {

                boolean inThere = false;
                try {
                    for (Object mem_id : all_members.get("member_id")) {
                        if (Long.parseLong(String.valueOf(mem_id)) == member_id) {
                            inThere = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }

                if (!inThere) {
                    return false;
                }


                if ((determent_needed_permissions.isBlank() && leave_type.equals("leave")) ||
                        (databaseManager.doesUserHavePermissions(List.of(determent_needed_permissions),
                                actor_id, group_id))) {

                    Map<String, List<Object>> group_info = databaseManager.getDataSQL(DatabaseManager.table_groups,
                            "*", "id = ?", condition_data, null, "", 0);

                    if (group_info == null) {
                        return false;
                    }

                    Map<String, Object> notification = new HashMap<>();
                    notification.put("leave_type", leave_type);
                    notification.put("group_info", group_info);

                    addNotification(member_id, notification, "leave");

                    condition_data.add(member_id);

                    return databaseManager.deleteDataSQL(DatabaseManager.table_group_members,
                            "group_id = ? AND member_id = ?", condition_data) ?
                            databaseManager.updateGroupLogs(member_id, group_id, log_message, now, leave_type) : false;

                }
            }

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            if (all_members == null) {
                return false;
            }

            try {
                if (databaseManager.checkNotUniqueWithStream(all_members, "member_id", member_id)) {
                    return false;
                }

                if (all_members.isEmpty() && actor_id == 0L &&
                                leave_type.equals("leave")) {
                    // last member left | delete the group
                    return deleteGroup(group_id);

                } else {
                    if ((determent_needed_permissions.isBlank() && leave_type.equals("leave")) ||
                            (databaseManager.doesUserHavePermissions(List.of(determent_needed_permissions),
                                    actor_id, group_id))) {

                        List<Map<String, Object>> group_info = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                                filter, true, 0, "owner_id", "name", "id", "logo", "banner", "created_at", "animations");

                        if (group_info == null) {
                            return false;
                        }

                        addNotification(member_id, group_info, "leave");

                        return databaseManager.handleMemberLeaveGroupMongo(member_id, group_id, leave_type,
                                log_message, now, filter, all_members);

                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    public boolean updateMemberRoles(long member_id, long group_id, long role_id, long actor_id, boolean toAdd,
                                     String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.checkRoleExists(role_id, group_id) ||
                !databaseManager.doesUserHavePermissions(List.of("update_member_roles"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        String role_value = "," + role_id;
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(role_value);

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(member_id);

            return toAdd ? (databaseManager.editDataSQL(DatabaseManager.table_group_members,
                    databaseManager.postgressql_connection != null ? "roles_id = roles_id || ?" :
                            "roles_id = CONCAT(roles_id, ?)", set_data,
                    "group_id = ? AND member_id = ?", condition_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Added Roles From Member"))
                    :
                    (databaseManager.editDataSQL(databaseManager.table_group_members,
                            "roles_id = REPLACE(roles_id, ?, '')", set_data,
                            "group_id = ? AND member_id = ?", condition_data) &&
                            databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                                    "Removed Roles From Member"));


        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            if (members == null) {
                return false;
            }

            for (Map<String, Object> mem : members) {
                if (Long.valueOf(String.valueOf(mem.get("member_id"))) == member_id) {
                    if (toAdd) {
                        mem.put("roles_id", String.valueOf(mem.get("roles_id")) + role_value);
                    } else {
                        mem.put("roles_id", String.valueOf(mem.get("roles_id")).replace(role_value, ""));
                    }
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("members", members)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            toAdd ? "Added Roles From Member" : "Removed Roles From Member");


        }
        return false;

    }

    public boolean updateMemberNickname(long member_id, long group_id, long actor_id, String new_nickname,
                                        String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_member_nickname"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_nickname);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(member_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_members, "nickname = ?",
                    nickname_data, "group_id = ? AND member_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Nickname");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            if (members == null) {
                return false;
            }

            members = databaseManager.MongoUpdateValueInCollection(members, "member_id",
                    member_id, "nickname", new_nickname, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("members", members)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Nickname");


        }
        return false;
    }








    public Long createGroupChannel(long group_id, long actor_id, String channel_type, String name,
                                   String permissions, String log_message, String categories_id) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("create_channel"), actor_id, group_id)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> channels_data = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "channel_id", "group_id = ?", condition_data,
                    null, "", 0);

            if (channels_data == null || channels_data.isEmpty() || channels_data.get("channel_id") == null) {
                return null;
            }

            List<Object> add_data = new ArrayList<>();
            long id = databaseManager.generateID(channels_data.get("channel_id"));
            add_data.add(group_id);
            add_data.add(id);
            add_data.add(name);
            add_data.add(permissions);
            add_data.add(channel_type);
            add_data.add(categories_id);

            if (databaseManager.addDataSQL(DatabaseManager.table_group_channels,
                    "group_id, channel_id, name, permissions, channel_type, categories_id",
                    "?, ?, ?, ?, ?, ?", add_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + channel_type + " Channel")) {

                return id;

            } else {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null) {
                return null;
            }

            Map<String, Object> channel = new HashMap<>();
            long id = databaseManager.generateID(databaseManager.extract_all_content(all_channels,
                    "channel_id"));

            channel.put("channel_id", id);
            channel.put("name", name);
            channel.put("permissions", permissions);
            channel.put("channel_type", channel_type);
            channel.put("categories_id", categories_id);

            all_channels.add(channel);


            if (databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + channel_type + " Channel")) {

                return id;

            } else {
                return null;
            }

        }
        return null;
    }

    public Map<String, List<Object>> getGroupChannelMessages(long channel_id, long group_id, int amount) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(channel_id);
            condition.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_chats,
                    "*", "channel_id = ? AND group_id = ?", condition, null,
                    "", amount);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                    new Document("channel_id", channel_id).append("group_id", group_id), false, amount);

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("channel_id", new ArrayList<>());
            res.put("group_id", new ArrayList<>());
            res.put("user1", new ArrayList<>());
            res.put("user2", new ArrayList<>());
            res.put("msgs", new ArrayList<>());

            return databaseManager.transformMongoToSQL(amount, mongoData, res);

        }
        return null;
    }

    public Map<String, List<Object>> getGroupChannels(long group_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_group_channels, "*",
                    "group_id = ?", condition, null, "", 0);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", new Document("id", group_id));

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("channel_id", new ArrayList<>());
            res.put("name", new ArrayList<>());
            res.put("permissions", new ArrayList<>());
            res.put("channel_type", new ArrayList<>());
            res.put("categories_id", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, mongoData, res);
        }
        return null;
    }

    public boolean deleteGroupChannel(long channel_id, long group_id, long actor_id, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("delete_channel"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(channel_id);

            Map<String, List<Object>> messages = databaseManager.getDataSQL(DatabaseManager.table_chats,
                    "msg_id, send_by", "channel_id = ?", condition_data, null, "", 0);

            if (messages == null || messages.get("msg_id").isEmpty() || messages.get("send_by").isEmpty()) {
                return false;
            }

            try {
                for (int i = 0; i < messages.get("msg_id").size(); i++) {
                    databaseManager.messageDeletionSQL(channel_id,
                            Long.parseLong(String.valueOf(messages.get("msg_id").get(i))),
                            Long.parseLong(String.valueOf(messages.get("send_by").get(i))));
                }
            } catch (Exception e) {
                return false;
            }

            condition_data.add(group_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_channels,
                    "channel_id = ? AND group_id = ?",
                    condition_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Channel");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            all_channels = databaseManager.MongoUpdateValueInCollection(all_channels,
                    "channel_id", channel_id, null, null, true);

            databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                    new Document("channel_id", channel_id).append("post_id", 0l));

            databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_chats,
                    new Document("channel_id", channel_id));

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Deleted Channel");


        }
        return false;
    }

    public boolean updateGroupChannelName(long channel_id, long group_id, long actor_id, String new_name,
                                          String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_channel_name"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "name = ?",
                    nickname_data, "group_id = ? AND channel_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Name");

        } else if (databaseManager.isMongo()) {
            Document fiter = new Document("id", group_id);

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", fiter);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            all_channels = databaseManager.MongoUpdateValueInCollection(all_channels, "channel_id",
                    channel_id, "name", new_name, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    fiter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Name");
        }

        return false;

    }

    public boolean updateGroupChannelPermissions(long channel_id, long group_id, long actor_id, String new_permissions,
                                                 String log_message) {
        // new_permissions IS JWT TOKEN
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_channel_permissions"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_permissions);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "permissions = ?",
                    nickname_data, "group_id = ? AND channel_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Permissions");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null) {
                return false;
            }

            all_channels = databaseManager.MongoUpdateValueInCollection(all_channels,
                    "channel_id", channel_id, "permissions", new_permissions, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Permissions");
        }

        return false;

    }

    public boolean updateGroupChannelType(long channel_id, long group_id, long actor_id, String new_channel_type,
                                          String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_channel_type"), actor_id, group_id)) {
            return false;
        }


        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_channel_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "channel_type = ?",
                    nickname_data, "group_id = ? AND channel_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Type");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            all_channels = databaseManager.MongoUpdateValueInCollection(all_channels,
                    "channel_id", channel_id, "channel_type", new_channel_type, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Type");
        }

        return false;
    }

    public boolean updateGroupChannelCategory(long channel_id, long group_id, long actor_id, String new_categories_id,
                                              String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_channel_category"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_categories_id);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "categories_id = ?",
                    set_data, "group_id = ? AND channel_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Category");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            all_channels = databaseManager.MongoUpdateValueInCollection(all_channels, "channel_id",
                    channel_id, "categories_id", new_categories_id, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Channel Category");
        }

        return false;
    }


    public Long createGroupRole(long actor_id, long group_id, String name, String permissions, String role_type,
                                String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("create_role"), actor_id, group_id)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> roles_data = databaseManager.getDataSQL(DatabaseManager.table_group_roles,
                    "role_id", "group_id = ?", condition_data, null, "", 0);

            if (roles_data == null || roles_data.isEmpty() || roles_data.get("role_id") == null) {
                return null;
            }

            long id = databaseManager.generateID(roles_data.get("role_id"));

            condition_data.add(id);
            condition_data.add(name);
            condition_data.add(permissions);
            condition_data.add(role_type);

            if (databaseManager.addDataSQL(DatabaseManager.table_group_roles,
                    "group_id, role_id, name, permissions, role_type",
                    "?, ?, ?, ?, ?", condition_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + role_type + " Role")) {

                return id;

            } else {
                return null;
            }

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> roles_id = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", filter);

            if (roles_id == null) {
                return null;
            }

            long id = databaseManager.generateID(databaseManager.extract_all_content(roles_id, "role_id"));

            Map<String, Object> New_role = new HashMap<>();
            New_role.put("role_id", id);
            New_role.put("name", name);
            New_role.put("permissions", permissions);
            New_role.put("role_type", role_type);
            roles_id.add(New_role);

            if (databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("roles", roles_id), filter) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + role_type + " Role")) {
                return id;

            } else {
                return null;
            }

        }
        return null;
    }

    public Map<String, List<Object>> getGroupRoles(long group_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_group_roles, "*",
                    "group_id = ?", condition, null, "", 0);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", new Document("id", group_id));

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("role_id", new ArrayList<>());
            res.put("name", new ArrayList<>());
            res.put("permissions", new ArrayList<>());
            res.put("role_type", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, mongoData, res);
        }
        return null;
    }

    public boolean deleteGroupRole(long role_id, long group_id, long actor_id, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("delete_role"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> members = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "roles_id, member_id", "group_id = ?", condition_data, null, "", 0);

            if (members == null || members.get("roles_id").isEmpty() || members.get("member_id").isEmpty()) {
                return false;
            }

            List<Object> set_data = new ArrayList<>();

            try {
                for (int i = 0; i < members.get("roles_id").size(); i++) {
                    String mem_role_id = String.valueOf(members.get("roles_id").get(i));
                    long mem_id = Long.parseLong(String.valueOf(members.get("member_id").get(i)));
                    if (mem_role_id.contains("," + role_id)) {
                        condition_data.clear();
                        condition_data.add(group_id);
                        condition_data.add(mem_id);

                        set_data.clear();
                        set_data.add(mem_role_id.replace("," + role_id, ""));

                        databaseManager.editDataSQL(DatabaseManager.table_group_members,
                                "roles_id = ?", set_data,
                                "group_id = ? AND member_id = ?", condition_data);
                    }
                }
            } catch (Exception e) {
                return false;
            }
            condition_data.clear();

            condition_data.add(group_id);
            condition_data.add(role_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_roles,
                    "group_id = ? AND role_id = ?", condition_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Role");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_roles = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", filter);

            if (all_roles == null || all_roles.isEmpty()) {
                return false;
            }

            List<Map<String, Object>> all_members = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "members", filter);

            if (all_members == null || all_members.isEmpty()) {
                return false;
            }

            all_roles = databaseManager.MongoUpdateValueInCollection(all_roles,
                    "role_id", role_id, null, null, true);

            try {
                for (int i = 0; i < all_members.size(); i++) {

                    Map<String, Object> map = all_members.get(i);
                    Object Member_roles = map.get("roles_id");
                    if (String.valueOf(Member_roles).contains(","+role_id)) {
                        map.put("roles_id", String.valueOf(Member_roles).replace(","+role_id, ""));
                        break;
                    }
                }

            } catch (Exception e) {
                return false;
            }

            databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("roles", all_roles));

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("members", all_members)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                    "Deleted Role");

        }
        return false;
    }

    public boolean updateGroupRoleName(long role_id, long group_id, long actor_id, String new_name,
                                       String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_role_name"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "name = ?",
                    set_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Role Name");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_roles = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", filter);

            if (all_roles == null || all_roles.isEmpty()) {
                return false;
            }

            all_roles = databaseManager.MongoUpdateValueInCollection(all_roles, "role_id",
                    role_id, "name", new_name, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("roles", all_roles)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Uodated Role Name");
        }

        return false;

    }

    public boolean updateGroupRolePermissions(long role_id, long group_id, long actor_id, String new_permissions,
                                              String log_message) {
        // new_permissions IS JWT TOKEN
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_role_permissions"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_permissions);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "permissions = ?",
                    set_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Role Permissions");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_roles = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", filter);

            if (all_roles == null || all_roles.isEmpty()) {
                return false;
            }

            all_roles = databaseManager.MongoUpdateValueInCollection(all_roles, "role_id",
                    role_id, "permissions", new_permissions, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("roles", all_roles)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Uodated Role Permissions");
        }

        return false;

    }

    public boolean updateGroupRoleType(long role_id, long group_id, long actor_id, String new_role_type,
                                       String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_role_type"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_role_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "role_type = ?",
                    set_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Role Type");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> all_roles = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "roles", filter);

            if (all_roles == null || all_roles.isEmpty()) {
                return false;
            }

            all_roles = databaseManager.MongoUpdateValueInCollection(all_roles, "role_id",
                    role_id, "type", new_role_type, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("roles", all_roles)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Uodated Role Type");
        }

        return false;

    }


    public boolean createGroupCategory(long actor_id, long group_id, String name,
                                       String category_type, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("create_category"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> add_data = new ArrayList<>();
            add_data.add(group_id);
            add_data.add(name);
            add_data.add(category_type);

            return databaseManager.addDataSQL(DatabaseManager.table_group_category,
                    "group_id, name, category_type",
                    "?, ?, ?", add_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + category_type + " Category");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            long id = databaseManager.generateID(databaseManager.extract_all_content(collection,
                    "category_id"));

            Map<String, Object> category = new HashMap<>();
            category.put("category_id", id);
            category.put("name", name);
            category.put("category_type", category_type);
            collection.add(category);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Created " + category_type + " Category");

        }
        return false;
    }

    public Map<String, List<Object>> getGroupCategories(long group_id) {
        if (databaseManager.isSQL()) {
            List<Object> condition = new ArrayList<>();
            condition.add(group_id);

            return databaseManager.getDataSQL(DatabaseManager.table_group_category, "*",
                    "group_id = ?", condition, null, "", 0);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", new Document("id", group_id));

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("category_id", new ArrayList<>());
            res.put("name", new ArrayList<>());
            res.put("category_type", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, mongoData, res);
        }
        return null;
    }

    public boolean deleteGroupCategory(long actor_id, long group_id, long category_id, String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("delete_category"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> channels_id = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "channel_id, categories_id", "group_id = ?", condition_data,
                    null, "", 0);

            if (channels_id == null || channels_id.get("channel_id").isEmpty() || channels_id.get("categories_id").isEmpty()) {
                return false;
            }

            List<Object> set_data = new ArrayList<>();

            try {
                for (int i = 0; i < channels_id.get("categories_id").size(); i++) {
                    String chan_catego_id = String.valueOf(channels_id.get("categories_id").get(i));
                    long chan_id = Long.parseLong(String.valueOf(channels_id.get("channel_id").get(i)));
                    if (chan_catego_id.contains("," + category_id)) {
                        condition_data.clear();
                        condition_data.add(group_id);
                        condition_data.add(chan_id);

                        set_data.clear();
                        set_data.add(chan_catego_id.replace("," + category_id, ""));

                        databaseManager.editDataSQL(DatabaseManager.table_group_channels,
                                "categories_id = ?", set_data,
                                "group_id = ? AND channel_id = ?", condition_data);
                    }
                }
            } catch (Exception e) {
                return false;
            }

            condition_data.clear();
            condition_data.add(group_id);
            condition_data.add(category_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_category,
                    "group_id = ? AND category_id = ?", condition_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Deleted Category");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", filter);

            if (all_channels == null || all_channels.isEmpty()) {
                return false;
            }

            try {
                for (int i = 0; i < all_channels.size(); i++) {

                    Map<String, Object> map = all_channels.get(i);
                    String Channel_categories_id = String.valueOf(map.get("categories_id"));
                    if (Channel_categories_id.contains(","+category_id)) {
                        map.put("categories_id", Channel_categories_id.replace(","+category_id, ""));
                        break;
                    }
                }

            } catch (Exception e) {
                return false;
            }

            collection = databaseManager.MongoUpdateValueInCollection(collection,
                    "category_id", category_id, null, null, true);

            databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("channels", all_channels));

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Deleted Category");

        }
        return false;
    }


    public boolean updateGroupCategoryName(long category_id, long group_id, long actor_id, String new_name,
                                           String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_category_name"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_category, "name = ?",
                    set_data, "group_id = ? AND category_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Category Name");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            collection = databaseManager.MongoUpdateValueInCollection(collection,
                    "category_id", category_id, "name", new_name, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Category Name");
        }

        return false;

    }


    public boolean updateGroupCategoryType(long category_id, long group_id, long actor_id, String new_category_type,
                                           String log_message) {
        if (!databaseManager.checkIDExists(actor_id, DatabaseManager.table_accounts) ||
                !databaseManager.doesUserHavePermissions(List.of("update_category_type"), actor_id, group_id)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.isSQL()) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(new_category_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_category, "category_type = ?",
                    set_data, "group_id = ? AND category_id = ?", conditon_data) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Category Type");

        } else if (databaseManager.isMongo()) {
            Document filter = new Document("id", group_id);
            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null || collection.isEmpty()) {
                return false;
            }

            collection = databaseManager.MongoUpdateValueInCollection(collection,
                    "category_id", category_id, "category_type", new_category_type, false);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    databaseManager.updateGroupLogs(actor_id, group_id, log_message, now,
                            "Updated Category Type");
        }

        return false;

    }

    public boolean createFriendRequest(long id, long id2) {
        if (checkFriendRequest(id, id2)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> users = new ArrayList<>();
            users.add(id);
            users.add(id2);

            addNotification(id, Collections.singletonMap("new_friend_request", users), "friend_requests");

             return databaseManager.addDataSQL(DatabaseManager.table_friend_requests,
                     "id, id2", "?, ?", users);

        } else if (databaseManager.isMongo()) {
            List<Object> users = new ArrayList<>();
            users.add(id);
            users.add(id2);

            addNotification(id, Collections.singletonMap("new_friend_request", users), "friend_requests");

            return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id", id).append("id2", id2), null);
        }

        return false;
    }

    public boolean deleteFriendRequest(long id, long id2) {
        if (!checkFriendRequest(id, id2)) {
            return false;
        }

        if (databaseManager.isSQL()) {
            List<Object> users = new ArrayList<>();
            users.add(id);
            users.add(id2);
            users.add(id2);
            users.add(id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_friend_requests,
                    "(id = ? AND id2 = ?) OR (id = ? AND id2 = ?)", users);

        } else if (databaseManager.isMongo()) {
            boolean sen1 =  databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id", id).append("id2", id2));

            boolean sen2 =  databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id2", id).append("id", id2));

            boolean sen3 =  databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id", id2).append("id2", id));

            return sen1 || sen2 || sen3;
        }

        return false;
    }


    public boolean checkFriendRequest(long id, long id2) {
        if (!databaseManager.checkIDExists(id, DatabaseManager.table_accounts) ||
                !databaseManager.checkIDExists(id2, DatabaseManager.table_accounts)) {
            return false;
        }


        if (databaseManager.isSQL()) {
            List<Object> users = new ArrayList<>();
            users.add(id);
            users.add(id2);
            users.add(id2);
            users.add(id);

            Map<String, List<Object>> request_data = databaseManager.getDataSQL(DatabaseManager.table_friend_requests,
                    "id", "(id = ? AND id2 = ?) OR (id2 = ? AND id = ?)", users, null, "", 0);

            return request_data != null && !request_data.get("id").isEmpty();

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> request_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id", id).append("id2", id2), true, 0);

            List<Map<String, Object>> request_data2 = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id2", id).append("id", id2), true, 0);

            List<Map<String, Object>> request_data3 = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_friend_requests,
                    new Document("id", id2).append("id2", id), true, 0);

            return (request_data != null || request_data2 != null || request_data3 != null) &&
                    (!request_data.isEmpty() || !request_data2.isEmpty() || !request_data.isEmpty());


        }

        return false;
    }


    public boolean addUpload(long user_id, String server, String name) {
        if (databaseManager.isSQL()) {
            List<Object> stuff = new ArrayList<>();
            stuff.add(user_id);
            stuff.add(server);
            stuff.add(name);

            return databaseManager.addDataSQL(DatabaseManager.table_uploads, "user_id, server, name", "?, ?, ?", stuff);

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_uploads,
                    new Document("user_id", user_id).append("server", server).append("name", name), null);
        }

        return false;
    }


    public boolean deleteUpload(long user_id, String server, String name) {
        if (databaseManager.isSQL()) {
            List<Object> stuff = new ArrayList<>();
            stuff.add(user_id);
            stuff.add(server);
            stuff.add(name);

            return databaseManager.deleteDataSQL(DatabaseManager.table_uploads,
                    "user_id=? AND server=? AND name=?",  stuff);

        } else if (databaseManager.isMongo()) {
            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_uploads,
                    new Document("user_id", user_id).append("server", server).append("name", name));
        }

        return false;
    }


    public Map<String, List<Object>> getUploads(long user_id) {
        if (databaseManager.isSQL()) {
            List<Object> search = new ArrayList<>();
            search.add(user_id);

            return databaseManager.getDataSQL(DatabaseManager.table_uploads,
                    "*", "user_id=?", search, null, "", 0);

        } else if (databaseManager.isMongo()) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_uploads,
                    new Document("user_id", user_id), false, 0);

            if (mongoData == null || mongoData.isEmpty()) {
                return null;
            }

            Map<String, List<Object>> res = new HashMap<>();
            res.put("user_id", new ArrayList<>());
            res.put("server", new ArrayList<>());
            res.put("name", new ArrayList<>());

            return databaseManager.transformMongoToSQL(0, mongoData, res);

        }

        return null;
    }
}
