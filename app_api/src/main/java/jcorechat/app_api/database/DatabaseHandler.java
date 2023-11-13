package jcorechat.app_api.database;

import jcorechat.app_api.API;
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
                    "name, email, password, encryption_key, sign_key, session_id, session_expire, last_edit_time, created_at, friends, chat_groups_,  starts_sub, ends_sub, bookmarks",
                    "?, ?, ?, ?, ?, NULL, NULL, NULL, ?, '', '', NULL, NULL, ''", account_details)) {
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

            if (!databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", account_ID).append("name", name)
                            .append("email", email).append("password", password)
                            .append("encryption_key", encryption_key).append("sign_key", sign_key)
                            .append("session_id", null).append("session_expire", null).append("last_edit_time", null)
                            .append("created_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                            .append("starts_sub", null).append("ends_sub", null).append("bookmarks", "")
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

            return databaseManager.editDataSQL(DatabaseManager.table_accounts, "email = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> all_emails = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, "email");

            if (all_emails == null || all_emails.stream().anyMatch(map -> String.valueOf(map.get("email")).equals(new_email))) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("email", new_email));

        }
        return false;
    }

    public boolean changeUserName(long id, String new_name) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_name);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "name = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> all_names = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, "name");

            if (all_names == null || all_names.stream().anyMatch(map -> String.valueOf(map.get("name")).equals(new_name))) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("name", new_name));

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

    public boolean changeUserStartsSub(long id, LocalDateTime new_starts) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_starts);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "starts_sub = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("starts_sub", new_starts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        }
        return false;
    }

    public boolean changeUserEndsSub(long id, LocalDateTime new_stops) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_stops);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "ends_sub = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("ends_sub", new_stops));

        }
        return false;
    }

    public boolean changeUserBookmarks(long id, String new_bookmarks) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_bookmarks);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts, "bookmarks = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("bookmarks", new_bookmarks));

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
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, "encryption_key");

            if (all_keys == null || all_keys.stream().anyMatch(map ->
                    String.valueOf(map.get("encryption_key")).equals(new_encryptino_key))) {
                return false;
            }

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
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, "sign_key");

            if (all_keys == null || all_keys.stream().anyMatch(map ->
                    String.valueOf(map.get("sign_key")).equals(new_sign_key))) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts, new Document("id", id),
                    new Document("sign_key", new_sign_key));

        }
        return false;
    }

    public boolean changeUserSessionID(long id, long session_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(session_id);
            account_set.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "session_id = ?, session_expire = 3, last_edit_time = ?", account_set,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> all_keys = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    null, false, "session_id");

            if (all_keys == null || all_keys.stream().anyMatch(map ->
                    Long.valueOf(String.valueOf(map.get("sign_key"))) == session_id)) {
                return false;
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("session_id", session_id).append("session_expire", 3)
                            .append("last_edit_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        }
        return false;
    }

    public boolean updateUserSessionExpire(long id) {
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

            if ((Short.valueOf(String.valueOf(sess_data.get("session_expire").get(0)))) <= 0) {
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
            if (sqlIfAccountExists(friend_id, DatabaseManager.table_accounts)) return false;

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + friend_id);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    databaseManager.postgressql_connection != null ? "friends = friends || ?" :
                            "friends = CONCAT(friends, ?)", account_friends, "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", friend_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", current_friends + "," + friend_id));

        }
        return false;
    }

    public boolean removeUserFriend(long id, long friend_id, String current_friends) {
        String friend_id_str = "," + friend_id;
        if (!current_friends.contains(friend_id_str)) {
            return false;
        }

        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(friend_id, databaseManager.table_accounts)) return false;

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(friend_id_str);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "friends = REPLACE(friends, ?, '')", account_friends, "id = ?",
                    account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", friend_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("friends", current_friends.replace(friend_id_str, "")));

        }
        return false;
    }

    public boolean addUserGroup(long id, long group_id, String current_groups) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(group_id, databaseManager.table_groups)) return false;

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + group_id);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    databaseManager.postgressql_connection != null ? "chat_groups_ = chat_groups_ || ?" :
                            "chat_groups_ = CONCAT(chat_groups_, ?)", account_friends, "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(databaseManager.table_accounts,
                    new Document("id", id),
                    new Document("chat_groups_", current_groups + "," + group_id));

        }
        return false;
    }

    public boolean removeUserGroup(long id, long group_id, String current_groups) {
        String group_id_str = "," + group_id;
        if (!current_groups.contains(group_id_str)) {
            return false;
        }

        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(group_id, databaseManager.table_groups)) return false;

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add(group_id_str);

            return databaseManager.editDataSQL(databaseManager.table_accounts,
                    "chat_groups_ = REPLACE(chat_groups_, ?, '')", account_friends,
                    "id = ?", account_where);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "name"))) return false;

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
                } catch (Exception e) {
                }
            }


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_accounts,
                    null, false, "");

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


    public boolean addMessage(long channel_id, long sender_id, long resiver_id, String message) {
        if (message.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(sender_id, databaseManager.table_accounts)) return false;

            List<Object> edit_condition_data = new ArrayList<>();
            edit_condition_data.add(channel_id);

            Map<String, List<Object>> current_chat_data = databaseManager.getDataSQL(databaseManager.table_chats,
                    "msg, msg_id, send_by",
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

            if (!current_chat_data.get("msg").isEmpty()) {
                try {
                    if (Long.valueOf(String.valueOf(current_chat_data.get("send_by").get(0))) == sender_id) {

                        edit_condition_data.add(Long.parseLong(String.valueOf(current_chat_data.get("msg_id").get(0))));

                        if (databaseManager.editDataSQL(DatabaseManager.table_chats,
                                databaseManager.postgressql_connection != null ? "msg = msg || ?" :
                                        "msg = CONCAT(msg, ?)", set_data, "channel_id = ? AND send_by = ? AND msg_id = ?",
                                edit_condition_data)) {

                            return true;
                        }

                    }

                    return databaseManager.addDataSQL(databaseManager.table_chats,
                            "channel_id, msg, send_at, send_by, msg_id", "?, ?, ?, ?, ?", chat_data);

                } catch (Exception e) {
                    return false;
                }

            } else if (channel_id == 0L) {
                // no messages in this channel

                Map<String, List<Object>> channel_ids = databaseManager.getDataSQL(DatabaseManager.table_chats,
                        "channel_id", "", null, null, "", 0);

                if (channel_ids == null) {
                    return false;
                }

                List<Object> new_chat_data = new ArrayList<>();
                new_chat_data.add(databaseManager.generateID(channel_ids.get("channel_id")));
                new_chat_data.add(message);
                new_chat_data.add(now.truncatedTo(ChronoUnit.MINUTES));
                new_chat_data.add(sender_id);
                new_chat_data.add(databaseManager.generateID(current_chat_data.get("msg_id")));

                return databaseManager.addDataSQL(DatabaseManager.table_chats,
                        "channel_id, msg, send_at, send_by, msg_id", "?, ?, ?, ?, ?", new_chat_data);

            } else {
                return false;
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", sender_id), true, "name"))) return false;


            Document convId = new Document("channel_id", channel_id);

            List<Map<String, Object>> chat_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_chats,
                    convId, true, "msgs", "channel_id");

            if (chat_data == null) {
                return false;
            }


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String send_at = now.format(formatter);

            List<Object> all_ids = new ArrayList<>();
            List<Object> all_channel_ids = Arrays.asList(chat_data.get(0).get("channel_id"));
            List<Map<String, Object>> chat_msgs = (List<Map<String, Object>>) chat_data.get(0).get("msgs");
            for (Map<String, Object> all_msg : chat_msgs) {
                all_ids.add(all_msg.get("msg_id"));
            }

            long new_msg_id = databaseManager.generateID(all_ids);
            Document msg = new Document("msg", message);

            if (chat_data.isEmpty() || chat_data.get(0).isEmpty()) {

                return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_chats,
                        new Document("channel_id", channel_id == 0L ?
                                databaseManager.generateID(all_channel_ids) : channel_id)
                                .append("user1", sender_id)
                                .append("user2", resiver_id)
                                .append("msgs",
                                        Arrays.asList(msg.append("send_by", sender_id)
                                                .append("send_at", send_at)
                                                .append("msg_id", new_msg_id))), null);

            } else {

                LocalDateTime mostRecentDate = null;
                Long resent_msg_id = null;
                String current_message = "";
                Long message_sender_id = null;

                for (Map<String, Object> map : chat_msgs) {
                    LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(map.get("send_at")), formatter);
                    if ((mostRecentDate == null && resent_msg_id == null) || (dateTime.isBefore(mostRecentDate))) {
                        mostRecentDate = dateTime;
                        resent_msg_id = Long.valueOf(String.valueOf(map.get("msg_id")));
                        current_message = String.valueOf(map.get("msg"));
                        message_sender_id = Long.valueOf(String.valueOf(map.get("send_by")));
                    }
                }

                if (message_sender_id == sender_id) {
                    for (int i = 0; i < chat_msgs.size(); i++) {
                        Map<String, Object> current_msg = chat_msgs.get(i);
                        if (Long.valueOf(String.valueOf(current_msg.get("send_by"))) == sender_id &&
                                Long.valueOf(String.valueOf(current_msg.get("msg_id"))) == resent_msg_id) {
                            current_msg.put("msg", current_message + message);
                            break;
                        }
                    }

                } else {
                    Map<String, Object> new_msg = new HashMap<>();
                    new_msg.put("send_by", sender_id);
                    new_msg.put("msg_id", new_msg_id);
                    new_msg.put("send_at", send_at);
                    new_msg.put("msg", message);

                    chat_msgs.add(new_msg);

                }
                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_chats, convId,
                        new Document("msgs", chat_msgs));


            }

        }
        return false;
    }

    public Long getDMChannelID(long user_id, long user_id2) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(user_id);
            condition_data.add(user_id2);

            Map<String, List<Object>> msg_data = databaseManager.getDataSQL(DatabaseManager.table_chats,
                    "channel_id", "send_by = ? OR send_by = ?", condition_data,
                    null, "", 0);

            if (msg_data == null || msg_data.isEmpty() || msg_data.get("channel_id").isEmpty()) {
                return null;

            } else {
                try {
                    return Long.parseLong(String.valueOf(msg_data.get("channel_id").get(0)));

                } catch (Exception e) {
                    return null;
                }
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> chat_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                    new Document("user1", user_id).append("user2", user_id2), true, "channel_id");

            if (chat_data == null || chat_data.isEmpty() || chat_data.get(0).isEmpty()) {

                List<Map<String, Object>> chat_data2 = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                        new Document("user2", user_id).append("user1", user_id2), true, "channel_id");

                return chat_data2 == null || chat_data2.isEmpty() || chat_data2.get(0).isEmpty() ? null :
                        Long.valueOf(String.valueOf(chat_data2.get(0).get("channel_id")));

            } else {
                return Long.valueOf(String.valueOf(chat_data.get(0).get("channel_id")));
            }

        }

        return null;
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

            result.put("msgs", new ArrayList<>());
            result.put("user1", new ArrayList<>());
            result.put("user2", new ArrayList<>());
            result.put("channel_id", new ArrayList<>());

            int i = 0;
            for (Map<String, Object> map : mongoData) {
                for (Map.Entry<String, Object> data : map.entrySet()) {
                    result.get(data.getKey()).add(data.getValue());
                }
                i++;
                if (i >= amount) {
                    break;
                }
            }

            return result;

        }
        return null;
    }

    public boolean deleteMessage(long sender_id, long channel_id, long message_id, long actor_id, long group_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(channel_id);
            condition_data.add(message_id);
            condition_data.add(sender_id);

            if (group_id == 0L) {
                // dm
                return databaseManager.deleteDataSQL(databaseManager.table_chats,
                        "channel_id = ? AND msg_id = ? AND send_by = ?", condition_data);

            } else if (sender_id != actor_id) {

                // TODO not a DM

                    return databaseManager.deleteDataSQL(databaseManager.table_chats,
                            "channel_id = ? AND msg_id = ? AND send_by = ?", condition_data);



            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            Document channelId = new Document("channel_id", channel_id);
            if (group_id == 0L) {

                List<Map<String, Object>> msgs_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                        channelId, true, "msgs");

                if (msgs_data == null || msgs_data.isEmpty()) {
                    return false;
                }

                List<Map<String, Object>> the_messages = (List<Map<String, Object>>) msgs_data.get(0).get("msgs");

                for (int i = 0; i < the_messages.size(); i++) {
                    Map<String, Object> msg = the_messages.get(i);
                    if (Long.valueOf(String.valueOf(msg.get("send_by"))) == sender_id &&
                            Long.valueOf(String.valueOf(msg.get("msg_id"))) == message_id) {
                        the_messages.remove(i);
                        break;
                    }
                }

                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_chats,
                        channelId, new Document("msgs", the_messages));


            } else {
                List<Map<String, Object>> reaction_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_group_members,
                        new Document("group_id", group_id)
                                .append("member_id", message_id), true, "roles_id");

                if (reaction_data == null) {
                    return false;
                }

                if (!reaction_data.isEmpty() && !reaction_data.get(0).isEmpty()) {
                    List<Long> role_ids = new ArrayList<>();
                    try {
                        String[] alL_roles = String.valueOf(reaction_data.get(0).get("roles_id")).split(",");
                        for (String role : alL_roles) {
                            role_ids.add(Long.parseLong(role));
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    int i = 0;
                    while (true) {
                        List<Map<String, Object>> roles_data =
                                databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_group_roles,
                                        new Document("group_id", group_id)
                                                .append("role_id", role_ids.get(i)), true, "permissions");

                        if (roles_data == null || i >= role_ids.size()) {
                            return false;
                        }
                        if (String.valueOf(roles_data.get(0).get("permissions")).contains("\"delete_message\": true")) {
                            break;
                        }
                        i++;
                    }

                    List<Map<String, Object>> msgs_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_chats,
                            channelId, true, "msgs");

                    if (msgs_data == null || msgs_data.isEmpty()) {
                        return false;
                    }

                    List<Map<String, Object>> the_messages = (List<Map<String, Object>>) msgs_data.get(0).get("msgs");

                    for (int i2 = 0; i2 < the_messages.size(); i2++) {
                        Map<String, Object> msg = the_messages.get(i2);
                        if (Long.valueOf(String.valueOf(msg.get("send_by"))) == sender_id &&
                                Long.valueOf(String.valueOf(msg.get("msg_id"))) == message_id) {
                            the_messages.remove(i2);
                            break;
                        }
                    }

                    return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_chats,
                            channelId, new Document("msgs", the_messages));

                }


            }

        }
        return false;
    }


    public boolean addReaction(long channel_id, long message_id, String reaction, long actor_id, long group_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> sender_condition = new ArrayList<>();
            sender_condition.add(channel_id);

            Map<String, List<Object>> account_data_sender2 = databaseManager.getDataSQL(databaseManager.table_group_channels,
                    "name", "channel_id = ?",
                    sender_condition, null, "", 1);

            if (account_data_sender2 == null || account_data_sender2.isEmpty() ||
                    account_data_sender2.get("name").isEmpty()) {

                // TODO not a DM

                List<Object> addData = new ArrayList<>();
                addData.add(channel_id);
                addData.add(reaction);
                addData.add(message_id);
                addData.add(actor_id);

                return databaseManager.addDataSQL(DatabaseManager.table_reactions,
                        "channel_id, reaction, msg_id, member_id",
                        "?, ?, ?, ?", addData);


            } else {
                List<Object> addData = new ArrayList<>();
                addData.add(channel_id);
                addData.add(reaction);
                addData.add(message_id);
                addData.add(actor_id);

                return databaseManager.addDataSQL(DatabaseManager.table_reactions,
                        "channel_id, reaction, msg_id, member_id",
                        "?, ?, ?, ?", addData);
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> account_data2 = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_group_channels,
                    new Document("channel_id", channel_id), true, "name");

            if (account_data2 == null || account_data2.isEmpty() || account_data2.get(0).isEmpty()) {
                // TODO not a DM

                return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_reactions,
                        new Document("channel_id", channel_id)
                                .append("reaction", reaction)
                                .append("msg_id", message_id).append("member_id", actor_id),
                        null);

            }


        } else {


            Document data = new Document("channel_id", channel_id)
                    .append("reaction", reaction)
                    .append("msg_id", message_id).append("member_id", actor_id);

            List<Map<String, Object>> reaction_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_reactions,
                    data, false);

            if (reaction_data == null || !reaction_data.isEmpty() || !reaction_data.get(0).isEmpty()) {
                return false;
            }

            return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_reactions, data, null);
        }

        return false;

    }

    public boolean removeReaction(long channel_id, long message_id, String reaction, long actor_id, long group_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> sender_condition = new ArrayList<>();
            sender_condition.add(channel_id);

            Map<String, List<Object>> account_data_sender2 = databaseManager.getDataSQL(databaseManager.table_group_channels,
                    "name", "channel_id = ?",
                    sender_condition, null, "", 1);

            if (account_data_sender2 != null && !account_data_sender2.isEmpty() &&
                    !account_data_sender2.get("name").isEmpty()) {
                // TODO not a DM

                    List<Object> condition_data3 = new ArrayList<>();
                    condition_data3.add(channel_id);
                    condition_data3.add(reaction);
                    condition_data3.add(message_id);
                    condition_data3.add(actor_id);

                    return databaseManager.deleteDataSQL(DatabaseManager.table_reactions,
                            "channel_id = ? AND reaction = ? AND msg_id = ? AND member_id = ?", condition_data3);


            } else {
                List<Object> condition_data = new ArrayList<>();
                condition_data.add(channel_id);
                condition_data.add(reaction);
                condition_data.add(message_id);
                condition_data.add(actor_id);

                return databaseManager.deleteDataSQL(DatabaseManager.table_reactions,
                        "channel_id = ? AND reaction = ? AND msg_id = ? AND member_id = ?", condition_data);
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> account_data2 = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", channel_id), true, "name");

            if (account_data2 == null || account_data2.isEmpty() || account_data2.get(0).isEmpty()) {
                // TODO not a DM
                return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_reactions,
                        new Document("channel_id", channel_id).append("reaction", reaction)
                                .append("msg_id", message_id));
            }

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
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> captcha_data = databaseManager.getDataSQL(databaseManager.table_captchas,
                    "answer, time, failed",
                    "id = ?", condition_data, null, "", 0);

            if (captcha_data == null) {
                return false;
            }

            if (String.valueOf(captcha_data.get("answer").get(0)).equals(given_answer)) {
                // solved!
                return databaseManager.deleteDataSQL(databaseManager.table_captchas, "id = ?", condition_data);

            } else if (3 <= Short.valueOf(String.valueOf(captcha_data.get("failed").get(0))) ||
                    (0 >= Short.valueOf(String.valueOf(captcha_data.get("time").get(0))))) {
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
            if (captcha_data == null) return false;

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

            if (Short.valueOf(String.valueOf(sess_data.get("time").get(0))) <= 0) {
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
            if (sqlIfAccountExists(sender_id, databaseManager.table_accounts)) return false;

            // there is no custom background if empty
            List<Object> data = new ArrayList<>();
            data.add(sender_id);
            data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            data.add(msg);
            data.add(background);

            return databaseManager.addDataSQL(databaseManager.table_posts, "sender_id, send_at, msg, background",
                    "?, ?, ?, ?", data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", sender_id), true, "name"))) return false;

            List<Map<String, Object>> post_data = databaseManager.MongoReadCollectionNoSQL(databaseManager.table_posts,
                    null, false,"id");
            if (post_data == null) {
                return false;
            }

            long id = databaseManager.MongoGenerateID(post_data);

            return databaseManager.MongoAddDataToCollectionNoSQL(databaseManager.table_posts,
                    new Document("id", id).append("sender_id", sender_id)
                    .append("msg", msg).append("send_at",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append("background", background), null);

        }
        return false;
    }

    public Map<String, List<Object>> getLatestPosts(int amount) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            Map<String, List<Object>> posts = databaseManager.getDataSQL(DatabaseManager.table_posts,
                    "*", "", null, null, "send_at DESC", amount);

            return posts == null || posts.isEmpty() ? null : posts;

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_posts,
                    null, false);
            Map<String, List<Object>> result = new HashMap<>();

            result.put("id", new ArrayList<>());
            result.put("sender_id", new ArrayList<>());
            result.put("msg", new ArrayList<>());
            result.put("send_at", new ArrayList<>());
            result.put("background", new ArrayList<>());

            int i = 0;
            for (Map<String, Object> map : mongoData) {
                for (Map.Entry<String, Object> data : map.entrySet()) {
                    result.get(data.getKey()).add(data.getValue());
                }
                i++;
                if (i >= amount) {
                    break;
                }
            }

            return result;

        }
        return null;
    }

    public boolean deletePost(long sender_id, long post_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            List<Object> condition_data_comments = new ArrayList<>();
            condition_data_comments.add(post_id);

            Map<String, List<Object>> all_comments = databaseManager.getDataSQL(DatabaseManager.table_post_comments, "*",
                    "post_id = ?", condition_data_comments, null, "send_at DESC", 0);

            if (all_comments != null && !databaseManager.deleteDataSQL(databaseManager.table_post_comments,
                    "post_id = ?", condition_data_comments)) {

                return false;
            }

            return databaseManager.deleteDataSQL(databaseManager.table_posts,
                    "id = ? AND sender_id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> all_comments = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_post_comments,
                    new Document("post_id", post_id), false);

            if (all_comments != null && !databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_post_comments,
                    new Document("post_id", post_id))) {
                return false;
            }

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id)
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





    public boolean addCommentToPost(long sender_id, long post_id, String message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(sender_id, DatabaseManager.table_accounts) ||
                    sqlIfAccountExists(post_id, DatabaseManager.table_posts)) return false;

            List<Object> addData = new ArrayList<>();
            addData.add(post_id);
            addData.add(sender_id);
            addData.add(now.truncatedTo(ChronoUnit.MINUTES));
            addData.add(message);

            return databaseManager.addDataSQL(DatabaseManager.table_post_comments,
                    "post_id, send_by, send_at, msg", "?, ?, ?, ?", addData);


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", sender_id), true, "name"))) return false;

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), true, "msg"))) return false;

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (collection == null) {
                return false;
            }

            List<Object> ids = new ArrayList<>();
            for (Map<String, Object> msg : collection) {
                ids.add(msg.get("msg_id"));
            }

            Map<String, Object> comment = new HashMap<>();
            comment.put("post_id", post_id);
            comment.put("send_by", sender_id);
            comment.put("send_at", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            comment.put("msg", message);
            comment.put("msg_id", databaseManager.generateID(ids));

            collection.add(comment);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", collection));
        }
        return false;
    }

    public Map<String, List<Object>> getLatestCommentsOnPost(long post_id, int amount) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);

            return databaseManager.getDataSQL(DatabaseManager.table_post_comments, "*",
                    "post_id = ?", condition_data, null, "send_at DESC", amount);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> mongoData = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            Map<String, List<Object>> result = new HashMap<>();
            result.put("post_id", new ArrayList<>());
            result.put("send_by", new ArrayList<>());
            result.put("send_at", new ArrayList<>());
            result.put("msg", new ArrayList<>());
            result.put("msg_id", new ArrayList<>());

            int i = 0;
            for (Map<String, Object> map : mongoData) {
                for (Map.Entry<String, Object> data : map.entrySet()) {
                    result.get(data.getKey()).add(data.getValue());
                }
                i++;
                if (i >= amount) {
                    break;
                }
            }

            return result;


        }
        return null;
    }

    public boolean deleteCommentFromPost(long post_id, long sender_id, long message_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);
            condition_data.add(message_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_post_comments,
                    "post_id = ? AND send_by = ? AND msg_id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> comment_data = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (comment_data == null) {
                return false;
            }

            for (int i = 0; i < comment_data.size(); i++) {
                if (Long.valueOf(String.valueOf(comment_data.get(i).get("msg_id"))) == message_id) {
                    comment_data.remove(i);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", comment_data));

        }
        return false;
    }

    public boolean updateCommentMessage(long post_id, long sender_id, long message_id, String new_message) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);
            condition_data.add(message_id);

            List<Object> set_data = new ArrayList<>();
            set_data.add(new_message);

            return databaseManager.editDataSQL(DatabaseManager.table_post_comments,
                    "msg = ?", set_data,
                    "post_id = ? AND send_by = ? AND msg_id = ?", condition_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> comment_data = databaseManager.getCollectionMongo(DatabaseManager.table_posts,
                    "comments", new Document("id", post_id));

            if (comment_data == null) {
                return false;
            }

            for (int i = 0; i < comment_data.size(); i++) {
                Map<String, Object> comment = comment_data.get(i);
                if (Long.valueOf(String.valueOf(comment.get("msg_id"))) == message_id) {
                    comment.put("msg", new_message);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_posts,
                    new Document("id", post_id), new Document("comments", comment_data));

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











    private boolean updateGroupLogs(long actor_id, long group_id, String log_message, LocalDateTime now,
                                    String log_type) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> log_data = new ArrayList<>();
            log_data.add(group_id);
            log_data.add(actor_id);
            log_data.add(log_type);
            log_data.add(log_message);
            log_data.add(now.truncatedTo(ChronoUnit.MINUTES));

            return databaseManager.addDataSQL(DatabaseManager.table_group_logs,
                    "group_id, actor_id, log_type, log_message, acted_at",
                    "?, ?, ?, ?, ?", log_data);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "logs", new Document("id", group_id));

            Map<String, Object> log = new HashMap<>();
            log.put("actor_id", actor_id);
            log.put("log_type", log_type);
            log.put("log_message", log_message);
            log.put("acted_at", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            collection.add(log);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("group_id", group_id), new Document("logs", collection));
        }

        return false;
    }

    public Map<String, List<Object>> getAllGroupsWithUser(long user_id, int amount) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(user_id);

            return databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "group_id", "member_id = ?", condition_data,
                    null, "", amount);

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> mongoData = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    null, false);

            if (mongoData == null) {
                return null;
            }

            Map<String, List<Object>> result = new HashMap<>();
            result.put("owner_id", new ArrayList<>());
            result.put("logs", new ArrayList<>());
            result.put("channels", new ArrayList<>());
            result.put("categories", new ArrayList<>());
            result.put("roles", new ArrayList<>());
            result.put("members", new ArrayList<>());
            result.put("name", new ArrayList<>());
            result.put("id", new ArrayList<>());
            result.put("logo", new ArrayList<>());
            result.put("banner", new ArrayList<>());
            result.put("animations", new ArrayList<>());
            result.put("settings", new ArrayList<>());
            result.put("created_at", new ArrayList<>());

            int i = 0;
            for (Map<String, Object> map : mongoData) {
                for (Map.Entry<String, Object> data : map.entrySet()) {
                    result.get(data.getKey()).add(data.getValue());
                }
                i++;
                if (i >= amount) {
                    break;
                }
            }

            return result;

        }

        return null;
    }

    public boolean createGroup(long owner_id, String name, String logo, String banner, String animations,
                               String settings) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(owner_id, databaseManager.table_accounts)) return false;

            List<Object> group_data = new ArrayList<>();
            group_data.add(name);
            group_data.add(owner_id);
            group_data.add(logo);
            group_data.add(banner);
            group_data.add(animations);
            group_data.add(settings);
            group_data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

            if (!databaseManager.addDataSQL(DatabaseManager.table_groups,
                    "name, owner_id, logo, banner, animations, settings, created_at",
                    "?, ?, ?, ?, ?, ?, ?",
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

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", owner_id), true, "name"))) return false;

            List<Map<String, Object>> all_groups_id = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    null, false, "id");

            if (all_groups_id == null) {
                return false;
            }

            return databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", databaseManager.MongoGenerateID(all_groups_id))
                            .append("name", name).append("owner_id", owner_id)
                            .append("logo", logo).append("banner", banner)
                            .append("animations", animations)
                            .append("settings", settings).append("members",
                                    Arrays.asList(new Document("member_id", owner_id)
                                            .append("roles_id", "").append("nickname", null))), null);

        }
        return false;
    }

    public boolean deleteGroup(long group_id) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_logs,
                    "group_id = ?", condition_data)) {
                return false;
            }


            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_channels,
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

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_group_members,
                    "group_id = ?", condition_data)) {
                return false;
            }

            if (!databaseManager.deleteDataSQL(DatabaseManager.table_groups,
                    "id = ?", condition_data)) {
                return false;
            }

            return true;

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id));

        }
        return false;
    }

    public boolean updateGroupSettings(long actor_id, long group_id, String new_settings, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_settings);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "settings = ?",
                    updatedData, "id = ?", search_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Settings");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("settings", new_settings)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Settings");
        }
        return false;
    }

    public boolean updateGroupName(long actor_id, long group_id, String new_name, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_name);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "name = ?",
                    updatedData, "id = ?", search_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Name");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("name", new_name)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Name");
        }
        return false;
    }

    public boolean updateGroupOwner(long new_owner_id, long group_id, long actor_id, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(new_owner_id, databaseManager.table_accounts) ||
                    sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;


            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_owner_id);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "owner_id = ?",
                    updatedData, "id = ?", search_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Changed Owner");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", new_owner_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("owner_id", new_owner_id)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Changed Owner");

        }
        return false;
    }

    public boolean updateGroupLogo(String new_logo, long group_id, long actor_id, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_logo);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "logo = ?",
                    updatedData, "id = ?", search_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Logo");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("logo", new_logo)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Logo");

        }
        return false;
    }

    public boolean updateGroupBanner(String new_banner, long group_id, long actor_id, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> search_data = new ArrayList<>();
            search_data.add(group_id);

            List<Object> updatedData = new ArrayList<>();
            updatedData.add(new_banner);

            return databaseManager.editDataSQL(DatabaseManager.table_groups, "banner = ?",
                    updatedData, "id = ?", search_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Banner");


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("banner", new_banner)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Banner");
        }
        return false;
    }

    public Map<String, List<Object>> getAllStuffFromGroupSQL(long group_id, int amount) {
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> group_data = databaseManager.getDataSQL(DatabaseManager.table_groups,
                    "name, id, owner_id, logo, banner, animations, settings, created_at",
                    "id = ?", condition_data, null, "", 0);

            if (group_data == null) {
                return null;
            }

            Map<String, List<Object>> members_data = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "member_id, roles_id, nickname",
                    "group_id = ?", condition_data, null, "", amount);

            if (members_data == null) {
                return null;
            }

            Map<String, List<Object>> channels_data = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "channel_id, name, permissions, channel_type, category_id",
                    "group_id = ?", condition_data, null, "", amount);

            if (channels_data == null) {
                return null;
            }

            Map<String, List<Object>> category_data = databaseManager.getDataSQL(DatabaseManager.table_group_category,
                    "category_id AS Category_category_id, name AS Category_name, permissions AS Category_permissions, category_type AS Category_category_type",
                    "group_id = ?", condition_data, null, "", amount);

            if (category_data == null) {
                return null;
            }

            Map<String, List<Object>> roles_data = databaseManager.getDataSQL(DatabaseManager.table_group_roles,
                    "role_id, name, permissions, role_type",
                    "group_id = ?", condition_data, null, "", amount);

            if (roles_data == null) {
                return null;
            }

            Map<String, List<Object>> logs_data = databaseManager.getDataSQL(DatabaseManager.table_group_logs,
                    "actor_id, log_type, log_message, acted_at",
                    "group_id = ?", condition_data, null, "", amount);

            if (logs_data == null) {
                return null;
            }

            for (Map.Entry<String, List<Object>> mem : members_data.entrySet()) {
                group_data.put("member_"+mem.getKey(), mem.getValue());
            }

            for (Map.Entry<String, List<Object>> ch : channels_data.entrySet()) {
                group_data.put("channel_"+ch.getKey(), ch.getValue());
            }

            for (Map.Entry<String, List<Object>> cate : category_data.entrySet()) {
                group_data.put("category_"+cate.getKey(), cate.getValue());
            }

            for (Map.Entry<String, List<Object>> role : roles_data.entrySet()) {
                group_data.put("role_"+role.getKey(), role.getValue());
            }

            for (Map.Entry<String, List<Object>> log : logs_data.entrySet()) {
                group_data.put("log_"+log.getKey(), log.getValue());
            }

            return group_data.isEmpty() ? null : group_data;

        }
        return null;
    }








    public boolean addGroupMember(long member_id, long group_id, String roles_id, String nickname, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(member_id, databaseManager.table_accounts)) return false;

            if (sqlIfAccountExists(group_id, databaseManager.table_groups)) return false;

            List<Object> member_data = new ArrayList<>();
            member_data.add(group_id);
            member_data.add(member_id);
            member_data.add(roles_id);
            member_data.add(nickname);

            return databaseManager.addDataSQL(DatabaseManager.table_group_members,
                    "group_id, member_id, roles_id, nickname", "?, ?, ?, ?", member_data) ?
                    updateGroupLogs(member_id, group_id, log_message, now, "Joined The Group") : false;

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", member_id), true, "name"))) return false;

            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "name"))) return false;

            List<Map<String, Object>> group = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "members");

            if (group == null) {
                return false;
            }

            List<Map<String, Object>> all_members = new ArrayList<>();
            Object mem = group.get(0).get("members");

            if (mem != null) {
                all_members.addAll((List<Map<String, Object>>) group.get(0).get("members"));
            }

            Map<String, Object> member_data = new HashMap<>();
            member_data.put("member_id", member_id);
            member_data.put("roles_id", roles_id);
            member_data.put("nickname", nickname);

            all_members.add(member_data);

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("members", all_members)) &&
                    updateGroupLogs(member_id, group_id, log_message, now, "Joined The Group");
        }
        return false;
    }

    public boolean removeGroupMember(long member_id, long group_id, String leave_type, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> member_data = new ArrayList<>();
            member_data.add(group_id);

            Map<String, List<Object>> group = databaseManager.getDataSQL(DatabaseManager.table_group_members,
                    "member_id", "group_id = ?", member_data, null, "" ,0);

            if (group == null) {
                return false;
            }

            if (group.isEmpty() || group.get("member_id").isEmpty()) {
                // the group is empty | delete the group
                return deleteGroup(group_id);

            } else {

                member_data.add(member_id);

                return databaseManager.deleteDataSQL(DatabaseManager.table_group_members,
                        "group_id = ? AND member_id = ?", member_data) ?
                        updateGroupLogs(member_id, group_id, log_message, now, leave_type) : false;
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {

            List<Map<String, Object>> group = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "members");

            if (group == null || group.isEmpty()) {
                return false;
            }


            List<Map<String, Object>> all_members = (List<Map<String, Object>>) group.get(0).get("members");

            for (int i = 0; i < all_members.size(); i++) {
                if (Long.valueOf(String.valueOf(all_members.get(i).get("member_id"))) == member_id) {
                    all_members.remove(i);
                    break;
                }
            }

            if (all_members.isEmpty()) {
                // last member left | delete the group
                return deleteGroup(group_id);

            } else {
                return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_groups,
                        new Document("id", group_id)
                                .append("members", all_members)) &&
                        updateGroupLogs(member_id, group_id, log_message, now, leave_type);

            }
        }
        return false;
    }

    public boolean updateMemberRoles(long member_id, long group_id, long role_id, long actor_id, boolean toAdd,
                                     String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, DatabaseManager.table_accounts)) return false;
            if (sqlIfAccountExists(role_id, DatabaseManager.table_group_roles)) return false;

            List<Object> set_data = new ArrayList<>();
            set_data.add(","+role_id);

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(member_id);

            return toAdd ? (databaseManager.editDataSQL(DatabaseManager.table_group_members,
                        databaseManager.postgressql_connection != null ? "roles_id = roles_id || ?" :
                                "roles_id = CONCAT(roles_id, ?)", set_data,
                        "group_id = ? AND member_id = ?", condition_data) &&
                        updateGroupLogs(actor_id, group_id, log_message, now, "Added Roles From Member"))
                        :
                        (databaseManager.editDataSQL(databaseManager.table_group_members,
                                "roles_id = REPLACE(roles_id, ?, '')", set_data,
                                "group_id = ? AND member_id = ?", condition_data) &&
                                updateGroupLogs(actor_id, group_id, log_message, now,
                                        "Removed Roles From Member"));


        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("group_id", group_id);
            List<Map<String, Object>> user_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    filter, true, "members");

            if (user_data == null) {
                return false;
            }

            String role_value = "," + role_id;
            List<Map<String, Object>> members = (List<Map<String, Object>>) user_data.get(0).get("members");
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
                    updateGroupLogs(actor_id, group_id, log_message, now,
                            toAdd ? "Added Roles From Member" : "Removed Roles From Member" );


        }
        return false;

    }

    public boolean updateMemberNickname(long member_id, long group_id, long actor_id, String new_nickname,
                                        String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_nickname);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(member_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_members, "nickname = ?",
                    nickname_data, "group_id = ? AND member_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Nickname");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("group_id", group_id);
            List<Map<String, Object>> user_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    filter, true, "members");

            if (user_data == null) {
                return false;
            }

            List<Map<String, Object>> members = (List<Map<String, Object>>) user_data.get(0).get("members");
            for (Map<String, Object> mem : members) {
                if (Long.valueOf(String.valueOf(mem.get("member_id"))) == member_id) {
                    mem.put("nickname", new_nickname);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("members", members)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Nickname");


        }
        return false;
    }









    public Long createGroupChannel(long group_id, long actor_id, String channel_type, String name,
                                      String permissions, String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> sender_condition = new ArrayList<>();
            sender_condition.add(actor_id);

            Map<String, List<Object>> account_data_sender = databaseManager.getDataSQL(DatabaseManager.table_accounts,
                    "name", "id = ?",
                    sender_condition, null, "", 1);

            if (account_data_sender == null || account_data_sender.isEmpty() ||
                    account_data_sender.get("name").isEmpty()) {
                return null;
            }

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(category_id);

            Map<String, List<Object>> channels_data = databaseManager.getDataSQL(DatabaseManager.table_group_channels,
                    "channel_id", "group_id = ? AND category_id = ?", condition_data,
                    null, "", 0);

            if (channels_data == null) {
                return null;
            }

            List<Object> add_data = new ArrayList<>();
            long id = databaseManager.generateID(channels_data.get("channel_id"));
            add_data.add(group_id);
            add_data.add(id);
            add_data.add(name);
            add_data.add(permissions);
            add_data.add(channel_type);
            add_data.add(category_id);

            if (databaseManager.addDataSQL(DatabaseManager.table_group_channels,
                    "group_id, channel_id, name, permissions, channel_type, category_id",
                    "?, ?, ?, ?, ?, ?", add_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+channel_type+" Channel")) {

                return id;

            } else {
                return null;
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            List<Map<String, Object>> account_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name");

            if (account_data == null || account_data.isEmpty() || account_data.get(0).isEmpty()) {
                return null;
            }

            List<Map<String, Object>> channels_data = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "channels");

            if (channels_data == null) {
                return null;
            }

            List<Map<String, Object>> all_channels = new ArrayList<>();

            Map<String, Object> channel = new HashMap<>();


            Object channels = channels_data.get(0).get("channels");
            if (channels != null) {
                all_channels.addAll((List<Map<String, Object>>) channels);

            }

            List<Object> channels_id = new ArrayList<>();
            for (Map<String, Object> ch : all_channels) {
                channels_id.add(ch.get("channel_id"));
            }

            long id = databaseManager.generateID(channels_id);

            channel.put("channel_id", id);
            channel.put("name", name);
            channel.put("permissions", permissions);
            channel.put("channel_type", channel_type);
            channel.put("category_id", category_id);

            all_channels.add(channel);



            if (databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("channels", all_channels)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+channel_type+" Channel")) {

                return id;

            } else {
                return null;
            }

        }
        return null;
    }


    public boolean deleteGroupChannel(long channel_id, long group_id, long actor_id, String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(channel_id);
            condition_data.add(category_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_channels,
                    "group_id = ? AND channel_id = ? AND category_id = ?",
                    condition_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Channel");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "id"))) return false;

            List<Map<String, Object>> group = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), true, "channels");

            if (group == null) {
                return false;
            }

            List<Map<String, Object>> all_channels = new ArrayList<>();
            Object channels = group.get(0).get("channels");

            if (!group.isEmpty() && channels != null) {
                all_channels.addAll((List<Map<String, Object>>) channels);
                for (int i = 0; i < all_channels.size(); i++) {
                    if (Long.valueOf(String.valueOf(all_channels.get(i).get("channel_id"))) == channel_id) {
                        all_channels.remove(i);
                        break;
                    }
                }

                return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                        new Document("id", group_id), new Document("channels", all_channels)) &&
                        updateGroupLogs(actor_id, group_id, log_message, now,
                                "Deleted Channel");

            } else {
                return false;
            }

        }
        return false;
    }

    public boolean updateGroupChannelName(long channel_id, long group_id, long actor_id, String new_name,
                                          String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "name = ?",
                    nickname_data, "group_id = ? AND channel_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Name");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) {
                return false;
            }

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", new Document("id", group_id));

            if (all_channels == null) {
                return false;
            }

            for (int i = 0; i < all_channels.size(); i++) {
                Map<String, Object> ch = all_channels.get(i);
                if (Long.valueOf(String.valueOf(ch.get("channel_id"))) == channel_id) {
                    ch.put("name", new_name);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("channels", all_channels)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Name");
        }

        return false;

    }

    private boolean checkIfAccountDoesntExists(List<Map<String, Object>> databaseManager) {
        return databaseManager == null || databaseManager.isEmpty() || databaseManager.get(0).isEmpty();
    }

    public boolean updateGroupChannelPermissions(long channel_id, long group_id, long actor_id, String new_permissions,
                                          String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_permissions);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "permissions = ?",
                    nickname_data, "group_id = ? AND channel_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Permissions");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", new Document("id", group_id));

            if (all_channels == null) {
                return false;
            }

            for (int i = 0; i < all_channels.size(); i++) {
                Map<String, Object> ch = all_channels.get(i);
                if (Long.valueOf(String.valueOf(ch.get("channel_id"))) == channel_id) {
                    ch.put("permissions", new_permissions);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("channels", all_channels)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Permissions");
        }

        return false;

    }

    public boolean updateGroupChannelType(long channel_id, long group_id, long actor_id, String new_channel_type,
                                                 String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_channel_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "channel_type = ?",
                    nickname_data, "group_id = ? AND channel_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Type");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", new Document("id", group_id));

            if (all_channels == null) {
                return false;
            }

            for (int i = 0; i < all_channels.size(); i++) {
                Map<String, Object> ch = all_channels.get(i);
                if (Long.valueOf(String.valueOf(ch.get("channel_id"))) == channel_id) {
                    ch.put("channel_type", new_channel_type);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("channels", all_channels)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Type");
        }

        return false;
    }

    public boolean updateGroupChannelCategory(long channel_id, long group_id, long actor_id, long new_category_id,
                                          String log_message, long category_id) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_category_id);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(channel_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_channels, "category_id = ?",
                    nickname_data, "group_id = ? AND channel_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Category");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> all_channels = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "channels", new Document("id", group_id));

            if (all_channels == null) {
                return false;
            }

            for (int i = 0; i < all_channels.size(); i++) {
                Map<String, Object> ch = all_channels.get(i);
                if (Long.valueOf(String.valueOf(ch.get("channel_id"))) == channel_id) {
                    ch.put("category_id", new_category_id);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("channels", all_channels)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Channel Category");
        }

        return false;
    }













    public Long createGroupRole(long actor_id, long group_id, String name, String permissions, String role_type,
                                String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            List<Object> sender_condition = new ArrayList<>();
            sender_condition.add(actor_id);

            Map<String, List<Object>> account_data_sender = databaseManager.getDataSQL(databaseManager.table_accounts,
                    "name", "id = ?",
                    sender_condition, null, "", 1);

            if (account_data_sender == null || account_data_sender.isEmpty() ||
                    account_data_sender.get("name").isEmpty()) {
                return null;
            }

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> roles_data = databaseManager.getDataSQL(DatabaseManager.table_group_roles,
                    "role_id", "group_id = ?", condition_data, null, "", 0);

            if (roles_data == null) {
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
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+role_type+" Role")) {

                return id;

            } else {
                return null;
            }

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return null;

            List<Map<String, Object>> roles_id = databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id), false, "role_id");

            if (roles_id == null) {
                return null;
            }

            long id = databaseManager.MongoGenerateID(roles_id);

            if (databaseManager.MongoAddDataToCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id)
                            .append("role_id", id)
                            .append("name", name)
                            .append("permissions", permissions)
                            .append("role_type", role_type), null) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+role_type+" Role")) {
                return id;

            } else {
                return null;
            }

        }
        return null;
    }

    public boolean deleteGroupRole(long role_id, long group_id, long actor_id, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(role_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_roles,
                    "group_id = ? AND role_id = ?", condition_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Role");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoDeleteDataFromCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id)
                            .append("role_id", role_id)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Role");

        }
        return false;
    }

    public boolean updateGroupRoleName(long role_id, long group_id, long actor_id, String new_name,
                                          String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "name = ?",
                    nickname_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Name");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id).append("role_id", role_id),
                    new Document("name", new_name)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Name");
        }

        return false;

    }

    public boolean updateGroupRolePermissions(long role_id, long group_id, long actor_id, String new_permissions,
                                       String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_permissions);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "permissions = ?",
                    nickname_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Permissions");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id).append("role_id", role_id),
                    new Document("permissions", new_permissions)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Permissions");
        }

        return false;

    }

    public boolean updateGroupRoleType(long role_id, long group_id, long actor_id, String new_role_type,
                                              String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_role_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(role_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_roles, "role_type = ?",
                    nickname_data, "group_id = ? AND role_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Type");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_group_roles,
                    new Document("group_id", group_id).append("role_id", role_id),
                    new Document("role_type", new_role_type)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Role Type");
        }

        return false;

    }






    public boolean createGroupCategory(long actor_id, long group_id, String name, String permissions,
                                       String category_type, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> add_data = new ArrayList<>();
            add_data.add(group_id);
            add_data.add(name);
            add_data.add(permissions);
            add_data.add(category_type);

            return databaseManager.addDataSQL(DatabaseManager.table_group_category,
                    "group_id, name, permissions, category_type",
                    "?, ?, ?, ?", add_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+category_type+" Category");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", new Document("id", group_id));

            if (collection == null) {
                return false;
            }

            List<Object> all_ids = new ArrayList<>();
            for (Map<String, Object> cat : collection) {
                all_ids.add(cat.get("category_id"));
            }

            long id = databaseManager.generateID(all_ids);

            Map<String, Object> category = new HashMap<>();
            category.put("category_id", id);
            category.put("name", name);
            category.put("permissions", permissions);
            category.put("category_type", category_type);
            collection.add(category);


            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    new Document("id", group_id), new Document("categories", collection)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Created "+category_type+" Category");

        }
        return false;
    }

    public boolean deleteGroupCategory(long actor_id, long group_id, long category_id, String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(category_id);

            return databaseManager.deleteDataSQL(DatabaseManager.table_group_category,
                    "group_id = ? AND category_id = ?", condition_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Category");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("id", group_id);

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            for (int i = 0; i < collection.size(); i++) {
                if (Long.valueOf(String.valueOf(collection.get(i).get("category_id"))) == category_id) {
                    collection.remove(i);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Deleted Category");

        }
        return false;
    }


    public boolean updateGroupCategoryName(long category_id, long group_id, long actor_id, String new_name,
                                       String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_name);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_category, "name = ?",
                    nickname_data, "group_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Name");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("id", group_id);

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            for (int i = 0; i < collection.size(); i++) {
                Map<String, Object> cat = collection.get(i);
                if (Long.valueOf(String.valueOf(cat.get("category_id"))) == category_id) {
                    cat.put("name", new_name);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Name");
        }

        return false;

    }

    public boolean updateGroupCategoryPermissions(long category_id, long group_id, long actor_id, String new_permissions,
                                              String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, databaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_permissions);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_category, "permissions = ?",
                    nickname_data, "group_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Permissions");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("id", group_id);

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            for (int i = 0; i < collection.size(); i++) {
                Map<String, Object> cat = collection.get(i);
                if (Long.valueOf(String.valueOf(cat.get("category_id"))) == category_id) {
                    cat.put("permissions", new_permissions);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Permissions");
        }

        return false;

    }

    public boolean updateGroupCategoryType(long category_id, long group_id, long actor_id, String new_category_type,
                                       String log_message) {
        LocalDateTime now = LocalDateTime.now();
        if (databaseManager.postgressql_connection != null || databaseManager.mysql_connection != null) {
            if (sqlIfAccountExists(actor_id, DatabaseManager.table_accounts)) return false;

            List<Object> nickname_data = new ArrayList<>();
            nickname_data.add(new_category_type);

            List<Object> conditon_data = new ArrayList<>();
            conditon_data.add(group_id);
            conditon_data.add(category_id);

            return databaseManager.editDataSQL(DatabaseManager.table_group_category, "category_type = ?",
                    nickname_data, "group_id = ? AND category_id = ?", conditon_data) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Type");

        } else if (databaseManager.mongoClient != null && databaseManager.mongoDatabase != null) {
            if (checkIfAccountDoesntExists(databaseManager.MongoReadCollectionNoSQL(DatabaseManager.table_accounts,
                    new Document("id", actor_id), true, "name"))) return false;

            Document filter = new Document("id", group_id);

            List<Map<String, Object>> collection = databaseManager.getCollectionMongo(DatabaseManager.table_groups,
                    "categories", filter);

            if (collection == null) {
                return false;
            }

            for (int i = 0; i < collection.size(); i++) {
                Map<String, Object> cat = collection.get(i);
                if (Long.valueOf(String.valueOf(cat.get("category_id"))) == category_id) {
                    cat.put("category_type", new_category_type);
                    break;
                }
            }

            return databaseManager.MongoUpdateDocumentInCollectionNoSQL(DatabaseManager.table_groups,
                    filter, new Document("categories", collection)) &&
                    updateGroupLogs(actor_id, group_id, log_message, now, "Updated Category Type");
        }

        return false;

    }

    private boolean sqlIfAccountExists(long actor_id, String table) {
        List<Object> sender_condition = new ArrayList<>();
        sender_condition.add(actor_id);

        Map<String, List<Object>> account_data_sender = null;

        if (Objects.equals(table, DatabaseManager.table_group_roles)) {
            account_data_sender = databaseManager.getDataSQL(table,
                    "role_id", "role_id = ?",
                    sender_condition, null, "", 1);

            return account_data_sender == null || account_data_sender.isEmpty() ||
                    account_data_sender.get("role_id").isEmpty();
        } else {
            account_data_sender = databaseManager.getDataSQL(table,
                    "id", "id = ?",
                    sender_condition, null, "", 1);
            return account_data_sender == null || account_data_sender.isEmpty() ||
                    account_data_sender.get("id").isEmpty();
        }

    }

}
