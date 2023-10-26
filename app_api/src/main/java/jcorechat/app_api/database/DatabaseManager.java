package jcorechat.app_api.database;

import jcorechat.app_api.API;

import java.sql.*;

public class DatabaseManager {


    private final String nosql_url = "jdbc:postgresql://localhost:5432/yourdatabase";

    private final String nosql_username = "";
    private final String nosql_password = "";

    private final String sql_url = "jdbc:postgresql://localhost:5433/postgres";

    private final String sql_username = "jcorechat";
    private final String sql_password = "app_api";



    private Connection sql_connection = null;
    private Connection nosql_connection = null;


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
            id BIGINT PRIMARY KEY NOT NULL,
            name VARCHAR(20) UNIQUE NOT NULL,
            email VARCHAR(50) UNIQUE NOT NULL,
            password VARCHAR(100) NOT NULL,
            encryption_key VARCHAR(100) UNIQUE NOT NULL,
            sign_key VARCHAR(100) UNIQUE NOT NULL,
            session_id BIGINT UNIQUE,
            friends TEXT NOT NULL,
            groups TEXT NOT NULL
        );


        friends: accountID,accountID,..
        groups: groupID,groupID,...

     */

    public void createUser() {
        if (null == sql_connection) { return; }

        try {
            readOutput(sql_connection.prepareStatement("").executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readOutput(ResultSet resultSet) {
        if (null == resultSet) { return; }

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print rows
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
