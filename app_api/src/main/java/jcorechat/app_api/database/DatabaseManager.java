package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    created_at DATE NOT NULL,
    friends TEXT NOT NULL,
    groups TEXT NOT NULL
);

CREATE TABLE chats (
    id BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    msg VARCHAR(2000) NOT NULL,
    sent_at timestamp NOT NULL,
    sent_by BIGINT NOT NULL,
    FOREIGN KEY (id) REFERENCES accounts(id),
    FOREIGN KEY (id2) REFERENCES accounts(id)
);

CREATE TABLE captchas (
    id bigserial PRIMARY KEY NOT NULL,
    answer TEXT NOT NULL,
    time smallint NOT NULL
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

        chats -> msgs: "[msg]{accountID},[msg]{accountID},..."
        
        answer: "[ans1],[ans2]..."

     */

    public boolean createUser(String name, String email, String password, String encryption_key, String sign_key) {
        if (null == sql_connection) {
            return false;
        }

        List<Object> account_details = new ArrayList<>();
        account_details.add(name);
        account_details.add(email);
        account_details.add(password);
        account_details.add(encryption_key);
        account_details.add(sign_key);
        account_details.add(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));


        if (!addData(table_accounts,
                "name, email, password, encryption_key, sign_key, session_id, session_expire, created_at, friends, groups",
                "?, ?, ?, ?, ?, NULL, NULL, ?, '', ''", account_details)) { return false; }


        List<Object> search_condition = new ArrayList<>();
        search_condition.add(email);

        Map<String, List<Object>> account_data = getData(table_accounts, "id",
                "email = ?", search_condition, null, "", 0);

        if (account_data == null || account_data.isEmpty()) { return false; }

        List<Object> profile_details = new ArrayList<>();
        profile_details.add((long) account_data.get("id").get(0));

        if (!addData(table_profiles, "id, pfp, banner, pets, coins, badges, animations",
                "?, 'Default Pic', 'Default Banner', NULL, 0, 'No badges', NULL", profile_details)) { return false; }

        return true;

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

    public boolean changeUserSessionExpire(long id, short session_expire) {
        List<Object> account_set = new ArrayList<>();
        account_set.add(session_expire);

        List<Object> account_where = new ArrayList<>();
        account_where.add(id);

        return editData(table_accounts, "session_expire = ?", account_set, "id = ?", account_where);
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





    public boolean addMessage(long sender_id, long resiver_id, String message) {

        List<Object> where_values = new ArrayList<>();
        where_values.add(sender_id);
        where_values.add(resiver_id);
        where_values.add(resiver_id);
        where_values.add(sender_id);

        Map<String, List<Object>> chat_data = getData(table_chats, "id, id2",
                "(id = ? AND id2 = ?) OR (id = ? AND id2 = ?)", where_values, null, "", 0);

        if (chat_data == null || chat_data.values().stream().allMatch(List::isEmpty)) {
            // create the chat
            List<Object> values = new ArrayList<>();
            values.add(sender_id);
            values.add(resiver_id);
            values.add(message);
            values.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            values.add(sender_id);

            if (!addData(table_chats, "id, id2, msg, sent_at, sent_by", "?, ?, ?, ?, ?", values)) {
                return false;
            }
        }

        List<Object> set_data = new ArrayList<>();
        set_data.add(message);

        return editData(table_chats, "msg = msg || ?", set_data,
                "(id = ? AND id2 = ?) OR (id = ? AND id2 = ?)", where_values);
    }

    public List<String> getMessages(long sender_id, long resiver_id, int amount) {
        // index 0 -> message
        // index 1 -> sender_id
        List<Object> where_values = new ArrayList<>();
        where_values.add(sender_id);
        where_values.add(resiver_id);
        where_values.add(resiver_id);
        where_values.add(sender_id);

        Map<String, List<Object>> data = getData(table_chats, "msg",
                "(id = ? AND id2 = ?) OR (id = ? AND id2 = ?)", where_values, null,
                "sent_at DESC", amount);

        return data == null ? null : data.get("msg").stream().map(String::valueOf).collect(Collectors.toList());
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
            List<Object> data = setData((short) 1, set_data, sql_connection.prepareStatement(updateQuery.toString()));

            ((PreparedStatement) setData((short) data.get(0), conditionData, (PreparedStatement) data.get(1)).get(1))
                    .executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
