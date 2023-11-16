package jcorechat.app_api.database;

import com.mongodb.client.*;
import jcorechat.app_api.API;
import org.bson.Document;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseManager {

    protected static final String table_accounts = "accounts";
    protected static final String table_chats = "chats";
    protected static final String table_groups = "chat_groups";
    protected static final String table_group_members = "chat_group_members";
    protected static final String table_group_channels = "chat_group_channels";
    protected static final String table_group_category = "chat_group_categories";
    protected static final String table_group_roles = "chat_group_roles";
    protected static final String table_group_logs = "chat_group_logs";
    protected static final String table_captchas = "captchas";
    protected static final String table_reactions = "reactions";
    protected static final String table_posts = "posts";
    protected static final String table_post_comments = "post_comments";
    protected static final String table_profiles = "profiles";
    protected static final String table_shop = "shop";


    protected final String postgressql_url = "jdbc:postgresql://localhost:5433/jcorechat-db";
    protected final String postgressql_username = "jcorechat";
    protected final String postgressql_password = "app_api";
    protected Connection postgressql_connection = null;


    protected final String mysql_url = "jdbc:mysql://localhost:3306/jcorechat";
    protected final String mysql_username = "jcorechat";
    protected final String mysql_password = "JCCpwd123";
    protected Connection mysql_connection = null;




    protected final String mongo_database = "jcorechat";
    protected final String mongo_url = "mongodb://localhost:27017";
    protected MongoDatabase mongoDatabase = null;
    protected MongoClient mongoClient = null;



    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");





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
            accounts_table.add("created_at timestamp NOT NULL, ");
            accounts_table.add("friends TEXT NOT NULL, ");
            accounts_table.add("chat_groups_ TEXT NOT NULL, ");
            accounts_table.add("starts_sub TIMESTAMP NULL, ");
            accounts_table.add("ends_sub TIMESTAMP NULL, ");
            accounts_table.add("bookmarks TEXT NOT NULL");

            List<String> chats_table = new ArrayList<>();
            chats_table.add("channel_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("send_at timestamp NOT NULL, ");
            chats_table.add("send_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL");
            chats_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id)");

            List<String> reactions_table = new ArrayList<>();
            reactions_table.add("channel_id BIGINT NOT NULL, ");
            reactions_table.add("reaction VARCHAR(255) UNIQUE NOT NULL, ");
            reactions_table.add("msg_id BIGINT NOT NULL, ");
            reactions_table.add("member_id BIGINT NOT NULL, ");
            reactions_table.add("FOREIGN KEY (member_id) REFERENCES accounts(id)");

            List<String> group_table = new ArrayList<>();
            group_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_table.add("name VARCHAR(50) NOT NULL, ");
            group_table.add("owner_id BIGINT NOT NULL, ");
            group_table.add("logo TEXT NOT NULL, ");
            group_table.add("banner TEXT NOT NULL, ");
            group_table.add("animations TEXT NOT NULL, ");
            group_table.add("created_at TIMESTAMP NOT NULL, ");
            group_table.add("FOREIGN KEY (owner_id) REFERENCES accounts(id)");

            List<String> group_members_tabls = new ArrayList<>();
            group_members_tabls.add("group_id BIGINT, ");
            group_members_tabls.add("member_id BIGINT, ");
            group_members_tabls.add("roles_id TEXT NOT NULL, ");
            group_members_tabls.add("nickname VARCHAR(50) NULL, ");
            group_members_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_members_tabls.add("FOREIGN KEY (member_id) REFERENCES accounts(id)");

            List<String> group_channels_tabls = new ArrayList<>();
            group_channels_tabls.add("group_id BIGINT, ");
            group_channels_tabls.add("channel_id BIGINT, ");
            group_channels_tabls.add("name TEXT NOT NULL, ");
            group_channels_tabls.add("permissions TEXT NOT NULL, ");
            group_channels_tabls.add("channel_type TEXT NOT NULL, ");
            group_channels_tabls.add("category_id BIGINT NOT NULL, ");
            group_channels_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_channels_tabls.add("FOREIGN KEY (category_id) REFERENCES chat_group_categories(category_id)");

            List<String> group_categories_tabls = new ArrayList<>();
            group_categories_tabls.add("category_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_categories_tabls.add("group_id BIGINT NOT NULL, ");
            group_categories_tabls.add("name TEXT NOT NULL, ");
            group_categories_tabls.add("permissions TEXT NOT NULL, ");
            group_categories_tabls.add("category_type TEXT NOT NULL, ");
            group_categories_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id)");

            List<String> group_roles_tabls = new ArrayList<>();
            group_roles_tabls.add("group_id BIGINT, ");
            group_roles_tabls.add("role_id BIGINT, ");
            group_roles_tabls.add("name TEXT NOT NULL, ");
            group_roles_tabls.add("permissions TEXT NOT NULL, ");
            group_roles_tabls.add("role_type TEXT NOT NULL, ");
            group_roles_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id)");

            List<String> group_logs_tabls = new ArrayList<>();
            group_logs_tabls.add("group_id BIGINT, ");
            group_logs_tabls.add("actor_id BIGINT, ");
            group_logs_tabls.add("log_type TEXT NOT NULL, ");
            group_logs_tabls.add("log_message TEXT NOT NULL, ");
            group_logs_tabls.add("acted_at TIMESTAMP NOT NULL, ");
            group_logs_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_logs_tabls.add("FOREIGN KEY (actor_id) REFERENCES accounts(id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT PRIMARY KEY NOT NULL, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time smallint NOT NULL, ");
            captchas_table.add("last_edit_time TEXT NOT NULL, ");
            captchas_table.add("failed smallint NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id bigserial PRIMARY KEY NOT NULL, ");
            posts_table.add("sender_id BIGINT NOT NULL, ");
            posts_table.add("send_at timestamp NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("background TEXT NOT NULL, ");
            posts_table.add("FOREIGN KEY (sender_id) REFERENCES accounts(id)");

            List<String> posts_comment_table = new ArrayList<>();
            posts_comment_table.add("post_id BIGINT NOT NULL, ");
            posts_comment_table.add("send_by BIGINT NOT NULL, ");
            posts_comment_table.add("send_at timestamp NOT NULL, ");
            posts_comment_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_comment_table.add("msg_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_comment_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id), ");
            posts_comment_table.add("FOREIGN KEY (post_id) REFERENCES posts(id)");

            List<String> profiles_table = new ArrayList<>();
            profiles_table.add("id BIGINT NOT NULL, ");
            profiles_table.add("pfp TEXT NOT NULL, ");
            profiles_table.add("banner TEXT NOT NULL, ");
            profiles_table.add("pets TEXT, ");
            profiles_table.add("coins INT NOT NULL, ");
            profiles_table.add("badges TEXT NOT NULL, ");
            profiles_table.add("animations TEXT, ");
            profiles_table.add("FOREIGN KEY (id) REFERENCES accounts(id)");

            List<String> shop_table = new ArrayList<>();
            shop_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL, ");
            shop_table.add("item_name TEXT NOT NULL, ");
            shop_table.add("item_price INT NOT NULL, ");
            shop_table.add("seller_id BIGINT NOT NULL");

            deleteTableSQL(table_profiles);
            deleteTableSQL(table_shop);
            deleteTableSQL(table_post_comments);
            deleteTableSQL(table_posts);
            deleteTableSQL(table_group_members);
            deleteTableSQL(table_group_channels);
            deleteTableSQL(table_group_category);
            deleteTableSQL(table_group_roles);
            deleteTableSQL(table_group_logs);
            deleteTableSQL(table_groups);
            deleteTableSQL(table_reactions);
            deleteTableSQL(table_chats);
            deleteTableSQL(table_captchas);
            deleteTableSQL(table_accounts);

            createTableSQL( table_accounts, accounts_table);
            createTableSQL( table_captchas, captchas_table);
            createTableSQL( table_profiles, profiles_table);
            createTableSQL( table_posts, posts_table);
            createTableSQL(table_post_comments, posts_comment_table);
            createTableSQL( table_chats, chats_table);
            createTableSQL(table_reactions, reactions_table);
            createTableSQL(table_groups, group_table);
            createTableSQL(table_group_members, group_members_tabls);
            createTableSQL(table_group_category, group_categories_tabls);
            createTableSQL(table_group_channels, group_channels_tabls);
            createTableSQL(table_group_roles, group_roles_tabls);
            createTableSQL(table_group_logs, group_logs_tabls);
            createTableSQL(table_shop, shop_table);

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
            accounts_table.add("created_at TIMESTAMP NOT NULL, ");
            accounts_table.add("friends TEXT NOT NULL, ");
            accounts_table.add("chat_groups_ TEXT NOT NULL, ");
            accounts_table.add("starts_sub TIMESTAMP NULL, ");
            accounts_table.add("ends_sub TIMESTAMP NULL, ");
            accounts_table.add("bookmarks TEXT NOT NULL");

            List<String> chats_table = new ArrayList<>();
            chats_table.add("channel_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("send_at TIMESTAMP NOT NULL, ");
            chats_table.add("send_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL, ");
            chats_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id)");

            List<String> reactions_table = new ArrayList<>();
            reactions_table.add("channel_id BIGINT NOT NULL, ");
            reactions_table.add("reaction VARCHAR(255) UNIQUE NOT NULL, ");
            reactions_table.add("msg_id BIGINT NOT NULL, ");
            reactions_table.add("member_id BIGINT NOT NULL, ");
            reactions_table.add("FOREIGN KEY (member_id) REFERENCES accounts(id)");

            List<String> group_table = new ArrayList<>();
            group_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_table.add("name VARCHAR(50) NOT NULL, ");
            group_table.add("owner_id BIGINT NOT NULL, ");
            group_table.add("logo TEXT NOT NULL, ");
            group_table.add("banner TEXT NOT NULL, ");
            group_table.add("animations TEXT NOT NULL, ");
            group_table.add("settings TEXT NOT NULL, ");
            group_table.add("created_at TIMESTAMP NOT NULL, ");
            group_table.add("FOREIGN KEY (owner_id) REFERENCES accounts(id)");

            List<String> group_members_tabls = new ArrayList<>();
            group_members_tabls.add("group_id BIGINT, ");
            group_members_tabls.add("member_id BIGINT, ");
            group_members_tabls.add("roles_id TEXT NOT NULL, ");
            group_members_tabls.add("nickname VARCHAR(50) NULL, ");
            group_members_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_members_tabls.add("FOREIGN KEY (member_id) REFERENCES accounts(id)");

            List<String> group_channels_tabls = new ArrayList<>();
            group_channels_tabls.add("group_id BIGINT, ");
            group_channels_tabls.add("channel_id BIGINT, ");
            group_channels_tabls.add("name TEXT NOT NULL, ");
            group_channels_tabls.add("permissions TEXT NOT NULL, ");
            group_channels_tabls.add("channel_type TEXT NOT NULL, ");
            group_channels_tabls.add("category_id BIGINT NOT NULL, ");
            group_channels_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_channels_tabls.add("FOREIGN KEY (category_id) REFERENCES chat_group_categories(category_id)");

            List<String> group_categories_tabls = new ArrayList<>();
            group_categories_tabls.add("category_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_categories_tabls.add("group_id BIGINT NOT NULL, ");
            group_categories_tabls.add("name TEXT NOT NULL, ");
            group_categories_tabls.add("permissions TEXT NOT NULL, ");
            group_categories_tabls.add("category_type TEXT NOT NULL, ");
            group_categories_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id)");

            List<String> group_roles_tabls = new ArrayList<>();
            group_roles_tabls.add("group_id BIGINT, ");
            group_roles_tabls.add("role_id BIGINT, ");
            group_roles_tabls.add("name TEXT NOT NULL, ");
            group_roles_tabls.add("permissions TEXT NOT NULL, ");
            group_roles_tabls.add("role_type TEXT NOT NULL, ");
            group_roles_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id)");

            List<String> group_logs_tabls = new ArrayList<>();
            group_logs_tabls.add("group_id BIGINT, ");
            group_logs_tabls.add("actor_id BIGINT, ");
            group_logs_tabls.add("log_type TEXT NOT NULL, ");
            group_logs_tabls.add("log_message TEXT NOT NULL, ");
            group_logs_tabls.add("acted_at TIMESTAMP NOT NULL, ");
            group_logs_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_logs_tabls.add("FOREIGN KEY (actor_id) REFERENCES accounts(id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time SMALLINT NOT NULL, ");
            captchas_table.add("last_edit_time TIMESTAMP NOT NULL, ");
            captchas_table.add("failed SMALLINT NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_table.add("sender_id BIGINT NOT NULL, ");
            posts_table.add("send_at TIMESTAMP NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("background TEXT NULL, ");
            posts_table.add("FOREIGN KEY (sender_id) REFERENCES accounts(id)");

            List<String> posts_comment_table = new ArrayList<>();
            posts_comment_table.add("post_id BIGINT NOT NULL, ");
            posts_comment_table.add("send_by BIGINT NOT NULL, ");
            posts_comment_table.add("send_at timestamp NOT NULL, ");
            posts_comment_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_comment_table.add("msg_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_comment_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id), ");
            posts_comment_table.add("FOREIGN KEY (post_id) REFERENCES posts(id)");

            List<String> profiles_table = new ArrayList<>();
            profiles_table.add("id BIGINT NOT NULL, ");
            profiles_table.add("pfp TEXT NOT NULL, ");
            profiles_table.add("banner TEXT NOT NULL, ");
            profiles_table.add("pets TEXT NULL, ");
            profiles_table.add("coins INT NOT NULL, ");
            profiles_table.add("badges TEXT NOT NULL, ");
            profiles_table.add("animations TEXT NULL, ");
            profiles_table.add("FOREIGN KEY (id) REFERENCES accounts(id)");

            List<String> shop_table = new ArrayList<>();
            shop_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL, ");
            shop_table.add("item_name TEXT NOT NULL, ");
            shop_table.add("item_price INT NOT NULL, ");
            shop_table.add("seller_id BIGINT NOT NULL");

            deleteTableSQL(table_profiles);
            deleteTableSQL(table_shop);
            deleteTableSQL(table_post_comments);
            deleteTableSQL(table_posts);
            deleteTableSQL(table_group_members);
            deleteTableSQL(table_group_channels);
            deleteTableSQL(table_group_category);
            deleteTableSQL(table_group_roles);
            deleteTableSQL(table_group_logs);
            deleteTableSQL(table_groups);
            deleteTableSQL(table_reactions);
            deleteTableSQL(table_chats);
            deleteTableSQL(table_captchas);
            deleteTableSQL(table_accounts);

            createTableSQL( table_accounts, accounts_table);
            createTableSQL( table_captchas, captchas_table);
            createTableSQL( table_profiles, profiles_table);
            createTableSQL( table_posts, posts_table);
            createTableSQL(table_post_comments, posts_comment_table);
            createTableSQL( table_chats, chats_table);
            createTableSQL(table_reactions, reactions_table);
            createTableSQL(table_groups, group_table);
            createTableSQL(table_group_members, group_members_tabls);
            createTableSQL(table_group_category, group_categories_tabls);
            createTableSQL(table_group_channels, group_channels_tabls);
            createTableSQL(table_group_roles, group_roles_tabls);
            createTableSQL(table_group_logs, group_logs_tabls);
            createTableSQL(table_shop, shop_table);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupMongoDB() {
        try {
            mongoClient = MongoClients.create(mongo_url);
            mongoDatabase = mongoClient.getDatabase(mongo_database);

            MongoDeleteCollectionNoSQL(table_accounts);
            MongoDeleteCollectionNoSQL(table_profiles);
            MongoDeleteCollectionNoSQL(table_captchas);
            MongoDeleteCollectionNoSQL(table_chats);
            MongoDeleteCollectionNoSQL(table_reactions);
            MongoDeleteCollectionNoSQL(table_posts);
            MongoDeleteCollectionNoSQL(table_groups);
            MongoDeleteCollectionNoSQL(table_shop);


            MongoCreateCollectionNoSQL(table_accounts);
            MongoCreateCollectionNoSQL(table_profiles);
            MongoCreateCollectionNoSQL(table_captchas);
            MongoCreateCollectionNoSQL(table_chats);
            MongoCreateCollectionNoSQL(table_reactions);
            MongoCreateCollectionNoSQL(table_posts);
            MongoCreateCollectionNoSQL(table_groups);
            MongoCreateCollectionNoSQL(table_shop);

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

            }
        } catch (Exception e) {}
    }


    @Deprecated
    protected boolean createTableSQL(String table, List<String> colums) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {
            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            stringBuilder.append(table).append(" ( ");
            if (colums != null) {
                for (String col : colums) {
                    stringBuilder.append(col);
                }
            }

            getSQLConnection().prepareStatement(stringBuilder.append(" );").toString()).executeUpdate();
            return true;

        } catch (Exception  e) {
            return false;
        }
    }


    @Deprecated
    protected boolean deleteTableSQL(String table) {
        if (postgressql_connection == null && mysql_connection == null) {
            return false;
        }

        try {
            getSQLConnection().prepareStatement(new StringBuilder("DROP TABLE IF EXISTS ")
                    .append(table).append(";").toString()).executeUpdate();

            return true;

        } catch (Exception  e) {
            return false;
        }
    }




    @Deprecated
    protected boolean MongoCreateCollectionNoSQL(String collectionName) {
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
    protected boolean MongoDeleteCollectionNoSQL(String collectionName) {
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

    protected List<Map<String, Object>> MongoReadCollectionNoSQL(String collectionName, Document condition,
                                                       boolean expectOne, String... filters) {
        if (mongoDatabase == null) {
            return null;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        List<String> filter_list = Arrays.stream(filters).toList();

        try {
            MongoCursor<Document> cursor = mongoDatabase.getCollection(collectionName).find(condition).iterator();

            while (cursor.hasNext()) {
                if (expectOne && !result.isEmpty() && !result.get(0).isEmpty()) {
                    break;
                }
                result = conditionFilter(condition, result, cursor.next(),
                        filter_list.size() > 0 ? filter_list : null, new HashMap<>());

            }

            cursor.close();
            return result;

        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> conditionFilter(Document condition, List<Map<String, Object>> result,
                                                      Document next, List<String> filter_list,
                                                      HashMap<String, Object> data_to_add) {
        if (condition != null) {
            boolean contains = false;
            for (Map.Entry<String, Object> entry2 : condition.entrySet()) {
                String key = entry2.getKey();
                contains = next.containsKey(key) && next.get(key).equals(entry2.getValue());
            }

            if (contains) {
                if (filter_list != null) {
                    data_to_add = FilterHandler(data_to_add, next, filter_list);

                } else if (!result.contains(next)) {
                    for (Map.Entry<String, Object> entry : next.entrySet()) {
                        data_to_add.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } else {
            if (filter_list != null) {
                data_to_add = FilterHandler(data_to_add, next, filter_list);

            } else if (!result.contains(next)) {

                for (Map.Entry<String, Object> entry : next.entrySet()) {
                    data_to_add.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (!data_to_add.isEmpty()) {
            result.add(data_to_add);
        }

        return result;
    }

    protected HashMap<String, Object> FilterHandler(HashMap<String, Object> data_to_add, Document next, List<String> filter_list) {
        for (Map.Entry<String, Object> entry : next.entrySet()) {
            if (filter_list.contains(entry.getKey())) {
                data_to_add.put(entry.getKey(), entry.getValue());
            }
        }

        return data_to_add;
    }

    protected boolean MongoAddDataToCollectionNoSQL(String collectionName, Document document, Document condition) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            if (condition != null) {
                Document existingDocument = collection.find(condition).first();
                if (existingDocument == null) {
                    return false;
                }

                Document update = new Document("$set", document);
                collection.updateOne(existingDocument, update);

            } else {
                collection.insertOne(document);
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    protected boolean MongoUpdateDocumentInCollectionNoSQL(String collectionName, Document filter, Document updatedDoc) {
        if (mongoDatabase == null) {
            return false;
        }

        try {

            //Document update = new Document("$set", updatedDoc);

            /*
            if (toConcat) {
                List<Document> docs = new ArrayList<>();
                for (Map.Entry<String, Object> entry: updatedDoc.entrySet()) {
                    String key = entry.getKey();
                    List<Map<String, Object>> original_data = MongoReadCollectionNoSQL(collectionName, filter,
                            true, key);

                    if (original_data == null) {
                        return false;
                    }

                    if (!original_data.isEmpty() && !original_data.get(0).isEmpty()) {
                        Object original_value = original_data.get(0).get(key);
                        Object value = entry.getValue();
                        Object updated_value = null;
                        switch (original_value.getClass().getSimpleName()) {
                            case "Long":
                                updated_value = Long.valueOf(String.valueOf(original_value)) +
                                        Long.valueOf(String.valueOf(value));
                                break;
                            case "String":
                                updated_value = String.valueOf(original_value) + String.valueOf(value);
                                break;
                            case "Integer":
                                updated_value = Integer.valueOf(String.valueOf(original_value)) +
                                        Integer.valueOf(String.valueOf(value));
                                break;
                            case "Short":
                                updated_value = Short.valueOf(String.valueOf(original_value)) +
                                        Short.valueOf(String.valueOf(value));
                                break;
                        }

                        if (updated_value == null) {
                            return false;
                        }

                        docs.add(new Document(entry.getKey(), updated_value));
                    }

                }

                Document whole_doc = new Document();
                for (Document document : docs) {
                    whole_doc.append("$set", document);
                }

                mongoDatabase.getCollection(collectionName).updateMany(filter, whole_doc);
                return true;

            } else if (toRemove) {
                List<Document> docs = new ArrayList<>();
                for (Map.Entry<String, Object> entry: updatedDoc.entrySet()) {
                    String key = entry.getKey();
                    List<Map<String, Object>> original_data = MongoReadCollectionNoSQL(collectionName, filter,
                            true, key);

                    if (original_data == null) {
                        return false;
                    }

                    if (!original_data.isEmpty() && !original_data.get(0).isEmpty()) {
                        Object original_value = original_data.get(0).get(key);
                        Object value = entry.getValue();
                        Object updated_value = null;
                        switch (original_value.getClass().getSimpleName()) {
                            case "Long":
                                updated_value = Long.valueOf(String.valueOf(original_value)) -
                                        Long.valueOf(String.valueOf(value));
                                break;
                            case "String":
                                updated_value = String.valueOf(original_value).replace(String.valueOf(value), "");
                                break;
                            case "Integer":
                                updated_value = Integer.valueOf(String.valueOf(original_value)) -
                                        Integer.valueOf(String.valueOf(value));
                                break;
                            case "Short":
                                updated_value = Short.valueOf(String.valueOf(original_value)) -
                                        Short.valueOf(String.valueOf(value));
                                break;
                        }

                        if (updated_value == null) {
                            return false;
                        }

                        docs.add(new Document(entry.getKey(), updated_value));
                    }

                }

                Document whole_doc = new Document();
                for (Document document : docs) {
                    whole_doc.append("$set", document);
                }

                mongoDatabase.getCollection(collectionName).updateMany(filter, whole_doc);
                return true;

            }

             */

            mongoDatabase.getCollection(collectionName).updateMany(filter, new Document("$set", updatedDoc));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    protected boolean MongoDeleteDataFromCollectionNoSQL(String collectionName, Document filter) {
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

    protected long MongoGenerateID(List<Map<String, Object>> post_data) {
        List<Object> all_posts = new ArrayList<>();

        for (Map<String, Object> data : post_data) {
            all_posts.addAll(data.values());
        }

        return generateID(all_posts);
    }






    protected Map<String, List<Object>> readOutputSQL(ResultSet resultSet) {
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

    protected List<Object> setDataSQL(short parameterIndex, List<Object> changes, PreparedStatement preparedStatement)
            throws Exception {
        List<Object> list = new ArrayList<>();

        if (null != changes) {
            for (Object value : changes) {
                if (value == null) {
                    preparedStatement.setNull(parameterIndex, Types.NULL);
                    continue;
                }
                switch (value.getClass().getSimpleName()) {
                    case "String":
                        preparedStatement.setString(parameterIndex, String.valueOf(value));
                        break;

                    case "Integer":
                        preparedStatement.setInt(parameterIndex, Integer.parseInt(String.valueOf(value)));
                        break;

                    case "Long":
                        preparedStatement.setLong(parameterIndex, Long.parseLong(String.valueOf(value)));
                        break;

                    case "Short":
                        preparedStatement.setShort(parameterIndex, Short.parseShort(String.valueOf(value)));
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

    protected boolean deleteDataSQL(String table, String condition, List<Object> conditionData) {
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

    protected boolean addDataSQL(String table, String fields, String values, List<Object> data) {

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
            return false;
        }

    }

    protected boolean editDataSQL(String table, String set_expression,
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

    protected Map<String, List<Object>> getDataSQL(String table, String data_to_get, String condition,
                                              List<Object> conditionData, Map<String, String> join_data,
                                              String order, int limit) {

        if (postgressql_connection == null && mysql_connection == null) {
            return null;
        }

        StringBuilder select_query = new StringBuilder("SELECT ").append(data_to_get).append(" FROM ").append(table);

        // join_data ->  key = table name ; value = condition
        if (null != join_data && !join_data.isEmpty()) {
            for (Map.Entry<String, String> entry : join_data.entrySet()) {
                select_query.append(" JOIN ").append(entry.getKey()).append(" ON ").append(entry.getValue());
            }

        }

        if (!condition.isBlank()) { select_query.append(" WHERE ").append(condition); }

        if (!order.isBlank()) { select_query.append(" ORDER BY ").append(order); }

        if (0 < limit) { select_query.append(" LIMIT ").append(limit); }

        try {

            // We use Java 17 so the hashmap is ordered, and we assume that it is.

            PreparedStatement preparedStatement = getSQLConnection().prepareStatement(select_query.append(";")
                    .toString());

            return readOutputSQL(((PreparedStatement) setDataSQL((short) 1, conditionData, preparedStatement).get(1)).executeQuery());

        } catch (Exception e) {
            return null;
        }

    }




    protected long generateID(List<Object> toContain) {
        long id = 1L;
        while (toContain.contains(id) || id == 0L) {
            id = API.random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return id;
    }

    protected boolean isOneSecondAgo(Timestamp last_edit_time) {
        try {

            return Duration.between(last_edit_time.toInstant(),
                    new Timestamp(System.currentTimeMillis()).toInstant()).toMillis() > 1000;

        } catch (Exception e) { return false; }

    }

    protected Connection getSQLConnection() {
        return postgressql_connection != null ? postgressql_connection :
                mysql_connection;
    }

    protected List<Map<String, Object>> getCollectionMongo(String table, String collection, Document filter) {
        List<Map<String, Object>> group = MongoReadCollectionNoSQL(table,
                filter, true, collection);

        if (group == null) {
            return null;
        }

        List<Map<String, Object>> all_categories = new ArrayList<>();
        if (!group.isEmpty()) {
            if (group.get(0).get(collection) != null) {
                all_categories.addAll((List<Map<String, Object>>) group.get(0).get(collection));
            }
        }
        return all_categories;
    }


    public boolean checkIDExists(long id, String table) {
        if (postgressql_connection != null || mysql_connection != null) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> data = getDataSQL(table, "id",
                    "id = ?", condition_data, null, "", 0);

            return data != null && data.get("id") != null && !data.get("id").isEmpty();

        } else if (mongoClient != null && mongoDatabase != null) {

            List<Map<String, Object>> data = MongoReadCollectionNoSQL(table,
                    new Document("id", id), true, "id");

            return data != null && data.get(0) != null && data.get(0).get("id") != null;
        }
        return false;
    }

    public boolean checkNotUniqueWithStream(List<Map<String, Object>> values, String key, Object key_to_check) {
        return values.stream().anyMatch(map ->
                String.valueOf(map.get(key)).equals(key_to_check));
    }

    public List<Object> extract_all_content(List<Map<String, Object>> mongo_data, String to_extract) {
        List<Object> extracted_data = new ArrayList<>();
        for (Map<String, Object> map : mongo_data) {
            if (map.containsKey(to_extract)) {
                extracted_data.add(map.get(to_extract));
            }
        }
        return extracted_data;
    }

    public List<Object> extract_all_content(Map<String, List<Object>> sql_data, String to_extract) {
        List<Object> extracted_data = new ArrayList<>();
        for (Map.Entry<String, List<Object>> map : sql_data.entrySet()) {
            if (map.getKey().equals(to_extract)) {
                extracted_data.add(map.getValue());
            }
        }
        return extracted_data;
    }

    public boolean checkUnique(List<Map<String, Object>> mongo_data, Map<String, Object> values) {
        for (Map<String, Object> map : mongo_data) {
            for (Map.Entry<String, Object> map_data : map.entrySet()) {
                String key = map_data.getKey();
                if (values.containsKey(key) && values.get(key).equals(map_data.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<String, List<Object>> transformMongoToSQL(int amount, List<Map<String, Object>> mongoData,
                                                                 Map<String, List<Object>> result) {
        int i = 0;
        for (Map<String, Object> map : mongoData) {
            for (Map.Entry<String, Object> data : map.entrySet()) {
                String key = data.getKey();
                if (!key.equals("_id")) {
                    result.get(key).add(data.getValue());
                }
            }
            i++;
            if (i >= amount) {
                break;
            }
        }

        return result;
    }

}
