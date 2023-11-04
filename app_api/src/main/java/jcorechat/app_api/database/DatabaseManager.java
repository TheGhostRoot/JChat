package jcorechat.app_api.database;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.mongodb.client.*;
import jcorechat.app_api.API;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatabaseManager {

    protected static final String table_accounts = "accounts";
    protected static final String table_chats = "chats";
    protected static final String table_captchas = "captchas";
    protected static final String table_posts = "posts";
    protected static final String table_profiles = "profiles";
    protected static final String table_conversations = "conversations";


    private final String postgressql_url = "jdbc:postgresql://localhost:5433/jcorechat-db";
    private final String postgressql_username = "jcorechat";
    private final String postgressql_password = "app_api";
    private Connection postgressql_connection = null;


    private final String mysql_url = "jdbc:mysql://localhost:3306/jcorechat";
    private final String mysql_username = "jcorechat";
    private final String mysql_password = "JCCpwd123";
    public Connection mysql_connection = null;




    private final String mongo_database = "jcorechat";
    private final String mongo_url = "mongodb://localhost:27017";
    private MongoDatabase mongoDatabase = null;
    private MongoClient mongoClient = null;




    private final String scylladb_host = "129.152.4.113";

    private final int scylladb_port = 9042;

    private final String scylladb_datacenter = "datacenter1";

    private CqlSession scylladb_session = null;



    public void setupPostgresSQL() {
        try {
            postgressql_connection = DriverManager.getConnection(postgressql_url, postgressql_username, postgressql_password);

            List<String> accounts_table = new ArrayList<>();
            accounts_table.add("id bigserial PRIMARY KEY NOT NULL, ");
            accounts_table.add("name VARCHAR(20) UNIQUE NOT NULL, ");
            accounts_table.add("email VARCHAR(50) UNIQUE NOT NULL, ");
            accounts_table.add("password VARCHAR(100) NOT NULL, ");
            accounts_table.add("encryption_key VARCHAR(100) UNIQUE NOT NULL, ");
            accounts_table.add("sign_key VARCHAR(100) UNIQUE NOT NULL, ");
            accounts_table.add("session_id BIGINT UNIQUE, ");
            accounts_table.add("session_expire smallint, ");
            accounts_table.add("last_edit_time timestamp, ");
            accounts_table.add("session_suspended VARCHAR(1) NOT NULL, ");
            accounts_table.add("created_at timestamp NOT NULL, ");
            accounts_table.add("friends TEXT NOT NULL, ");
            accounts_table.add("chat_groups TEXT NOT NULL ");

            List<String> conversations_table = new ArrayList<>();
            conversations_table.add("party_id BIGINT NOT NULL, ");
            conversations_table.add("party_id2 BIGINT NOT NULL, ");
            conversations_table.add("conv_id BIGINT PRIMARY KEY NOT NULL, ");
            conversations_table.add("FOREIGN KEY (party_id) REFERENCES accounts(id), ");
            conversations_table.add("FOREIGN KEY (party_id2) REFERENCES accounts(id)");


            List<String> chats_table = new ArrayList<>();
            chats_table.add("conv_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("sent_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL, ");
            chats_table.add("FOREIGN KEY (conv_id) REFERENCES conversations(conv_id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT PRIMARY KEY NOT NULL, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time smallint NOT NULL, ");
            captchas_table.add("last_edit_time TEXT NOT NULL, ");
            captchas_table.add("failed smallint NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id bigserial PRIMARY KEY NOT NULL, ");
            posts_table.add("sender_id BIGINT NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("tags VARCHAT(100) NOT NULL, ");
            posts_table.add("send_at timestamp NOT NULL, ");
            posts_table.add("background TEXT, ");
            posts_table.add("FOREIGN KEY (sender_id) REFERENCES accounts(id)");

            List<String> profiles_table = new ArrayList<>();
            profiles_table.add("id BIGINT NOT NULL, ");
            profiles_table.add("pfp TEXT NOT NULL, ");
            profiles_table.add("banner TEXT NOT NULL, ");
            profiles_table.add("pets TEXT, ");
            profiles_table.add("coins INT NOT NULL, ");
            profiles_table.add("badges TEXT NOT NULL, ");
            profiles_table.add("animations TEXT, ");
            profiles_table.add("FOREIGN KEY (id) REFERENCES accounts(id)");

            deleteTableSQL("profiles");
            deleteTableSQL("posts");
            deleteTableSQL("chats");
            deleteTableSQL("captchas");
            deleteTableSQL("conversations");
            deleteTableSQL("accounts");

            createTableSQL("accounts", accounts_table);
            createTableSQL( "conversations", conversations_table);
            createTableSQL( "captchas", captchas_table);
            createTableSQL( "profiles", profiles_table);
            createTableSQL( "posts", posts_table);
            createTableSQL( "chats", chats_table);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupMySQL() {
        try {
            mysql_connection = DriverManager.getConnection(mysql_url, mysql_username, mysql_password);

            List<String> accounts_table = new ArrayList<>();
            accounts_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            accounts_table.add("name VARCHAR(20) UNIQUE NOT NULL, ");
            accounts_table.add("email VARCHAR(50) UNIQUE NOT NULL, ");
            accounts_table.add("password VARCHAR(100) NOT NULL, ");
            accounts_table.add("encryption_key VARCHAR(100) UNIQUE NOT NULL, ");
            accounts_table.add("sign_key VARCHAR(100) UNIQUE NOT NULL, ");
            accounts_table.add("session_id BIGINT UNIQUE, ");
            accounts_table.add("session_expire SMALLINT, ");
            accounts_table.add("last_edit_time TIMESTAMP NULL, ");
            accounts_table.add("session_suspended CHAR(1) NOT NULL, ");
            accounts_table.add("created_at TIMESTAMP NOT NULL, ");
            accounts_table.add("friends LONGTEXT NOT NULL, ");
            accounts_table.add("chat_groups LONGTEXT NOT NULL ");

            List<String> conversations_table = new ArrayList<>();
            conversations_table.add("party_id BIGINT NOT NULL, ");
            conversations_table.add("party_id2 BIGINT NOT NULL, ");
            conversations_table.add("conv_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            conversations_table.add("FOREIGN KEY (party_id) REFERENCES accounts(id), ");
            conversations_table.add("FOREIGN KEY (party_id2) REFERENCES accounts(id)");

            List<String> chats_table = new ArrayList<>();
            chats_table.add("conv_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("sent_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL, ");
            chats_table.add("FOREIGN KEY (conv_id) REFERENCES conversations(conv_id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time SMALLINT NOT NULL, ");
            captchas_table.add("last_edit_time TIMESTAMP NOT NULL, ");
            captchas_table.add("failed SMALLINT NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_table.add("sender_id BIGINT NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("tags VARCHAR(100) NOT NULL, ");
            posts_table.add("send_at TIMESTAMP NOT NULL, ");
            posts_table.add("background TEXT NULL, ");
            posts_table.add("FOREIGN KEY (sender_id) REFERENCES accounts(id)");

            List<String> profiles_table = new ArrayList<>();
            profiles_table.add("id BIGINT NOT NULL, ");
            profiles_table.add("pfp TEXT NOT NULL, ");
            profiles_table.add("banner TEXT NOT NULL, ");
            profiles_table.add("pets TEXT NULL, ");
            profiles_table.add("coins INT NOT NULL, ");
            profiles_table.add("badges TEXT NOT NULL, ");
            profiles_table.add("animations TEXT NULL, ");
            profiles_table.add("FOREIGN KEY (id) REFERENCES accounts(id)");

            deleteTableSQL( "profiles");
            deleteTableSQL( "posts");
            deleteTableSQL( "chats");
            deleteTableSQL( "captchas");
            deleteTableSQL( "conversations");
            deleteTableSQL( "accounts");

            createTableSQL( "accounts", accounts_table);
            createTableSQL( "conversations", conversations_table);
            createTableSQL( "captchas", captchas_table);
            createTableSQL( "profiles", profiles_table);
            createTableSQL( "posts", posts_table);
            createTableSQL( "chats", chats_table);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupMongoDB() {
        try {
            mongoClient = MongoClients.create(mongo_url);
            mongoDatabase = mongoClient.getDatabase(mongo_database);

            MongoDeleteCollectionNoSQL(table_accounts);
            MongoDeleteCollectionNoSQL(table_conversations);
            MongoDeleteCollectionNoSQL(table_profiles);
            MongoDeleteCollectionNoSQL(table_captchas);
            MongoDeleteCollectionNoSQL(table_chats);
            MongoDeleteCollectionNoSQL(table_posts);

            MongoCreateCollectionNoSQL(table_accounts);
            MongoCreateCollectionNoSQL(table_conversations);
            MongoCreateCollectionNoSQL(table_profiles);
            MongoCreateCollectionNoSQL(table_captchas);
            MongoCreateCollectionNoSQL(table_chats);
            MongoCreateCollectionNoSQL(table_posts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupScyllaDB() {
        try {
            scylladb_session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(scylladb_host, scylladb_port))
                    .withLocalDatacenter(scylladb_datacenter).build();
            // Execute a simple query
            com.datastax.oss.driver.api.core.cql.ResultSet resultSet = scylladb_session.execute("SELECT * FROM system.local");

            // Process the result
            Row row = resultSet.one();
            if (row != null) {
                API.logger.info("ScyllaDB Release Version: " + row.getString("release_version"));
            }

            scylladb_session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        try {
            if (postgressql_connection != null) {
                postgressql_connection.close();

            } else if (mysql_connection != null) {
                mysql_connection.close();

            } else if (mongoClient != null) {
                mongoClient.close();

            } else if (scylladb_session != null) {
                scylladb_session.close();

            }
        } catch (Exception e) {}
    }


    @Deprecated
    private boolean createTableSQL(String table, List<String> colums) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {

            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ").append(table).append(" ( ");

            for (String col : colums) {
                stringBuilder.append(col);
            }

            getSQLConnection().prepareStatement(stringBuilder.append(" );").toString()).executeUpdate();
            return true;
        } catch (Exception  e) {
            return false;
        }
    }
    @Deprecated
    private boolean deleteTableSQL(String table) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {
            StringBuilder stringBuilder = new StringBuilder("DROP TABLE ").append(table).append(";");

            getSQLConnection().prepareStatement(stringBuilder.toString()).executeUpdate();
            return true;
        } catch (Exception  e) {
            return false;
        }
    }




    @Deprecated
    private boolean MongoCreateCollectionNoSQL(String collectionName) {
        if (mongoDatabase == null) {
            return false;
        }

        try {
            mongoDatabase.createCollection(collectionName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Deprecated
    private boolean MongoDeleteCollectionNoSQL(String collectionName) {
        if (mongoDatabase == null) {
            return false;
        }

        try {

            mongoDatabase.getCollection(collectionName).drop();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private List<Map<String, Object>> MongoReadCollectionNoSQL(String collectionName, Document condition,
                                                               String... filters) {
        if (mongoDatabase == null) {
            return null;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        List<String> filter_list = Arrays.stream(filters).toList();

        try {
            MongoCursor<Document> cursor = mongoDatabase.getCollection(collectionName).find().iterator();

            while (cursor.hasNext()) {
                Document next = cursor.next();
                if (filters.length > 0) {
                    for (Map.Entry<String, Object> entry : next.entrySet()) {
                        if (filter_list.contains(entry.getKey())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(entry.getKey(), entry.getValue());
                            result = conditionFilter(condition, result, next);
                        }
                    }
                } else {
                    result = conditionFilter(condition, result, next);
                }
            }

            cursor.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> conditionFilter(Document condition, List<Map<String, Object>> result, Document next) {
        if (condition != null) {
            boolean contains = false;
            for (Map.Entry<String, Object> entry2 : condition.entrySet()) {
                String key = entry2.getKey();
                if (!next.containsKey(key) ||
                        !next.get(key).equals(entry2.getValue())) {
                    contains = true;
                } else {
                    contains = false;
                }
            }

            if (contains) {
                result.add(new HashMap<>(next));
            }
        } else {
            result.add(new HashMap<>(next));
        }

        return result;
    }

    private boolean MongoAddDataToCollectionNoSQL(String collectionName, Document document) {
        if (mongoDatabase == null) {
            return false;
        }

        try {
            mongoDatabase.getCollection(collectionName).insertOne(document);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean MongoUpdateDocumentInCollectionNoSQL(String collectionName, Document filter, Document updatedDoc) {
        if (mongoDatabase == null) {
            return false;
        }

        try {
            mongoDatabase.getCollection(collectionName).updateOne(filter, new Document("$set", updatedDoc));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean MongoDeleteDataFromCollectionNoSQL(String collectionName, Document filter) {
        if (mongoDatabase == null) {
            return false;
        }

        try {
            mongoDatabase.getCollection(collectionName).deleteMany(filter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }






    private Map<String, List<Object>> readOutputSQL(ResultSet resultSet) {
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
    private List<Object> setDataSQL(short parameterIndex, List<Object> changes, PreparedStatement preparedStatement)
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
                    case "LocalDateTime":
                        preparedStatement.setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) value));
                        break;
                    default:
                        preparedStatement.setObject(parameterIndex, value);
                        break;
                }

                parameterIndex++;
            }
        }
        list.add(parameterIndex);
        list.add(preparedStatement);

        return list;
    }
    private boolean deleteDataSQL(String table, String condition, List<Object> conditionData) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ").append(table).append(" WHERE ").append(condition);
        try {
            ((PreparedStatement) setDataSQL((short) 1, conditionData,
                    getSQLConnection().prepareStatement(stringBuilder.toString())).get(1)).executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean addDataSQL(String table, String fields, String values, List<Object> data) {

        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {

            ( (PreparedStatement) setDataSQL((short) 1, data, getSQLConnection()
                    .prepareStatement(new StringBuilder("INSERT INTO ")
                    .append(table).append(" (").append(fields).append(") VALUES (").append(values).append(");")
                    .toString())).get(1) ).executeUpdate();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    private boolean editDataSQL(String table, String set_expression,
                                      List<Object> set_data, String condition, List<Object> conditionData) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {
            List<Object> data = setDataSQL((short) 1, set_data, getSQLConnection()
                    .prepareStatement(new StringBuilder("UPDATE ").append(table).append(" SET ").append(set_expression)
                            .append(" WHERE ").append(condition).append(";").toString()));

            ((PreparedStatement) setDataSQL((short) data.get(0), conditionData, (PreparedStatement) data.get(1)).get(1))
                    .executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private Map<String, List<Object>> getDataSQL(String table, String data_to_get, String condition,
                                              List<Object> conditionData, Map<String, List<Object>> join_data,
                                              String order, int limit) {

        if (postgressql_connection == null && mysql_connection == null) {
            return null;
        }

        StringBuilder select_query = new StringBuilder("SELECT ").append(data_to_get).append(" FROM ").append(table);

        // join_data ->  key = table name ; value = index 0 -> condition  | the rest is data for condition
        if (null != join_data && !join_data.isEmpty()) {
            for (Map.Entry<String, List<Object>> entry : join_data.entrySet()) {
                List<Object> value = entry.getValue();
                select_query.append(" JOIN ").append(entry.getKey()).append(" ON ")
                        .append(value.get(0));
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

            PreparedStatement preparedStatement = getSQLConnection().prepareStatement(select_query.append(";")
                    .toString());

            if (null != join_data && !join_data.isEmpty()) {
                for (Map.Entry<String, List<Object>> entry : join_data.entrySet()) {
                    List<Object> data_list = setDataSQL(i, entry.getValue(), preparedStatement);
                    i = (short) data_list.get(0);
                    preparedStatement = (PreparedStatement) data_list.get(1);
                }
            }

            return readOutputSQL(((PreparedStatement) setDataSQL(i, conditionData, preparedStatement).get(1)).executeQuery());
        } catch (Exception e) {
            return null;
        }

    }




    private long generateID(List<Object> toContain) {
        long id = 1L;
        while (toContain.contains(id)) {
            id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return id;
    }

    public boolean isOneSecondAgo(Timestamp last_edit_time) {
        try {

            return Duration.between(last_edit_time.toInstant(),
                    new Timestamp(System.currentTimeMillis()).toInstant()).toMillis() > 1000;

        } catch (Exception e) { return false; }

    }

    private Connection getSQLConnection() {
        return postgressql_connection != null ? postgressql_connection :
                mysql_connection;
    }









    public Long createUser(String name, String email, String password, String encryption_key, String sign_key) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_details = new ArrayList<>();
            account_details.add(name);
            account_details.add(email);
            account_details.add(password);
            account_details.add(encryption_key);
            account_details.add(sign_key);
            account_details.add(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));

            if (!addDataSQL(table_accounts,
                        "name, email, password, encryption_key, sign_key, session_id, session_expire, last_edit_time, session_suspended, created_at, friends, chat_groups",
                        "?, ?, ?, ?, ?, NULL, NULL, NULL, 'f', ?, '', ''", account_details)) {
                return null;
            }


            List<Object> search_condition = new ArrayList<>();
            search_condition.add(email);

            Map<String, List<Object>> account_data = getDataSQL(
                    table_accounts, "id",
                    "email = ?", search_condition, null, "", 0);

            if (account_data == null || account_data.isEmpty()) {
                return null;
            }

            List<Object> profile_details = new ArrayList<>();
            long id = (long) account_data.get("id").get(0);
            profile_details.add(id);

            if (!addDataSQL(table_profiles, "id, pfp, banner, pets, coins, badges, animations",
                    "?, 'Default Pic', 'Default Banner', NULL, 0, 'No badges', NULL", profile_details)) {

                deleteDataSQL(table_accounts, "id = ?", profile_details);
                return null;
            }

            return id;

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Object> all_ids = new ArrayList<>();

            List<Map<String, Object>> accounts_id_data = MongoReadCollectionNoSQL(table_accounts, null, "id");
            if (accounts_id_data == null) {
                return null;
            }

            for (Map<String, Object> map : accounts_id_data) {
                all_ids.addAll(map.values());
            }

            long account_ID = generateID(all_ids);

            if (!MongoAddDataToCollectionNoSQL(table_accounts, new Document("id", account_ID).append("name", name)
                    .append("email", email).append("password", password)
                    .append("encryption_key", encryption_key).append("sign_key", sign_key)
                    .append("session_id", null).append("session_expire", null).append("last_edit_time", null)
                    .append("session_suspended", "f")
                    .append("created_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append("friends", "").append("chat_groups", ""))) {

                return null;
            }

            if (!MongoAddDataToCollectionNoSQL(table_profiles, new Document())) {

                MongoDeleteDataFromCollectionNoSQL(table_accounts, new Document("id", account_ID));
                return null;
            }


            return account_ID;

        } else if (scylladb_session != null) {
            return null;

        }
        return null;
    }
    public boolean changeUserEmail(long id, String new_email) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_email);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return editDataSQL(table_accounts, "email = ?", account_set,
                    "id = ?", account_where);

        } else if (mongoClient != null && mongoDatabase != null) {

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, new Document("id", id),
                    new Document("email", new_email));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean changeUserPassword(long id, String new_password) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_password);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return editDataSQL(table_accounts, "password = ?", account_set,
                    "id = ?", account_where);

        } else if (mongoClient != null && mongoDatabase != null) {

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, new Document("id", id),
                    new Document("password", new_password));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean changeUserEncryptionKey(long id, String new_encryptino_key) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_encryptino_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return editDataSQL(table_accounts, "encryption_key = ?", account_set,
                    "id = ?", account_where);
        } else if (mongoClient != null && mongoDatabase != null) {

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, new Document("id", id),
                    new Document("encryption_key", new_encryptino_key));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean changeUserSignKey(long id, String new_sign_key) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(new_sign_key);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return editDataSQL(table_accounts, "sign_key = ?", account_set,
                    "id = ?", account_where);
        } else if (mongoClient != null && mongoDatabase != null) {

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, new Document("id", id),
                    new Document("sign_key", new_sign_key));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean changeUserSessionID(long id, long session_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_set = new ArrayList<>();
            account_set.add(session_id);

            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            return editDataSQL(table_accounts, "session_id = ?", account_set,
                    "id = ?", account_where);

        } else if (mongoClient != null && mongoDatabase != null) {

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, new Document("id", id),
                    new Document("session_id", session_id));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateUserSessionExpire(long id)  {
        if (isUserSessionSuspended(id)) { return false; }

        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            Map<String, List<Object>> sess_data = getDataSQL(table_accounts, "last_edit_time, session_expire",
                    "id = ?", account_where, null, "", 0);

            if (sess_data == null || (sess_data.get("last_edit_time").isEmpty() ||
                    Objects.equals(String.valueOf(sess_data.get("last_edit_time").get(0)), "null")) ||
                    (sess_data.get("session_expire").isEmpty() ||
                    Objects.equals(String.valueOf(sess_data.get("session_expire").get(0)), "null")) ||
                    !isOneSecondAgo(Timestamp.valueOf(String.valueOf(sess_data.get("last_edit_time").get(0))))) {

                return false;
            }

            if (((short) sess_data.get("session_expire").get(0)) <= 0) {
                // session expired. END IT
                return editDataSQL(table_accounts,
                        "session_expire = NULL, session_id = NULL, last_edit_time = NULL",
                        null, "id = ?", account_where);
            }

            List<Object> account_set = new ArrayList<>();
            account_set.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return editDataSQL(table_accounts,
                    "last_edit_time = ?, session_expire = session_expire - 1", account_set,
                    "id = ?", account_where);

        } else if (mongoClient != null && mongoDatabase != null) {
            Document filter = new Document("id", id);

            List<Map<String, Object>> account_data = MongoReadCollectionNoSQL(table_accounts, filter,
                    "last_edit_time", "session_expire");

            if (account_data == null || account_data.get(0).get("last_edit_time") == null ||
                    account_data.get(0).get("session_expire") == null ||
                    !isOneSecondAgo(Timestamp.valueOf(String.valueOf(account_data.get(0).get("last_edit_time"))))) {

                return false;
            }

            Map<String, Object> data = account_data.get(0);

            short sessionExpire = Short.valueOf(String.valueOf(data.get("session_expire")));

            if (sessionExpire <= 0) {
                // remove the session
                return MongoUpdateDocumentInCollectionNoSQL(table_accounts, filter,
                        new Document("session_expire", null).append("last_edit_time", null)
                                .append("session_id", null));
            }

            sessionExpire--;

            return MongoUpdateDocumentInCollectionNoSQL(table_accounts, filter,
                    new Document("session_expire", sessionExpire)
                            .append("last_edit_time",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean changeUserSessionSuspended(long id, String stats) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(stats);

            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            return editDataSQL(table_accounts,
                    "session_suspended = ?", set_data,
                    "id = ?", condition_data);

        }  else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    private boolean isUserSessionSuspended(long id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);
            // session_suspended

            Map<String, List<Object>> data = getDataSQL(table_accounts, "session_suspended",
                    "id = ?", condition_data, null, "", 0);


            return data != null && !data.get("session_suspended").isEmpty() &&
                    data.get("session_suspended").get(0).equals("t");
        }  else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean addUserFriend(long id, long friend_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + friend_id);

            return editDataSQL(table_accounts,
                    postgressql_connection != null ? "friends = friends || ?" : "friends = CONCAT(friends, ?)",
                    account_friends, "id = ?", account_where);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean removeUserFriend(long id, long friend_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + friend_id);

            return editDataSQL(table_accounts,
                    "friends = REPLACE(friends, ?, '')", account_friends,"id = ?",
                    account_where);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean addUserGroup(long id, long group_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + group_id);

            return editDataSQL(table_accounts,
                    postgressql_connection != null ? "chat_groups = chat_groups || ?" :
                            "chat_groups = CONCAT(chat_groups, ?)", account_friends,"id = ?", account_where);
        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean removeUserGroup(long id, long group_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> account_where = new ArrayList<>();
            account_where.add(id);

            List<Object> account_friends = new ArrayList<>();
            account_friends.add("," + group_id);

            return editDataSQL(table_accounts,
                    "groups = REPLACE(groups, ?, '')", account_friends,
                    "id = ?", account_where);
        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public void handleSessions() {
        if (postgressql_connection != null || mysql_connection != null) {
            Map<String, List<Object>> account_data = getDataSQL(table_accounts, "id", "",
                    null, null, "", 0);

            if (account_data == null) {
                return;
            }

            for (Object account_id : account_data.get("id")) {
                try {
                    updateUserSessionExpire(Long.parseLong(String.valueOf(account_id)));
                } catch (Exception e) {}
            }


        } else if (mongoClient != null) {


        } else if (scylladb_session != null) {


        }
    }





    public Long createConvID(long party_id, long party_id2) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> set_data = new ArrayList<>();
            set_data.add(party_id);
            set_data.add(party_id2);

            Map<String, List<Object>> conv_data = getDataSQL(table_conversations, "conv_id", "",
                    null, null, "", 0);

            if (conv_data == null) {
                return null;
            }

            long id = generateID(conv_data.get("conv_id"));

            set_data.add(id);
            return addDataSQL(table_conversations, "party_id, party_id2, conv_id",
                    "?, ?, ?", set_data) ?
                    id : null;

        } else if (mongoClient != null) {
            return null;

        } else if (scylladb_session != null) {
            return null;

        }
        return null;
    }
    public boolean addMessage(long conv_id, long sender_id, String message) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> edit_condition_data = new ArrayList<>();
            edit_condition_data.add(conv_id);

            Map<String, List<Object>> current_chat_data = getDataSQL(table_chats,"msg, msg_id",
                    "conv_id = ?",
                    edit_condition_data, null, "", 0);

            if (current_chat_data == null) {
                return false;
            }

            List<Object> chat_data = new ArrayList<>();
            chat_data.add(conv_id);
            chat_data.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "|" + message);
            chat_data.add(sender_id);
            chat_data.add(generateID(current_chat_data.get("msg_id")));

            List<Object> set_data = new ArrayList<>();
            set_data.add(message);

            if ((current_chat_data.get("msg").isEmpty()) ||
                    (!editDataSQL(table_chats, "msg = msg || ?", set_data,"conv_id = ?",
                            edit_condition_data))) {

                return addDataSQL(table_chats, "conv_id, msg, sent_by, msg_id","?, ?, ?, ?", chat_data);

            } else {
                return true;
            }
        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public Map<String, List<Object>> getMessages(long sender_id, long conv_id, int amount) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> where_values = new ArrayList<>();
            where_values.add(conv_id);

            return getDataSQL(table_chats, "*", "conv_id = ?", where_values, null,
                    "sent_at DESC", amount);
        } else if (mongoClient != null) {
            return null;

        } else if (scylladb_session != null) {
            return null;

        }
        return null;
    }
    public boolean deleteMessage(long sender_id, long conv_id, long message_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(conv_id);
            condition_data.add(message_id);
            condition_data.add(sender_id);

            return deleteDataSQL(table_chats, "conv_id = ? AND msg_id = ? AND sent_by = ?", condition_data);
        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }




    public Long startCaptcha(String answer) {
        if (postgressql_connection != null || mysql_connection != null) {
            Map<String, List<Object>> data = getDataSQL(table_captchas, "id", "",
                    null, null, "", 0);

            if (data == null) {
                return null;
            }

            long id = generateID(data.get("id"));

            List<Object> values = new ArrayList<>();
            values.add(id);
            values.add(answer);
            values.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            if (!addDataSQL(table_captchas,"id, answer, time, last_edit_time, failed",
                    "?, ?, 10, ?, 0", values)) {
                return null;
            }

            return id;
        } else if (mongoClient != null) {
            return null;

        } else if (scylladb_session != null) {
            return null;

        }
        return null;
    }
    public boolean verifyCaptcha(long id, String given_answer) {
        // TODO when captcha is ready then update this

        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> captcha_data = getDataSQL(table_captchas, "answer, time, failed",
                    "id = ?", condition_data, null, "", 0);

            if (captcha_data == null) {
                return false;
            }

            if (captcha_data.get("answer").contains(given_answer)) {
                // solved!
                return deleteDataSQL(table_captchas, "id = ?", condition_data);

            } else if ((3 <= (short) captcha_data.get("failed").get(0)) ||
                    (0 >= (short) captcha_data.get("time").get(0))) {
                // extended fails or time

                deleteDataSQL(table_captchas, "id = ?", condition_data);

                return false;
            } else {
                // the captcha was not solved and the user has more time and didn't failed 3 times

                if (!(captcha_data.get("time").isEmpty() || captcha_data.get("failed").isEmpty())) {
                    editDataSQL(table_captchas, "failed = failed + 1", null, "id = ?",
                            condition_data);
                }
                return false;
            }
        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateCaptchaTime(long id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> sess_data = getDataSQL(table_captchas, "last_edit_time, time",
                    "id = ?", condition_data, null, "", 0);

            if (sess_data == null || sess_data.get("last_edit_time").isEmpty() || sess_data.get("time").isEmpty()) {
                return false;
            }

            String editTime = String.valueOf(sess_data.get("last_edit_time").get(0));

            if (editTime != "null" && !isOneSecondAgo(Timestamp.valueOf(editTime))) {
                return false;
            }

            if (((short) sess_data.get("time").get(0)) <= 0) {
                // time expire
                return deleteDataSQL(table_captchas, "id = ?", condition_data);
            }

            List<Object> set_data = new ArrayList<>();
            set_data.add(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            return editDataSQL(table_captchas, "last_edit_time = ?, time = time - 1", set_data,
                    "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public void handleCaptchas() {
        if (postgressql_connection != null || mysql_connection != null) {
            Map<String, List<Object>> captcha_data = getDataSQL(table_captchas, "id", "",
                    null, null, "", 0);

            if (captcha_data == null) {
                return;
            }

            for (Object captchas_id : captcha_data.get("id")) {
                try {
                    updateCaptchaTime(Long.parseLong(String.valueOf(captchas_id)));
                } catch (Exception e) {}
            }


        } else if (mongoClient != null) {


        } else if (scylladb_session != null) {


        }
    }





    public boolean createPost(long sender_id, String msg, String tags, String background) {
        if (postgressql_connection != null || mysql_connection != null) {
            // there is no custom background if empty
            List<Object> data = new ArrayList<>();
            data.add(sender_id);
            data.add(msg);
            data.add(tags);
            data.add(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            data.add(background);

            return addDataSQL(table_posts,"sender_id, msg, tags, send_at, background",
                    "?, ?, ?, ?, ?", data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean deletePost(long sender_id, long post_id) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            return deleteDataSQL(table_posts, "id = ? AND sender_id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean editPost(long sender_id, long post_id, String edited_tags,
                            String edited_msg, String given_background) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(post_id);
            condition_data.add(sender_id);

            List<Object> set_data = new ArrayList<>();
            set_data.add(edited_msg);
            set_data.add(edited_tags);
            set_data.add(given_background);

            return editDataSQL(table_posts, "msg = ?, tags = ?, background = ?", set_data,
                    "id = ? AND sender_id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }




    public boolean updateProfilePfp(long id, String given_pfp) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_pfp);

            return editDataSQL(table_profiles, "pfp = ?", profile_data, "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateProfileBanner(long id, String given_banner) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_banner);

            return editDataSQL(table_profiles, "banner = ?", profile_data, "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateProfilePets(long id, String given_pets) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_pets);

            return editDataSQL(table_profiles, "pets = ?", profile_data, "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateProfileCoins(long id, int given_coins) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_coins);

            return editDataSQL(table_profiles, "coins = ?", profile_data, "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateProfileBadges(long id, String given_badges) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_badges);

            return editDataSQL(table_profiles, "badges = ?", profile_data,
                    "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }
    public boolean updateProfileAnimations(long id, String given_animations) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            List<Object> profile_data = new ArrayList<>();
            profile_data.add(given_animations);

            return editDataSQL(table_profiles, "animations = ?", profile_data,
                    "id = ?", condition_data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;
    }





    @Deprecated
    public boolean deleteUser(long id) {
        if (postgressql_connection != null || mysql_connection != null) {

            List<Object> data = new ArrayList<>();

            data.add(id);

            if (!deleteDataSQL(table_accounts, "id = ?", data)) {
                return false;
            }
            if (!deleteDataSQL(table_profiles, "id = ?", data)) {
                return false;
            }

            data.clear();
            data.add(id);
            if (!deleteDataSQL(table_posts, "sender_id = ?", data)) {
                return false;
            }

            data.clear();
            data.add(id);
            data.add(id);
            return deleteDataSQL(table_chats, "id = ? OR id2 = ?", data);

        } else if (mongoClient != null) {
            return false;

        } else if (scylladb_session != null) {
            return false;

        }
        return false;

    }

}
