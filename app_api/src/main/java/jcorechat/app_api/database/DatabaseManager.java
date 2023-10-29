package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {


    private final String nosql_url = "jdbc:postgresql://localhost:5432/yourdatabase";

    private final String nosql_username = "";
    private final String nosql_password = "";

    private final String sql_url = "jdbc:postgresql://localhost:5432/jcorechat-db";

    private final String sql_username = "jcorechat";
    private final String sql_password = "app_api";


    private Connection sql_connection = null;
    private Connection nosql_connection = null;


    private final String INSERT_USER = "INSERT INTO accounts (name, email, password, encryption_key, sign_key, session_id, session_expire, friends, groups) VALUES (?, ?, ?, ?, ?, NULL, NULL, '', '');";
    private final String INSER_PROFILE = "INSET INTO profiles (pfp, banner, pets, coins, badges, animations) VALUES ('', '', NULL, 0, '', NULL);";


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
    msg TEXT NOT NULL,
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

        List<Object> account_data = new ArrayList<>();
        account_data.add(name);
        account_data.add(email);
        account_data.add(password);
        account_data.add(encryption_key);
        account_data.add(sign_key);

        return addData("accounts", account_data);

    }

    private boolean editData(String table, Map<String, Object> changes, String condition, Map<String, Object> conditionData) {
        if (null == sql_connection) {
            return false;
        }

        StringBuilder updateQuery = new StringBuilder("UPDATE ");
        updateQuery.append(table).append(" SET ");

        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            updateQuery.append(entry.getKey()).append(" = ?, ");
        }

        updateQuery.setLength(updateQuery.length() - 2);
        updateQuery.append(" WHERE ").append(condition);

        try {
            List<Object> data = setData(1, changes, sql_connection.prepareStatement(updateQuery.toString()));

            ((PreparedStatement) setData((int) data.get(0), conditionData, (PreparedStatement) data.get(1)).get(1)).executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<Object> setData(int parameterIndex, Map<String, Object> changes, PreparedStatement preparedStatement)
            throws SQLException {
        List<Object> list = new ArrayList<>();

        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            Object value = entry.getValue();

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
        list.add(parameterIndex);
        list.add(preparedStatement);

        return list;
    }

    private boolean deleteData(String table, String condition, Map<String, Object> conditionData) {
        if (null == sql_connection) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ").append(table).append(" WHERE ").append(condition);
        try {
            ((PreparedStatement) setData(1, conditionData, sql_connection.prepareStatement(stringBuilder.toString())).get(1)).executeQuery();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean addData(String table, List<Object> data) {
        // "INSERT INTO accounts (name, email, password, encryption_key, sign_key, session_id, session_expire, friends, groups) VALUES (?, ?, ?, ?, ?, NULL, NULL, '', '');

        if (null == sql_connection) { return false; }

        try {

            PreparedStatement preparedStatement = sql_connection.prepareStatement("INSERT INTO " + table +
                    " (name, email, password, encryption_key, sign_key, session_id, session_expire, friends, groups) VALUES (?, ?, ?, ?, ?, NULL, NULL, '', '');");

            for (int i = 0; i < data.size(); i++) {
                Object value = data.get(i);

                switch (value.getClass().getSimpleName()) {
                    case "String":
                        preparedStatement.setString(i + 1, (String) value);
                        break;
                    case "Integer":
                        preparedStatement.setInt(i + 1, (Integer) value);
                        break;
                    case "Long":
                        preparedStatement.setLong(i + 1, (Long) value);
                        break;
                    case "Short":
                        preparedStatement.setShort(i + 1, (Short) value);
                        break;
                    case "LocalDateTime", "LocalDate":
                        preparedStatement.setObject(i + 1, value);
                        break;
                    default:
                        return false;
                }
            }

            preparedStatement.executeQuery();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean deleteUser(long id) {
        if (null == sql_connection) {
            return false;
        }

        Map<String, Object> data = new HashMap<>();

        data.put("id", id);

        if (!deleteData("accounts", "id = ?", data)) { return false; }
        if (!deleteData("profiles", "id = ?", data)) { return false; }

        data.clear();
        data.put("sender_id", id);

        if (!deleteData("posts", "sender_id = ?", data)) { return false; }

        data.clear();
        data.put("id", id);
        data.put("id2", id);
        return deleteData("chats", "id = ? OR id2 = ?", data);

    }

    public void readOutput(ResultSet resultSet) {
        if (null == resultSet) {
            return;
        }

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                API.logger.info("Column Name: " + metaData.getColumnName(i));
            }

            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    API.logger.info("Row: " + value);
                }
                API.logger.info("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
