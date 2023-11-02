package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class MySQLDB {

    private final String mysql_url = "jdbc:mysql://localhost:3306/mydb";
    private final String mysql_username = "user";
    private final String mysql_password = "password";
    private Connection mysql_connection = null;

    MySQLDB() {
        try {
            mysql_connection = DriverManager.getConnection(mysql_url, mysql_username, mysql_password);
            API.logger.info("Connected to the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void close() {
        try {
            mysql_connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
