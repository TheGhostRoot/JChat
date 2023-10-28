package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.*;

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

    public void createUser(String name, String email, String password, String encryption_key, String sign_key) {
        if (null == sql_connection) { return; }


        try {
            PreparedStatement account_statement = sql_connection.prepareStatement(INSERT_USER);
            account_statement.setString(1, name);
            account_statement.setString(2, email);
            account_statement.setString(3, password);
            account_statement.setString(4, encryption_key);
            account_statement.setString(5, sign_key);

            readOutput(account_statement.executeQuery());
            readOutput(sql_connection.prepareStatement(INSER_PROFILE).executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void editData(long id, String table, String to_change, Object the_change) {
        if (null == sql_connection) { return; }

        try {
            PreparedStatement account_statement = sql_connection.prepareStatement("UPDATE " + table + " SET " + to_change + " = ? WHERE id = ?;");
            switch (the_change.getClass().getSimpleName()) {
                case "String":
                    account_statement.setString(1, (String) the_change);
                    break;
                case "Integer":
                    account_statement.setInt(1, (int) the_change);
                    break;
                case "Long":
                    account_statement.setLong(1, (long) the_change);
                    break;
                case "Short":
                    account_statement.setShort(1, (short) the_change);
                    break;
                case "LocalDateTime":
                    account_statement.setObject(1, the_change);
                    break;
                default:
                    return;
            }
            account_statement.setLong(2, id);
            readOutput(account_statement.executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(long id) {
        if (null == sql_connection) { return; }

        try {
            PreparedStatement account = sql_connection.prepareStatement("DELETE FROM accounts WHERE id = ?;");
            account.setLong(1, id);

            PreparedStatement profile = sql_connection.prepareStatement("DELETE FROM profiles WHERE id = ?;");
            profile.setLong(1, id);

            PreparedStatement posts = sql_connection.prepareStatement("DELETE FROM posts WHERE sender_id = ?;");
            posts.setLong(1, id);

            PreparedStatement chat = sql_connection.prepareStatement("DELETE FROM chats WHERE id = ? OR id2 = ?;");
            chat.setLong(1, id);
            chat.setLong(2, id);

            readOutput(profile.executeQuery());
            readOutput(posts.executeQuery());
            readOutput(chat.executeQuery());
            readOutput(account.executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readOutput(ResultSet resultSet) {
        if (null == resultSet) { return; }

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                API.logger.info("Column Name: "+metaData.getColumnName(i));
            }

            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    API.logger.info("Row: "+value);
                }
                API.logger.info("\n");
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }



}
