package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {


    private final String nosql_url = "jdbc:postgresql://localhost:5432/yourdatabase";

    private final String nosql_username = "";
    private final String nosql_password = "";

    private final String sql_url = "jdbc:postgresql://localhost:5433/jcorechat-db";

    private final String sql_username = "jcorechat";
    private final String sql_password = "app_api";


    private Connection sql_connection = null;
    private Connection nosql_connection = null;


    private final String table_accounts = "accounts";
    private final String table_chats = "chats";
    private final String table_captchas = "captchas";
    private final String table_posts = "posts";
    private final String table_profiles = "profiles";
    private final String table_conversations = "conversations";

    public DatabaseManager() {
        try {
            sql_connection = DriverManager.getConnection(sql_url, sql_username, sql_password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        try {
            sql_connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
CREATE TABLE accounts (
    id bigserial PRIMARY KEY NOT NULL,
    name VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    encryption_key VARCHAR(100) UNIQUE NOT NULL,
    sign_key VARCHAR(100) UNIQUE NOT NULL,
    session_id BIGINT UNIQUE,
    session_expire smallint,
    last_edit_time TEXT,
    session_suspended VARCHAR(1) NOT NULL,
    created_at DATE NOT NULL,
    friends TEXT NOT NULL,
    groups TEXT NOT NULL
);

CREATE TABLE chats (
    conv_id BIGINT NOT NULL,
    msg VARCHAR(2000) NOT NULL,
    sent_at timestamp NOT NULL,
    sent_by BIGINT NOT NULL,
    msg_id BIGINT NOT NULL,
    FOREIGN KEY (conv_id) REFERENCES conversations(conv_id)
);

CREATE TABLE conversations (
    party_id BIGINT NOT NULL,
    party_id2 BIGINT NOT NULL,
    conv_id BIGINT PRIMARY KEY NOT NULL,
    FOREIGN KEY (party_id) REFERENCES accounts(id),
    FOREIGN KEY (party_id2) REFERENCES accounts(id)
);

CREATE TABLE captchas (
    id BIGINT PRIMARY KEY NOT NULL,
    answer TEXT NOT NULL,
    time smallint NOT NULL,
    last_edit_time TEXT NOT NULL,
    failed smallint NOT NUL
);

CREATE TABLE posts (
    id bigserial PRIMARY KEY NOT NULL,
    sender_id BIGINT NOT NULL,
    msg VARCHAR(200) NOT NULL,
    tags TEXT NOT NULL,
    send_at DATE NOT NULL,
    background TEXT,
    FOREIGN KEY (sender_id) REFERENCES accounts(id)
);

CREATE TABLE profiles (
    id BIGINT NOT NULL,
    pfp TEXT NOT NULL,
    banner TEXT NOT NULL,
    pets TEXT,
    coins INT NOT NULL,
    badges TEXT NOT NULL,
    animations TEXT,
    FOREIGN KEY (id) REFERENCES accounts(id)
);



        friends: "accountID,accountID,..."
        groups: "groupID,groupID,..."

        last_edit_time: "yyyy-MM-dd HH:mm:ss"


        
        answer: "...."

     */

    public Long createUser(String name, String email, String password, String encryption_key, String sign_key) {
        if (null == sql_connection) {
            return null;
        }

        List<Object> account_details = new ArrayList<>();
        account_details.add(name);
        account_details.add(email);
        account_details.add(password);
        account_details.add(encryption_key);
        account_details.add(sign_key);
        account_details.add(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));


        if (!addData(table_accounts,
                "name, email, password, encryption_key, sign_key, session_id, session_expire, last_edit_time, session_suspended, created_at, friends, groups",
                "?, ?, ?, ?, ?, NULL, NULL, NULL, 'f', ?, '', ''", account_details)) { return null; }


        List<Object> search_condition = new ArrayList<>();
        search_condition.add(email);

        Map<String, List<Object>> account_data = getData(table_accounts, "id",
                "email = ?", search_condition, null, "", 0);

        if (account_data == null || account_data.isEmpty()) { return null; }

        List<Object> profile_details = new ArrayList<>();
        long id = (long) account_data.get("id").get(0);
        profile_details.add(id);

        if (!addData(table_profiles, "id, pfp, banner, pets, coins, badges, animations",
                "?, 'Default Pic', 'Default Banner', NULL, 0, 'No badges', NULL", profile_details)) {
            return null;
        }

        return id;

    }

    public boolean changeUserEmail(long id, String new_email) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(new_email);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "email = ?", account_set, "id = ?", account_where);
    }

    public boolean changeUserPassword(long id, String new_password) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(new_password);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "password = ?", account_set, "id = ?", account_where);
    }

    public boolean changeUserEncryptionKey(long id, String new_encryptino_key) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(new_encryptino_key);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "encryption_key = ?", account_set, "id = ?", account_where);
    }

    public boolean changeUserSignKey(long id, String new_sign_key) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(new_sign_key);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "sign_key = ?", account_set, "id = ?", account_where);
    }

    public boolean changeUserSessionID(long id, Long session_id) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(session_id);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "session_id = ?", account_set, "id = ?", account_where);
    }

    public boolean changeUserSessionExpire(long id, short session_expire)  {
        if (isUserSessionSuspended(id)) { return false; }

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        Map<String, List<Object>> sess_data = getData(table_accounts, "last_edit_time",
                "id = ?", account_where, null, "", 0);

        if ((sess_data == null || sess_data.get("last_edit_time").isEmpty()) ||
                !isOneSecondAgo(String.valueOf(sess_data.get("last_edit_time").get(0)))) {
            return false;
        }

        List<Object> account_set = new ArrayList<>();
        account_set.add(session_expire);

        return editData(table_accounts, "session_expire = ?", account_set, "id = ?", account_where);
    }

    private boolean isOneSecondAgo(String last_edit_time) {
        try {

            if (ChronoUnit.SECONDS.between(LocalDateTime.parse((last_edit_time),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), LocalDateTime.now()) > 1) {
                return true;
            }

        } catch (Exception e) { return false; }
        return false;
    }

    public boolean changeUserSessionSuspended(long id, String stats) {
        List<Object> set_data = new ArrayList<>();
        set_data.add(stats);

        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        return editData(table_accounts, "session_suspended = ?", set_data,
                "id = ?", condition_data);
    }

    public boolean isUserSessionSuspended(long id) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);
        // session_suspended

        Map<String, List<Object>> data = getData(table_accounts, "session_suspended",
                "id = ?", condition_data, null, "", 0);


        return data != null && !data.get("session_suspended").isEmpty() &&
                data.get("session_suspended").get(0).equals("t");
    }

    public boolean addUserFriend(long id, long friend_id) {
        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        List<Object> account_friends = new ArrayList<>();
        account_friends.add("," + friend_id);

        return editData(table_accounts, "friends = friends || ?", account_friends, "id = ?",
                account_where);
    }

    public boolean removeUserFriend(long id, long friend_id) {
        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        List<Object> account_friends = new ArrayList<>();
        account_friends.add("," + friend_id);

        return editData(table_accounts, "friends = REPLACE(friends, ?, '')", account_friends,
                "id = ?", account_where);
    }

    public boolean addUserGroup(long id, long group_id) {
        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        List<Object> account_friends = new ArrayList<>();
        account_friends.add("," + group_id);

        return editData(table_accounts, "groups = groups || ?", account_friends, "id = ?",
                account_where);
    }

    public boolean removeUserGroup(long id, long group_id) {
        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        List<Object> account_friends = new ArrayList<>();
        account_friends.add("," + group_id);

        return editData(table_accounts, "groups = REPLACE(groups, ?, '')", account_friends,
                "id = ?", account_where);
    }





    public Long createConvID(long party_id, long party_id2) {
        List<Object> set_data = new ArrayList<>();
        set_data.add(party_id);
        set_data.add(party_id2);

        Map<String, List<Object>> conv_data = getData(table_conversations, "conv_id", "",
                null, null, "", 0);

        if (conv_data == null) { return null; }

        long id = generateID(conv_data.get("conv_id"));

        set_data.add(id);

        return addData(table_conversations, "party_id, party_id2, conv_id", "?, ?, ?", set_data) ?
                id : null;
    }
    public boolean addMessage(long conv_id, long sender_id, String message) {
        List<Object> edit_condition_data = new ArrayList<>();
        edit_condition_data.add(conv_id);

        Map<String, List<Object>> current_chat_data = getData(table_chats, "msg, msg_id", "conv_id = ?",
                edit_condition_data, null, "", 0);

        if (current_chat_data == null) { return false; }

        List<Object> chat_data = new ArrayList<>();
        chat_data.add(conv_id);
        chat_data.add(message);
        chat_data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        chat_data.add(sender_id);
        chat_data.add(generateID(current_chat_data.get("msg_id")));

        List<Object> set_data = new ArrayList<>();
        set_data.add(message);

        if ((current_chat_data.get("msg").isEmpty()) ||
                (!editData(table_chats, "msg = msg || ?" , set_data,
                        "conv_id = ?", edit_condition_data))) {
            return addData(table_chats, "conv_id, msg, sent_at, sent_by, msg_id",
                    "?, ?, ?, ?, ?", chat_data);

        } else {
            return true;
        }

    }

    public Map<String, List<Object>> getMessages(long sender_id, long conv_id, int amount) {
        List<Object> where_values = new ArrayList<>();
        where_values.add(conv_id);

        return getData(table_chats, "*", "conv_id = ?", where_values, null,
                "sent_at DESC", amount);
    }

    public boolean deleteMessage(long sender_id, long conv_id, long message_id) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(conv_id);
        condition_data.add(message_id);
        condition_data.add(sender_id);

        return deleteData(table_chats, "conv_id = ? AND msg_id = ? AND sent_by = ?", condition_data);
    }




    public Long startCaptcha(String answer) {
        Map<String, List<Object>> data = getData(table_captchas, "id", "",
                null, null, "", 0);


        if (data == null) { return null; }


        long id = generateID(data.get("id"));

        List<Object> values = new ArrayList<>();
        values.add(id);
        values.add(answer);
        values.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!addData(table_captchas, "id, answer, time, last_edit_time, failed", "?, ?, 10, ?, 0", values))
        { return null; }

        return id;
    }

    public boolean verifyCaptcha(long id, String given_answer) {
        // TODO when captcha is ready then update this

        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        Map<String, List<Object>> captcha_data = getData(table_captchas, "answer, time, failed",
                "id = ?", condition_data, null, "", 0);

        if (captcha_data == null) { return false; }

        if (captcha_data.get("answer").contains(given_answer)) {
            // solved!
            return deleteData(table_captchas, "id = ?", condition_data);

        } else if ((3 <= (short) captcha_data.get("failed").get(0)) ||
                (0 >= (short) captcha_data.get("time").get(0))) {
            // extended fails or time

            deleteData(table_captchas, "id = ?", condition_data);

            return false;
        } else {
            // the captcha was not solved and the user has more time and didn't failed 3 times

            if (!(captcha_data.get("time").isEmpty() || captcha_data.get("failed").isEmpty())) {
                editData(table_captchas, "failed = failed + 1", null, "id = ?",
                        condition_data);
            }
            return false;
        }
    }

    public boolean updateCaptchaTime(long id) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        Map<String, List<Object>> sess_data = getData(table_captchas, "last_edit_time",
                "id = ?", condition_data, null, "", 0);

        if ((sess_data == null || sess_data.get("last_edit_time").isEmpty()) ||
                !isOneSecondAgo(String.valueOf(sess_data.get("last_edit_time").get(0)))) {
            return false;
        }

        return editData(table_captchas, "time = time - 1", null, "id = ?", condition_data);
    }





    public boolean createPost(long sender_id, String msg, String tags, String background) {
        // there is no custom background if empty
        List<Object> data = new ArrayList<>();
        data.add(sender_id);
        data.add(msg);
        data.add(tags);
        data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        data.add(background);

        return addData(table_posts, "sender_id, msg, tags, send_at, background", "?, ?, ?, ?, ?", data);
    }

    public boolean deletePost(long sender_id, long post_id) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(post_id);
        condition_data.add(sender_id);

        return deleteData(table_posts, "id = ? AND sender_id = ?", condition_data);
    }

    public boolean editPost(long sender_id, long post_id, String edited_tags,
                            String edited_msg, String given_background) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(post_id);
        condition_data.add(sender_id);

        List<Object> set_data = new ArrayList<>();
        set_data.add(edited_msg);
        set_data.add(edited_tags);
        set_data.add(given_background);

        return editData(table_posts, "msg = ?, tags = ?, background = ?", set_data,
                "id = ? AND sender_id = ?", condition_data);
    }




    public boolean updateProfilePfp(long id, String given_pfp) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_pfp);

        return editData(table_profiles, "pfp = ?", profile_data, "id = ?", condition_data);
    }

    public boolean updateProfileBanner(long id, String given_banner) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_banner);

        return editData(table_profiles, "banner = ?", profile_data, "id = ?", condition_data);
    }

    public boolean updateProfilePets(long id, String given_pets) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_pets);

        return editData(table_profiles, "pets = ?", profile_data, "id = ?", condition_data);
    }

    public boolean updateProfileCoins(long id, int given_coins) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_coins);

        return editData(table_profiles, "coins = ?", profile_data, "id = ?", condition_data);
    }

    public boolean updateProfileBadges(long id, String given_badges) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_badges);

        return editData(table_profiles, "badges = ?", profile_data, "id = ?", condition_data);
    }

    public boolean updateProfileAnimations(long id, String given_animations) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(id);

        List<Object> profile_data = new ArrayList<>();
        profile_data.add(given_animations);

        return editData(table_profiles, "animations = ?", profile_data, "id = ?", condition_data);
    }





    @Deprecated
    public boolean deleteUser(long id) {
        if (null == sql_connection) {
            return false;
        }

        List<Object> data = new ArrayList<>();

        data.add(id);

        if (!deleteData(table_accounts, "id = ?", data)) { return false; }
        if (!deleteData(table_profiles, "id = ?", data)) { return false; }

        data.clear();
        data.add(id);

        if (!deleteData(table_posts, "sender_id = ?", data)) { return false; }

        data.clear();
        data.add(id);
        data.add(id);
        return deleteData(table_chats, "id = ? OR id2 = ?", data);

    }

    private long generateID(List<Object> toContain) {
        long id = 1L;
        while (toContain.contains(id)) {
            id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return id;
    }

    private List<Object> setData(short parameterIndex, List<Object> changes, PreparedStatement preparedStatement)
            throws SQLException {
        List<Object> list = new ArrayList<>();

        if (null != changes) {
            for (Object value : changes) {
                switch (value.getClass().getSimpleName()) {
                    case "String":
                        preparedStatement.setString(parameterIndex, (String) value);
                        break;
                    case "Integer":
                        preparedStatement.setInt(parameterIndex, (Integer) value);
                        break;
                    case "Long":
                        preparedStatement.setLong(parameterIndex, (Long) value);
                        break;
                    case "Short":
                        preparedStatement.setShort(parameterIndex, (Short) value);
                        break;
                    case "LocalDateTime", "LocalDate":
                        preparedStatement.setObject(parameterIndex, value);
                        break;
                    default:
                        return null;
                }

                parameterIndex++;
            }
        }
        list.add(parameterIndex);
        list.add(preparedStatement);

        return list;
    }

    private boolean deleteData(String table, String condition, List<Object> conditionData) {
        if (null == sql_connection) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ").append(table).append(" WHERE ").append(condition);
        try {
            ((PreparedStatement) setData((short) 1, conditionData,
                    sql_connection.prepareStatement(stringBuilder.toString())).get(1)).executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean addData(String table, String fields, String values, List<Object> data) {
        // "INSERT INTO accounts (name, email, password, encryption_key, sign_key, session_id, session_expire, friends, groups) VALUES (?, ?, ?, ?, ?, NULL, NULL, '', '');

        if (null == sql_connection) { return false; }

        try {

            ( (PreparedStatement) setData((short) 1, data, sql_connection.prepareStatement(new StringBuilder("INSERT INTO ")
                    .append(table).append(" (").append(fields).append(") VALUES (").append(values).append(");")
                    .toString())).get(1) ).executeUpdate();

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private boolean editData(String table, String set_expression, List<Object> set_data, String condition,
                             List<Object> conditionData) {
        if (null == sql_connection) {
            return false;
        }

        StringBuilder updateQuery = new StringBuilder("UPDATE ").append(table).append(" SET ").append(set_expression)
                .append(" WHERE ").append(condition).append(";");

        try {
            List<Object> data = setData((short) 1, set_data, sql_connection.prepareStatement(new StringBuilder("UPDATE ").append(table).append(" SET ").append(set_expression)
                    .append(" WHERE ").append(condition).append(";").toString()));

            ((PreparedStatement) setData((short) data.get(0), conditionData, (PreparedStatement) data.get(1)).get(1))
                    .executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, List<Object>> getData(String table, String data_to_get, String condition,
                                              List<Object> conditionData, Map<String, List<Object>> join_data,
                                              String order, int limit) {
        // conditionData can be null if there is no condition
        if (null == sql_connection) {
            return null;
        }

        StringBuilder select_query = new StringBuilder("SELECT ").append(data_to_get).append(" FROM ").append(table);

        // join_data ->  key = table name ; value = index 0 -> condition  | the rest is data for condition
        if (null != join_data && !join_data.isEmpty()) {
            for (Map.Entry<String, List<Object>> entry : join_data.entrySet()) {
                List<Object> value = entry.getValue();
                select_query.append(" JOIN ").append(entry.getKey()).append(" ON ")
                        .append(String.valueOf(value.get(0)));
                value.remove(0);
                entry.setValue(value);
            }
        }

        if (!condition.isBlank()) { select_query.append(" WHERE ").append(condition); }

        if (!order.isBlank()) { select_query.append(" ORDER BY ").append(order); }

        if (0 < limit) { select_query.append("LIMIT ").append(limit); }

        try {

            // We use Java 17 so the hashmap is ordered, and we assume that it is.

            short i = 1;

            PreparedStatement preparedStatement = sql_connection.prepareStatement(select_query.append(";").toString());

            if (null != join_data && !join_data.isEmpty()) {
                for (Map.Entry<String, List<Object>> entry : join_data.entrySet()) {
                    List<Object> data_list = setData(i, entry.getValue(), preparedStatement);
                    i = (short) data_list.get(0);
                    preparedStatement = (PreparedStatement) data_list.get(1);
                }
            }

            return readOutput(((PreparedStatement) setData(i, conditionData, preparedStatement).get(1)).executeQuery());
        } catch (Exception e) {
            return null;
        }

    }

    @Deprecated
    public boolean createTable(String table, List<String> colums) {
        if (null == sql_connection) { return false; }

        try {

            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ").append(table).append(" ( ");

            for (String col : colums) {
                stringBuilder.append(col);
            }

            readOutput(sql_connection.prepareStatement(stringBuilder.append(" );").toString()).executeQuery());
            return true;
        } catch (Exception  e) {
            return false;
        }
    }

    @Deprecated
    public boolean deleteTable(String table) {
        if (null == sql_connection) { return false; }

        try {
            StringBuilder stringBuilder = new StringBuilder("DROP TABLE ").append(table).append(";");

            sql_connection.prepareStatement(stringBuilder.toString()).executeQuery();
            return true;
        } catch (Exception  e) {
            return false;
        }
    }

    private Map<String, List<Object>> readOutput(ResultSet resultSet) {
        Map<String, List<Object>> result = new HashMap<>();

        if (null == resultSet) {
            return null;
        }

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                result.put(metaData.getColumnName(i), new ArrayList<>());
            }

            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.get(metaData.getColumnName(i)).add(resultSet.getObject(i));
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }


}
