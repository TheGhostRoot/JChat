package jcorechat.app_api.database;

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


    public Long createUser(String name, String email, String password, String encryption_key, String sign_key) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_details = new ArrayList<>();
            account_details.add(name);
            account_details.add(email);
            account_details.add(password);
            account_details.add(encryption_key);
            account_details.add(sign_key);
            account_details.add(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));

            if (!databaseManager.addDataSQL(databaseManager.table_accounts,
                        "name, email, password, encryption_key, sign_key, session_id, session_expire, last_edit_time, created_at",
                        "?, ?, ?, ?, ?, NULL, NULL, NULL, ?", account_details)) {
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
            long id = (long) account_data.get("id").get(0);
            profile_details.add(id);

            if (!databaseManager.addDataSQL(databaseManager.table_profiles, "id, pfp, banner, pets, coins, badges, animations",
                    "?, 'Default Pic', 'Default Banner', NULL, 0, 'No badges', NULL", profile_details)) {

                databaseManager.deleteDataSQL(databaseManager.table_accounts, "id = ?", profile_details);
                return null;
            }

            return id;

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> accounts_id_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_accounts, null,
                    false, "id", "name", "email");

            if (accounts_id_data == null) {
                return null;
            }

            List<Object> all_ids = new ArrayList<>();

            for (Map<String, Object> map : accounts_id_data) {
                if (map.get("name").equals(name) || map.get("email").equals(email) ||
                        map.get("encryption_key").equals(encryption_key) || map.get("sign_key").equals(sign_key)) {
                    return null;
                }
                if (map.containsKey("id")) {
                    all_ids.add(map.get("id"));
                }
            }

            long account_ID = databaseManager.generateID(all_ids);

            if (!databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_accounts, new Document("id", account_ID).append("name", name)
                    .append("email", email).append("password", password)
                    .append("encryption_key", encryption_key).append("sign_key", sign_key)
                    .append("session_id", null).append("session_expire", null).append("last_edit_time", null)
                    .append("created_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    , null)) {

                return null;
            }

            if (!databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_profiles, new Document("id", account_ID)
                    .append("pfp", "Default Pic").append("banner", "Default Banner")
                    .append("pets", null).append("coins", 0).append("badges", "No badges")
                            .append("animations", null), null)) {


                databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_accounts, new Document("id", account_ID));
                return null;
            }


            return account_ID;

        }
        return null;
    }

    public boolean changeUserEmail(long id, String new_email) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_email);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "email = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("email", new_email));

        }
        return false;
    }

    public boolean changeUserPassword(long id, String new_password) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_password);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "password = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("password", new_password));

        }
        return false;
    }

    public boolean changeUserEncryptionKey(long id, String new_encryptino_key) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_encryptino_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "encryption_key = ?", account_set,
                    "id = ?", account_where);
        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("encryption_key", new_encryptino_key));

        }
        return false;
    }

    public boolean changeUserSignKey(long id, String new_sign_key) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_sign_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "sign_key = ?", account_set,
                    "id = ?", account_where);
        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("sign_key", new_sign_key));

        }
        return false;
    }

    public boolean changeUserSessionID(long id, long session_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(session_id);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "session_id = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("session_id", session_id));

        }
        return false;
    }

    public boolean updateUserSessionExpire(long id)  {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            Map<String, List<Object>> sess_data = databaseManager.getDataSQL(databaseManager.table_accounts, "last_edit_time, session_expire",
                    "id = ?", account_where, null, "", 0);

            if (sess_data == null || (sess_data.get("last_edit_time").isEmpty() ||
                    Objects.equals(String.valueOf(sess_data.get("last_edit_time").get(0)), "null")) ||
                    (sess_data.get("session_expire").isEmpty() ||
                    Objects.equals(String.valueOf(sess_data.get("session_expire").get(0)), "null")) ||
                    !databaseManager.isOneSecondAgo(Timestamp.valueOf(String.valueOf(sess_data.get("last_edit_time").get(0))))) {

                return false;
            }

            if (((short) sess_data.get("session_expire").get(0)) <= 0) {
                // session expired. END IT
                return databaseManager.editDataSQL(databaseManager.table_accounts,
                        "session_expire = NULL, session_id = NULL, last_edit_time = NULL",
                        null, "id = ?", account_where);
            }

            List<Object> account_set = new ArrayList<>();
            account_set.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "last_edit_time = ?, session_expire = session_expire - 1", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            Document filter = new Document("id", id);

            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_accounts, filter, true,
                    "last_edit_time", "session_expire");

            if (account_data == null || account_data.get(0).get("last_edit_time") == null ||
                    account_data.get(0).get("session_expire") == null ||
                    !databaseManager.isOneSecondAgo(Timestamp.valueOf(String.valueOf(account_data.get(0).get("last_edit_time"))))) {

                return false;
            }

            Map<String, Object> data = account_data.get(0);

            short sessionExpire = Short.valueOf(String.valueOf(data.get("session_expire")));

            if (sessionExpire <= 0) {
                // remove the session
                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, filter,
                        new Document("session_expire", null).append("last_edit_time", null)
                                .append("session_id", null));
            }

            sessionExpire--;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, filter,
                    new Document("session_expire", sessionExpire)
                            .append("last_edit_time",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        }
        return false;
    }

    public boolean addUserFriend(long id, long friend_id, String current_friends) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + friend_id);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    databaseManager.postgressql_connection != null ? "friends = friends || ?" :
                            "friends = CONCAT(friends, ?)", account_friends,"id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> accounts_ids = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", friend_id), true, "name");

            if (accounts_ids == null || accounts_ids.isEmpty() || accounts_ids.get(0).isEmpty()) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", current_friends+","+friend_id));

        }
        return false;
    }

    public boolean removeUserFriend(long id, long friend_id, String current_friends) {
        String friend_id_str = "," + friend_id;
        if (!current_friends.contains(friend_id_str)) {
            return false;
        }

        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(friend_id_str);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "friends = REPLACE(friends, ?, '')", account_friends,"id = ?",
                    account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", current_friends.replace(friend_id_str, "")));

        }
        return false;
    }

    public boolean addUserGroup(long id, long group_id, String current_groups) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + group_id);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    databaseManager.postgressql_connection != null ? "chat_groups_ = chat_groups_ || ?" :
                            "chat_groups_ = CONCAT(chat_groups_, ?)", account_friends,"id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> accounts_ids = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "name");

            if (accounts_ids == null || accounts_ids.isEmpty() || accounts_ids.get(0).isEmpty()) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("chat_groups_", current_groups+","+group_id));

        }
        return false;
    }

    public boolean removeUserGroup(long id, long group_id, String current_groups) {
        String group_id_str = "," + group_id;
        if (!current_groups.contains(group_id_str)) {
            return false;
        }

        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(group_id_str);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "chat_groups_ = REPLACE(chat_groups_, ?, '')", account_friends,
                    "id = ?", account_where);
        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("chat_groups_", current_groups.replace(group_id_str, "")));

        }
        return false;
    }

    public void handleSessions() {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            Map<String, List<Object>> account_data = databaseManager.getDataSQL(databaseManager.table_accounts,
                    "id", "",
                    null, null, "", 0);

            if (account_data == null) {
                return;
            }

            for (Object account_id : account_data.get("id")) {
                try {
                    updateUserSessionExpire(Long.parseLong(String.valueOf(account_id)));
                } catch (Exception e) {}
            }


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_accounts,
                    null,  false,"");

            if (account_data == null) {
                return;
            }

            for (Map<String, Object> ids : account_data) {
                try {
                    updateUserSessionExpire(Long.parseLong(String.valueOf(ids.get("id"))));
                } catch (Exception e) {}
            }

        }
    }





    public boolean addMessage(long channel_id, long sender_id, String message) {
        if (message.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> edit_condition_data = new ArrayList<>();
            edit_condition_data.add(channel_id);

            Map<String, List<Object>> current_chat_data = databaseManager.getDataSQL(databaseManager.table_chats,
                    "msg, msg_id",
                    "channel_id = ?",
                    edit_condition_data, null, "send_at DESC", 0);

            if (current_chat_data == null) {
                return false;
            }

            List<Object> chat_data = new ArrayList<>();
            chat_data.add(channel_id);
            chat_data.add(message);
            chat_data.add(now.truncatedTo(ChronoUnit.MINUTES));
            chat_data.add(sender_id);
            chat_data.add(databaseManager.generateID(current_chat_data.get("msg_id")));

            List<Object> set_data = new ArrayList<>();
            set_data.add(message);

            edit_condition_data.add(sender_id);

            if (current_chat_data.get("msg").isEmpty()) {
                if (Long.valueOf(String.valueOf(current_chat_data.get("sent_by").get(0))) == sender_id) {

                    edit_condition_data.add(Long.parseLong(String.valueOf(current_chat_data.get("msg_id").get(0))));

                    if (databaseManager.editDataSQL(DatabaseManager.table_chats,
                            databaseManager.postgressql_connection != null ? "msg = msg || ?" :
                            "msg = CONCAT(msg, ?)", set_data, "channel_id = ? AND sent_by = ? AND msg_id = ?",
                            edit_condition_data)) {

                        return true;
                    }

                }

                return databaseManager.addDataSQL(databaseManager.table_chats,
                            "channel_id, msg, send_at, sent_by, msg_id", "?, ?, ?, ?, ?", chat_data);

            } else {
                return true;
            }
        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            Document convId = new Document("channel_id", channel_id);
            List<Map<String, Object>> chat_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_chats,
                    convId, false, "msg", "msg_id", "sent_by", "send_at");

            if (chat_data == null) {
                return false;
            }

            Document msg = new Document("msg", message);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String send_at = now.format(formatter);

            List<Object> all_ids = new ArrayList<>();
            for (Map<String, Object> map : chat_data) {
                all_ids.add(map.get("msg_id"));
            }

            long new_msg_id = databaseManager.generateID(all_ids);
            if (chat_data.isEmpty() || chat_data.get(0).isEmpty()) {

                return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_chats,
                        msg.append("channel_id", channel_id)
                        .append("sent_by", sender_id).append("msg_id", new_msg_id)
                        .append("send_at", send_at), null);

            } else {

                LocalDateTime mostRecentDate = null;
                Long resent_msg_id = null;
                String current_message = "";
                Long message_sender_id = null;

                for (Map<String, Object> map : chat_data) {
                    LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(map.get("send_at")), formatter);
                    if ((mostRecentDate == null && resent_msg_id == null) || (dateTime.isBefore(mostRecentDate))) {
                        mostRecentDate = dateTime;
                        resent_msg_id = Long.valueOf(String.valueOf(map.get("msg_id")));
                        current_message = String.valueOf(map.get("msg"));
                        message_sender_id = Long.valueOf(String.valueOf(map.get("send_by")));
                    }
                }

                if (message_sender_id == null || message_sender_id != sender_id) {

                    return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_chats,
                            msg.append("channel_id", channel_id)
                                    .append("sent_by", sender_id).append("msg_id", new_msg_id)
                                    .append("send_at", send_at), null);

                } else {
                    convId.append("msg_id", resent_msg_id);
                    convId.append("send_by", sender_id);

                    return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_chats, convId,
                            new Document("msg", current_message+message));

                }


            }

        }
        return false;
    }

    public Map<String, List<Object>> getMessages(long channel_id, int amount) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> where_values = new ArrayList<>();
            where_values.add(channel_id);

            return databaseManager.getDataSQL(databaseManager.table_chats, "*",
                    "channel_id = ?", where_values, null,
                    "send_at DESC", amount);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_chats,
                    new Document("channel_id", channel_id), false);
            Map<String, List<Object>> result = new HashMap<>();

            result.put("msg", new ArrayList<>());
            result.put("msg_id", new ArrayList<>());
            result.put("send_at", new ArrayList<>());
            result.put("sent_by", new ArrayList<>());
            result.put("channel_id", new ArrayList<>());

            for (Map<String, Object> map : mongoData) {
                for (Map.Entry<String, Object> data : map.entrySet()) {
                    result.get(data.getKey()).add(data.getValue());
                }
            }

            return result;

        }
        return null;
    }

    public boolean deleteMessage(long sender_id, long channel_id, long message_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(channel_id);
            condition_data.add(message_id);
            condition_data.add(sender_id);

            return databaseManager.deleteDataSQL(databaseManager.table_chats,
                    "channel_id = ? AND msg_id = ? AND sent_by = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_chats,
                    new Document("channel_id", channel_id)
                    .append("msg_id", message_id).append("sent_by", sender_id));

        }
        return false;
    }




    public Long startCaptcha(String answer) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            Map<String, List<Object>> data = databaseManager.getDataSQL(databaseManager.table_captchas, "id", "",
                    null, null, "", 0);

            if (data == null) {
                return null;
            }

            long id = databaseManager.generateID(data.get("id"));

            List<Object> values = new ArrayList<>();
            values.add(id);
            values.add(answer);
            values.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return databaseManager.addDataSQL(databaseManager.table_captchas,"id, answer, time, last_edit_time, failed",
                    "?, ?, 10, ?, 0", values) ? id : null;

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_captchas, null, false,"id");

            if (data == null) {
                return null;
            }

            List<Object> all_captchas = new ArrayList<>();
            for (Map<String, Object> map : data) {
                all_captchas.addAll(map.values());
            }

            long id = databaseManager.generateID(all_captchas);

            return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_captchas, new Document("id", id)
                    .append("answer", answer).append("time", 10).append("last_edit_time", LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("failed", 0),
                    null) ? id : null;

        }
        return null;
    }

    public boolean verifyCaptcha(long id, String given_answer) {
        // TODO when captcha is ready then update this

        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> captcha_data = databaseManager.getDataSQL(databaseManager.table_captchas, "answer, time, failed",
                    "id = ?", condition_data, null, "", 0);

            if (captcha_data == null) {
                return false;
            }

            if (captcha_data.get("answer").equals(given_answer)) {
                // solved!
                return databaseManager.deleteDataSQL(databaseManager.table_captchas, "id = ?", condition_data);

            } else if ((3 <= (short) captcha_data.get("failed").get(0)) ||
                    (0 >= (short) captcha_data.get("time").get(0))) {
                // extended fails or time

                databaseManager.deleteDataSQL(databaseManager.table_captchas, "id = ?", condition_data);

                return false;
            } else {
                // the captcha was not solved and the user has more time and didn't failed 3 times

                if (!(captcha_data.get("time").isEmpty() || captcha_data.get("failed").isEmpty())) {

                    databaseManager.editDataSQL(databaseManager.table_captchas, "failed = failed + 1",
                            null, "id = ?",
                            condition_data);

                }
                return false;
            }
        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            Document captcha_id = new Document("id", id);
            List<Map<String, Object>> captcha_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_captchas, captcha_id,
                    true, "answer", "time", "failed");

            if (captcha_data == null || captcha_data.isEmpty() || captcha_data.get(0).isEmpty()) {
                return false;
            }

            Map<String, Object> data = captcha_data.get(0);
            short time = Short.valueOf(String.valueOf(data.get("time")));

            if (String.valueOf(data.get("answer")).equals(given_answer)) {
                // solved!
                return databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_captchas, captcha_id);

            } else if ((time <= 0) || (Short.valueOf(String.valueOf(data.get("failed"))) >= 3)) {
                // failed!
                databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_captchas, captcha_id);

                return false;

            } else {
                // the captcha was not solved and the user has more time and didn't failed 3 times
                time--;
                databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_captchas,
                        captcha_id, new Document("time", time));

                return false;

            }

        }
        return false;
    }

    public boolean updateCaptchaTime(long id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> sess_data = databaseManager.getDataSQL(databaseManager.table_captchas, "last_edit_time, time",
                    "id = ?", condition_data, null, "", 0);

            if (sess_data == null || sess_data.get("last_edit_time").isEmpty() || sess_data.get("time").isEmpty()) {
                return false;
            }

            String editTime = String.valueOf(sess_data.get("last_edit_time").get(0));

            if (editTime != "null" && !databaseManager.isOneSecondAgo(Timestamp.valueOf(editTime))) {
                return false;
            }

            if (((short) sess_data.get("time").get(0)) <= 0) {
                // time expire
                return databaseManager.deleteDataSQL(databaseManager.table_captchas, "id = ?", condition_data);
            }

            List<Object> set_data = new ArrayList<>();
            set_data.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return databaseManager.editDataSQL(databaseManager.table_captchas, "last_edit_time = ?, time = time - 1", set_data,
                    "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            Document captcha_id = new Document("id", id);
            List<Map<String, Object>> captcha_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_captchas, captcha_id,
                    true,"last_edit_time", "time");

            if (captcha_data == null || captcha_data.get(0).isEmpty()) {
                return false;
            }

            Map<String, Object> map = captcha_data.get(0);
            if (!databaseManager.isOneSecondAgo(Timestamp.valueOf(String.valueOf(map.get("last_edit_time"))))) {
                return false;
            }
            short time = Short.valueOf(String.valueOf(map.get("time")));

            if (time <= 0) {
                // expired
                return databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_captchas, captcha_id);

            }

            time--;
            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_captchas, captcha_id,
                    new Document("last_edit_time",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .append("time", time));

        }
        return false;
    }

    public void handleCaptchas() {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            Map<String, List<Object>> captcha_data = databaseManager.getDataSQL(databaseManager.table_captchas, "id", "",
                    null, null, "", 0);

            if (captcha_data == null) {
                return;
            }

            for (Object captchas_id : captcha_data.get("id")) {
                try {
                    updateCaptchaTime(Long.parseLong(String.valueOf(captchas_id)));
                } catch (Exception e) {}
            }


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> captcha_ids = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_captchas, null,
                    false, "id");

            if (captcha_ids == null) {
                return;
            }

            for (Map<String, Object> ids : captcha_ids) {
                try {
                    updateCaptchaTime(Long.parseLong(String.valueOf(ids.get("id"))));
                } catch (Exception e) {}
            }

        }
    }





    public boolean createPost(long sender_id, String msg, String background) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            // there is no custom background if empty
            List<Object> data = new ArrayList<>();
            data.add(sender_id);
            data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            data.add(msg);
            data.add(background);

            return databaseManager.addDataSQL(databaseManager.table_posts, "sender_id, send_at, msg, background",
                    "?, ?, ?, ?", data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> post_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_posts, null,
                    false,"id");
            if (post_data == null) {
                return false;
            }

            long id = databaseManager.MongoGenerateID(post_data);

            return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_posts, new Document("id", id).append("sender_id", sender_id)
                    .append("msg", msg).append("send_at",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append("background", background), null);

        }
        return false;
    }

    public boolean deletePost(long sender_id, long post_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            return databaseManager.deleteDataSQL(databaseManager.table_posts, "id = ? AND sender_id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(databaseManager.table_posts, new Document("id", post_id)
                    .append("sender_id", sender_id));

        }
        return false;
    }

    public boolean editPost(long sender_id, long post_id,
                            String edited_msg, String given_background) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            List<Object> set_data = new ArrayList<>();
            set_data.add(edited_msg);
            set_data.add(given_background);

            return databaseManager.editDataSQL(databaseManager.table_posts, "msg = ?, background = ?", set_data,
                    "id = ? AND sender_id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_posts, new Document("id", post_id)
                    .append("sender_id", sender_id), new Document("msg", edited_msg)
                            .append("background", given_background));

        }
        return false;
    }




    public boolean updateProfilePfp(long id, String given_pfp) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_pfp);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "pfp = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles,
                    new Document("id", id),
                    new Document("pfp", given_pfp));

        }
        return false;
    }

    public boolean updateProfileBanner(long id, String given_banner) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_banner);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "banner = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles, new Document("id", id),
                    new Document("banner", given_banner));

        }
        return false;
    }

    public boolean updateProfilePets(long id, String given_pets) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_pets);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "pets = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles, new Document("id", id),
                    new Document("pets", given_pets));

        }
        return false;
    }

    public boolean updateProfileCoins(long id, int given_coins) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_coins);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "coins = ?", profile_data, "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles, new Document("id", id),
                    new Document("coins", given_coins));

        }
        return false;
    }

    public boolean updateProfileBadges(long id, String given_badges) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_badges);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "badges = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles, new Document("id", id),
                    new Document("badges", given_badges));

        }
        return false;
    }

    public boolean updateProfileAnimations(long id, String given_animations) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_animations);

            return databaseManager.editDataSQL(databaseManager.table_profiles, "animations = ?", profile_data,
                    "id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_profiles, new Document("id", id),
                    new Document("animations", given_animations));

        }
        return false;
    }




}
