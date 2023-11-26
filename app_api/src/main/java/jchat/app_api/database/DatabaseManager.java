package jchat.app_api.database;

import com.mongodb.client.*;
import jchat.app_api.API;
import org.bson.Document;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    protected static final String table_friend_requests = "friend_requests";


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



    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");





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
            accounts_table.add("starts_sub TIMESTAMP NULL, ");
            accounts_table.add("ends_sub TIMESTAMP NULL, ");
            accounts_table.add("bookmarks TEXT NOT NULL");

            List<String> chats_table = new ArrayList<>();
            chats_table.add("channel_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("send_at TIMESTAMP(6) NOT NULL, ");
            chats_table.add("send_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL");
            chats_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id)");

            List<String> reactions_table = new ArrayList<>();
            reactions_table.add("channel_id BIGINT NOT NULL, ");
            reactions_table.add("reaction VARCHAR(255) UNIQUE NOT NULL, ");
            reactions_table.add("msg_id BIGINT NOT NULL, ");
            reactions_table.add("post_id BIGINT NOT NULL, ");
            reactions_table.add("member_id BIGINT NOT NULL, ");
            reactions_table.add("FOREIGN KEY (member_id) REFERENCES accounts(id)");

            List<String> group_table = new ArrayList<>();
            group_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_table.add("name VARCHAR(50) NOT NULL, ");
            group_table.add("owner_id BIGINT NOT NULL, ");
            group_table.add("logo TEXT NOT NULL, ");
            group_table.add("banner TEXT NOT NULL, ");
            group_table.add("animations TEXT NOT NULL, ");
            group_table.add("created_at TIMESTAMP(6) NOT NULL, ");
            group_table.add("group_events TEXT, ");
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
            group_channels_tabls.add("categories_id TEXT NOT NULL, ");
            group_channels_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");

            List<String> group_categories_tabls = new ArrayList<>();
            group_categories_tabls.add("category_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_categories_tabls.add("group_id BIGINT NOT NULL, ");
            group_categories_tabls.add("name TEXT NOT NULL, ");
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
            group_logs_tabls.add("acted_at TIMESTAMP(6) NOT NULL, ");
            group_logs_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_logs_tabls.add("FOREIGN KEY (actor_id) REFERENCES accounts(id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT PRIMARY KEY NOT NULL, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time smallint NOT NULL, ");
            captchas_table.add("last_edit_time TEXT NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id bigserial PRIMARY KEY NOT NULL, ");
            posts_table.add("sender_id BIGINT NOT NULL, ");
            posts_table.add("send_at TIMESTAMP(6) NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("background TEXT NOT NULL, ");
            posts_table.add("FOREIGN KEY (sender_id) REFERENCES accounts(id)");

            List<String> posts_comment_table = new ArrayList<>();
            posts_comment_table.add("post_id BIGINT NOT NULL, ");
            posts_comment_table.add("send_by BIGINT NOT NULL, ");
            posts_comment_table.add("send_at TIMESTAMP(6) NOT NULL, ");
            posts_comment_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_comment_table.add("msg_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_comment_table.add("repl_to TEXT NOT NULL, ");
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
            shop_table.add("item_type TEXT NOT NULL, ");
            shop_table.add("item_price INT NOT NULL, ");
            shop_table.add("seller_id BIGINT NOT NULL, ");
            shop_table.add("sell_at TIMESTAMP(6) NOT NULL");

            List<String> friends_table = new ArrayList<>();
            friends_table.add("id BIGINT NOT NULL, ");
            friends_table.add("id2 BIGINT NOT NULL, ");
            friends_table.add("FOREIGN KEY (id) REFERENCES accounts(id), ");
            friends_table.add("FOREIGN KEY (id2) REFERENCES accounts(id)");

            deleteTableSQL(table_profiles);
            deleteTableSQL(table_friend_requests);
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
            createTableSQL(table_friend_requests, friends_table);

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
            accounts_table.add("starts_sub TIMESTAMP NULL, ");
            accounts_table.add("ends_sub TIMESTAMP NULL, ");
            accounts_table.add("bookmarks TEXT NOT NULL");

            List<String> chats_table = new ArrayList<>();
            chats_table.add("channel_id BIGINT NOT NULL, ");
            chats_table.add("msg VARCHAR(2000) NOT NULL, ");
            chats_table.add("send_at TIMESTAMP(6) NOT NULL, ");
            chats_table.add("send_by BIGINT NOT NULL, ");
            chats_table.add("msg_id BIGINT NOT NULL, ");
            chats_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id)");

            List<String> reactions_table = new ArrayList<>();
            reactions_table.add("channel_id BIGINT NOT NULL, ");
            reactions_table.add("reaction VARCHAR(255) UNIQUE NOT NULL, ");
            reactions_table.add("msg_id BIGINT NOT NULL, ");
            reactions_table.add("post_id BIGINT NOT NULL, ");
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
            group_table.add("created_at TIMESTAMP(6) NOT NULL, ");
            group_table.add("group_events TEXT, ");
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
            group_channels_tabls.add("categories_id TEXT NOT NULL, ");
            group_channels_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id)");

            List<String> group_categories_tabls = new ArrayList<>();
            group_categories_tabls.add("category_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            group_categories_tabls.add("group_id BIGINT NOT NULL, ");
            group_categories_tabls.add("name TEXT NOT NULL, ");
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
            group_logs_tabls.add("acted_at TIMESTAMP(6) NOT NULL, ");
            group_logs_tabls.add("FOREIGN KEY (group_id) REFERENCES chat_groups(id), ");
            group_logs_tabls.add("FOREIGN KEY (actor_id) REFERENCES accounts(id)");

            List<String> captchas_table = new ArrayList<>();
            captchas_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            captchas_table.add("answer TEXT NOT NULL, ");
            captchas_table.add("time SMALLINT NOT NULL, ");
            captchas_table.add("last_edit_time TIMESTAMP(6) NOT NULL");

            List<String> posts_table = new ArrayList<>();
            posts_table.add("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_table.add("send_by BIGINT NOT NULL, ");
            posts_table.add("send_at TIMESTAMP NOT NULL, ");
            posts_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_table.add("background TEXT NULL, ");
            posts_table.add("FOREIGN KEY (send_by) REFERENCES accounts(id)");

            List<String> posts_comment_table = new ArrayList<>();
            posts_comment_table.add("post_id BIGINT NOT NULL, ");
            posts_comment_table.add("send_by BIGINT NOT NULL, ");
            posts_comment_table.add("send_at TIMESTAMP(6) NOT NULL, ");
            posts_comment_table.add("msg VARCHAR(200) NOT NULL, ");
            posts_comment_table.add("msg_id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            posts_comment_table.add("repl_to TEXT NOT NULL, ");
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
            shop_table.add("item_type TEXT NOT NULL, ");
            shop_table.add("item_price INT NOT NULL, ");
            shop_table.add("seller_id BIGINT NOT NULL, ");
            shop_table.add("sell_at TIMESTAMP(6) NOT NULL");

            List<String> friends_table = new ArrayList<>();
            friends_table.add("id BIGINT NOT NULL, ");
            friends_table.add("id2 BIGINT NOT NULL, ");
            friends_table.add("FOREIGN KEY (id) REFERENCES accounts(id), ");
            friends_table.add("FOREIGN KEY (id2) REFERENCES accounts(id)");

            deleteTableSQL(table_profiles);
            deleteTableSQL(table_friend_requests);
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
            createTableSQL(table_friend_requests, friends_table);

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
            MongoCreateCollectionNoSQL(table_friend_requests);

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
        if (!isSQL()) {
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
        if (!isSQL()) {
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
        if (!isMongo()) {
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
        if (!isMongo()) {
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
        if (!isMongo()) {
            return null;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        List<String> filter_list = Arrays.stream(filters).toList();

        try {
            MongoCursor<Document> cursor = mongoDatabase.getCollection(collectionName).find().iterator();

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

    protected HashMap<String, Object> FilterHandler(HashMap<String, Object> data_to_add, Document next,
                                                    List<String> filter_list) {
        for (Map.Entry<String, Object> entry : next.entrySet()) {
            if (filter_list.contains(entry.getKey())) {
                data_to_add.put(entry.getKey(), entry.getValue());
            }
        }

        return data_to_add;
    }

    protected boolean MongoAddDataToCollectionNoSQL(String collectionName, Document document, Document condition) {
        if (!isMongo()) {
            return false;
        }

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
            e.printStackTrace();
            return false;
        }
    }


    protected boolean MongoUpdateDocumentInCollectionNoSQL(String collectionName, Document filter, Document updatedDoc) {
        if (!isMongo()) {
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
            e.printStackTrace();
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

    protected long MongoGenerateID(List<Map<String, Object>> post_data, String ids) {
        return generateID(extract_all_content(post_data, ids));
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
        if (!isSQL()) {
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
        if (!isSQL()) {
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
        if (!isSQL()) {
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

        if (!isSQL()) {
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

        if (!group.isEmpty()) {
            Object group_collection = group.get(0).get(collection);
            if (group_collection != null) {
                return (List<Map<String, Object>>) group_collection;
            }
        }
        return group;
    }


    protected boolean checkIDExists(long id, String table) {
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(id);

            Map<String, List<Object>> data = getDataSQL(table, "id",
                    "id = ?", condition_data, null, "", 0);

            return data != null && data.get("id") != null && !data.get("id").isEmpty();

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> data = MongoReadCollectionNoSQL(table, new Document("id", id), false);
            return data != null && !data.isEmpty() && data.get(0) != null && !data.get(0).isEmpty();
        }
        return false;
    }

    protected boolean checkRoleExists(long role_id, long group_id) {
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(role_id);
            condition_data.add(group_id);

            Map<String, List<Object>> data = getDataSQL(table_group_roles, "role_id",
                    "role_id = ? AND group_id = ?", condition_data, null, "", 0);

            return data != null && data.get("role_id") != null && !data.get("role_id").isEmpty();

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> roles_data = getCollectionMongo(table_groups, "roles",
                    new Document("id", group_id));

            if (roles_data == null) {
                return false;
            }

            for (Map<String, Object> role : roles_data) {
                if (Long.parseLong(String.valueOf(role.get("role_id"))) == role_id) {
                    return true;
                }
            }

            return false;
        }
        return false;
    }

    protected boolean checkNotUniqueWithStream(List<Map<String, Object>> values, String key, Object key_to_check) {
        return values.stream().anyMatch(map ->
                String.valueOf(map.get(key)).equals(key_to_check));
    }

    protected List<Object> extract_all_content(List<Map<String, Object>> mongo_data, String to_extract) {
        List<Object> extracted_data = new ArrayList<>();
        for (Map<String, Object> map : mongo_data) {
            if (map.containsKey(to_extract)) {
                extracted_data.add(map.get(to_extract));
            }
        }
        return extracted_data;
    }

    protected List<Object> extract_all_content(Map<String, List<Object>> sql_data, String to_extract) {
        List<Object> extracted_data = new ArrayList<>();
        for (Map.Entry<String, List<Object>> map : sql_data.entrySet()) {
            if (map.getKey().equals(to_extract)) {
                extracted_data.add(map.getValue());
            }
        }
        return extracted_data;
    }

    protected boolean checkUnique(List<Map<String, Object>> mongo_data, Map<String, Object> values) {
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

    protected Map<String, List<Object>> transformMongoToSQL(int amount, List<Map<String, Object>> mongoData,
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



    protected List<Long> getUserRolesID(long user_id, long group_id) {
        // we assume that user_id and group_id are valid
        List<Long> roles = new ArrayList<>();
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);
            condition_data.add(user_id);

            Map<String, List<Object>> roles_data = getDataSQL(table_group_members, "roles_id",
                    "group_id = ? AND member_id = ?",
                    condition_data, null, "", 0);

            if (roles_data == null || roles_data.isEmpty() || roles_data.get("roles_id").isEmpty()) {
                return null;
            }

            for (String role_id_text : String.valueOf(roles_data.get("roles_id").get(0)).split(",")) {
                if (!role_id_text.isBlank()) {
                    roles.add(Long.parseLong(role_id_text));
                }
            }
            return roles;

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> members_data = getCollectionMongo(table_groups, "members",
                    new Document("id", group_id));

            if (members_data == null || members_data.isEmpty() || members_data.get(0).isEmpty()) {
                return null;
            }

            for (Map<String, Object> member : members_data) {
                try {
                    if (Long.parseLong(String.valueOf(member.get("member_id"))) == user_id) {
                        for (String role_id_text : String.valueOf(member.get("roles_id")).split(",")) {
                            if (!role_id_text.isBlank()) {
                                roles.add(Long.parseLong(role_id_text));
                            }
                        }
                        return roles;
                    }
                } catch (Exception e) {
                    return null;
                }
            }

        }
        return null;
    }

    protected Map<String, Boolean> calculateRolePermissions(List<Long> roles_id, long group_id) {
        // name of permission : does the roles provide it?
        if (roles_id == null) {
            return null;
        }

        Map<String, Boolean> permissions = new HashMap<>();
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            for (long role_id : roles_id) {
                condition_data.clear();
                condition_data.add(role_id);
                condition_data.add(group_id);

                Map<String, List<Object>> role_data = getDataSQL(table_group_roles, "permissions",
                        "role_id = ? AND group_id = ?", condition_data, null, "", 0);

                if (role_data == null || role_data.isEmpty() || role_data.get("permissions") == null ||
                        role_data.get("permissions").isEmpty()) {
                    return null;
                }

                Map<String, Object> permission_data = API.jwtService.getDataNoEncryption(String.valueOf(role_data
                        .get("permissions").get(0))); // jwt token

                // expected {"permission_name": true/false, ...}

                if (permission_data == null) {
                    return null;
                }

                permissions = OverridePermissions(permissions, permission_data.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (Boolean) entry.getValue(),
                                (existing, replacement) -> existing,
                                HashMap::new
                        )));
            }

            return permissions;

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> roles_data = getCollectionMongo(table_groups, "roles",
                    new Document("id", group_id));

            if (roles_data == null || roles_data.isEmpty() || roles_data.get(0).isEmpty()) {
                return null;
            }

            for (Map<String, Object> role : roles_data) {
                try {
                    if (roles_id.contains(Long.parseLong(String.valueOf(role.get("role_id"))))) {
                        Map<String, Object> permission_data = API.jwtService.getDataNoEncryption(String.valueOf(role
                                .get("permissions"))); // jwt token

                        if (permission_data == null) {
                            return null;
                        }

                        permissions = OverridePermissions(permissions, permission_data.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> (Boolean) entry.getValue(),
                                        (existing, replacement) -> existing,
                                        HashMap::new
                                )));
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            return permissions;
        }
        return null;
    }

    protected Map<Long, Map<String, Boolean>> calculateChannelPermissions(long channel_id, long group_id) {
        Map<Long, Map<String, Boolean>> permissions = new HashMap<>();
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(channel_id);
            condition_data.add(group_id);

            Map<String, List<Object>> channel_data = getDataSQL(table_group_channels, "permissions",
                        "channel_id = ? AND group_id = ?", condition_data, null, "", 0);

            if (channel_data == null) {
                return null;
            }

            if (channel_data.isEmpty() || channel_data.get("permissions") == null ||
                    channel_data.get("permissions").isEmpty()) {
                return permissions;
            }

            String channel_permissions_text = String.valueOf(channel_data.get("permissions").get(0));
            if (channel_permissions_text.isBlank()) {
                return permissions;
            }

            Map<String, Object> permission_data = API.jwtService.getDataNoEncryption(channel_permissions_text); // jwt token

            // expected data: "role_id:: {"permission": true/false}, ...

            if (permission_data == null || permission_data.isEmpty()) {
                return permissions;
            }

            for (Map.Entry<String, Object> entry : permission_data.entrySet()) {
                try {
                    permissions.put(Long.parseLong(String.valueOf(entry.getKey())),
                            OverridePermissions((Map<String, Boolean>) entry.getValue()));
                } catch (Exception e) {
                    return null;
                }
            }
            return permissions;

        } else if (isMongo()) {
            List<Map<String, Object>> channel_data = getCollectionMongo(table_groups, "channels",
                    new Document("id", group_id));

            if (channel_data == null || channel_data.isEmpty() || channel_data.get(0).isEmpty()) {
                return permissions;
            }

            try {
                for (Map<String, Object> channel : channel_data) {
                    if (Long.parseLong(String.valueOf(channel.get("channel_id"))) == channel_id) {
                        String channel_permissions_text = String.valueOf(channel.get("permissions"));
                        if (channel_permissions_text.isBlank()) {
                            return permissions;
                        }

                        Map<String, Object> permission_data = API.jwtService.getDataNoEncryption(channel_permissions_text);// jwt token

                        if (permission_data == null) {
                            return null;
                        }

                        if (permission_data.isEmpty()) {
                            return permissions;
                        }

                        // expected data: role_id: {"permission": true/false}, ...

                        for (Map.Entry<String, Object> entry : permission_data.entrySet()) {
                            try {
                                permissions.put(Long.parseLong(String.valueOf(entry.getKey())),
                                        OverridePermissions((Map<String, Boolean>) entry.getValue()));
                            } catch (Exception e) {
                                return null;
                            }
                        }
                        return permissions;
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    protected Map<String, Boolean> OverridePermissions(Map<String, Boolean> role_permissions) {
        for (Map.Entry<String, Boolean> permission_entry : role_permissions.entrySet()) {
            String name_permission = permission_entry.getKey();
            if (!role_permissions.containsKey(name_permission)) {
                // that permission doesn't exist so save it.
                role_permissions.put(name_permission, permission_entry.getValue());

            } else if (!role_permissions.get(name_permission)) {
                // the current permission is false | Update it to true
                role_permissions.put(name_permission, permission_entry.getValue());
            }
        }

        return role_permissions;
    }

    protected Map<String, Boolean> OverridePermissions(Map<String, Boolean> permissions,
                                                    Map<String, Boolean> role_permissions) {
        for (Map.Entry<String, Boolean> permission_entry : role_permissions.entrySet()) {
            String name_permission = permission_entry.getKey();
            if (!permissions.containsKey(name_permission)) {
                // that permission doesn't exist so save it.
                permissions.put(name_permission, permission_entry.getValue());

            } else if (!permissions.get(name_permission)) {
                // the current permission is false | Update it to true
                permissions.put(name_permission, permission_entry.getValue());
            }
        }

        return permissions;
    }


    protected boolean doesUserHavePermissionsInChannel(long channel_id, List<String> needed_permissions,
                                                       long user_id, long group_id) {
        if (checkOwner(user_id, group_id)) { return true; }

        Map<Long, Map<String, Boolean>> channel_permissions = calculateChannelPermissions(channel_id, group_id);
        if (channel_permissions == null) { return false; }

        List<Long> roles_id = getUserRolesID(user_id, group_id);

        Map<String, Boolean> user_permissions = calculateRolePermissions(roles_id, group_id);
        if (user_permissions == null) { return false; }

        if (!channel_permissions.isEmpty()) {
            // channel has permission override | Only the channel's permission matter. (ofc after another override)
            for (long role_id : roles_id) {
                Map<String, Boolean> role_permissions = channel_permissions.get(role_id);
                if (role_permissions == null) {
                    continue;
                }
                for (Map.Entry<String, Boolean> permission : role_permissions.entrySet()) {
                    String key = permission.getKey();
                    if (!key.isBlank() && needed_permissions.contains(key)) {
                        return permission.getValue();
                    }
                }
            }

            // channel has no override for the user's role so use the role permissions.
        }
        return validateRolesPermissions(needed_permissions, user_permissions);
    }

    private static boolean validateRolesPermissions(List<String> needed_permissions, Map<String, Boolean> user_permissions) {
        short amount_of_matches = 0;
        for (String n_permission : needed_permissions) {
            if (!n_permission.isBlank() && user_permissions.containsKey(n_permission) &&
                    user_permissions.get(n_permission)) {
                amount_of_matches++;
            }
        }
        return amount_of_matches == needed_permissions.size();
    }

    protected boolean doesUserHavePermissions(List<String> needed_permissions, long user_id, long group_id) {
        if (checkOwner(user_id, group_id)) { return true; }

        Map<String, Boolean> user_permissions = calculateRolePermissions(getUserRolesID(user_id, group_id), group_id);
        if (user_permissions == null) { return false; }

        return validateRolesPermissions(needed_permissions, user_permissions);
    }


    protected boolean handleMessage(long channel_id, long sender_id, String message, LocalDateTime now,
                                    long resiver_id) {
        if (isSQL()) {
            List<Object> edit_condition_data = new ArrayList<>();
            edit_condition_data.add(channel_id);

            Map<String, List<Object>> current_chat_data = getDataSQL(table_chats,
                    "msg, msg_id, send_by",
                    "channel_id = ?",
                    edit_condition_data, null, "send_at DESC", 0);

            if (current_chat_data == null || current_chat_data.get("msg_id") == null ||
                    current_chat_data.get("send_by") == null || current_chat_data.get("msg") == null) {
                return false;
            }

            edit_condition_data.add(sender_id);

            // MESSAGE END -> {"p":true,"r":message_id}   pinned and replying to message id
            // MESSAGE END -> {"r":message_id}     only replying to message id
            // MESSAGE END -> {"p":true}                only pinned  (p) is always true
            // MESSAGE END -> {}                no data at all

            // Find all matches
            String message_json = getMessageJson(message);
            if (message_json == null) {
                message_json = "{}";
                message += message_json;
            }

            if (!current_chat_data.get("msg").isEmpty()) {
                List<Object> chat_data = new ArrayList<>();
                chat_data.add(channel_id);
                chat_data.add(message);
                chat_data.add(now.truncatedTo(ChronoUnit.MICROS));
                chat_data.add(sender_id);
                chat_data.add(generateID(current_chat_data.get("msg_id")));
                try {
                    long msg_id_to_concat = Long.parseLong(String.valueOf(current_chat_data.get("msg_id").get(0)));
                    String old_message = String.valueOf(current_chat_data.get("msg").get(0));
                    String old_message_json = getMessageJson(old_message);
                    if (old_message_json == null) {
                        old_message_json = "{}";
                    }

                    if (msg_id_to_concat == sender_id && old_message_json.equals(message_json)) {

                        String new_message = old_message.substring(0, old_message.length() - old_message_json.length())
                                + message.substring(0, message.length() - message_json.length()) + old_message_json;

                        List<Object> set_data = new ArrayList<>();
                        set_data.add(new_message);

                        edit_condition_data.add(msg_id_to_concat);
                        if (editDataSQL(table_chats, "msg = ?", set_data,
                                "channel_id = ? AND send_by = ? AND msg_id = ?",
                                edit_condition_data)) {
                            return true;
                        }
                    }

                    return addDataSQL(table_chats, "channel_id, msg, send_at, send_by, msg_id",
                            "?, ?, ?, ?, ?", chat_data);

                } catch (Exception e) {
                    return false;
                }

            } else {
                // no messages in this channel

                Map<String, List<Object>> channel_ids = getDataSQL(table_chats, "channel_id",
                        "", null, null, "", 0);

                if (channel_ids == null || channel_ids.get("channel_id") == null) {
                    return false;
                }

                List<Object> new_chat_data = new ArrayList<>();
                new_chat_data.add(channel_id == 0L ? generateID(channel_ids.get("channel_id")) : channel_id);
                new_chat_data.add(message);
                new_chat_data.add(now.truncatedTo(ChronoUnit.MICROS));
                new_chat_data.add(sender_id);
                new_chat_data.add(generateID(current_chat_data.get("msg_id")));

                return addDataSQL(table_chats, "channel_id, msg, send_at, send_by, msg_id",
                        "?, ?, ?, ?, ?", new_chat_data);

            }
        } else if (isMongo()) {
            Document convId = new Document("channel_id", channel_id);
            List<Map<String, Object>> chat_msgs = getCollectionMongo(table_chats, "msgs", convId);

            if (chat_msgs == null) {
                return false;
            }

            String send_at = now.format(formatter);
            String message_json = getMessageJson(message);
            if (message_json == null) {
                message_json = "{}";
                message += message_json;
            }

            if (chat_msgs.isEmpty() || chat_msgs.get(0).isEmpty()) {
                return MongoAddDataToCollectionNoSQL(table_chats,
                        new Document("channel_id", channel_id == 0L ?
                                generateID(new ArrayList<>()) : channel_id)
                                .append("user1", sender_id)
                                .append("user2", resiver_id)
                                .append("msgs",
                                        Arrays.asList(new Document("msg", message)
                                                .append("send_by", sender_id)
                                                .append("send_at", send_at)
                                                .append("msg_id", generateID(new ArrayList<>())))),
                        null);

            } else {
                List<Object> all_ids = extract_all_content(chat_msgs, "msg_id");

                LocalDateTime mostRecentDate = null;
                Long resent_msg_id = null;
                String old_message = "";
                Long message_sender_id = null;

                try {
                    for (Map<String, Object> map : chat_msgs) {
                        LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(map.get("send_at")), formatter);
                        if (mostRecentDate == null || dateTime.isBefore(mostRecentDate)) {
                            mostRecentDate = dateTime;
                            resent_msg_id = Long.valueOf(String.valueOf(map.get("msg_id")));
                            old_message = String.valueOf(map.get("msg"));
                            message_sender_id = Long.valueOf(String.valueOf(map.get("send_by")));
                        }
                    }
                } catch (Exception e) {
                    return false;
                }

                if (message_sender_id == sender_id && message_json.equals("{}")) {
                    try {
                        String new_message = old_message + message.substring(0, message.length() - 2) + message_json;

                        for (int i = 0; i < chat_msgs.size(); i++) {
                            Map<String, Object> current_msg = chat_msgs.get(i);
                            if (Long.valueOf(String.valueOf(current_msg.get("send_by"))) == sender_id &&
                                    Long.valueOf(String.valueOf(current_msg.get("msg_id"))) == resent_msg_id) {
                                current_msg.put("msg", new_message);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        return false;
                    }

                } else {
                    Map<String, Object> new_msg = new HashMap<>();
                    new_msg.put("send_by", sender_id);
                    new_msg.put("msg_id", generateID(all_ids));
                    new_msg.put("send_at", send_at);
                    new_msg.put("msg", message);

                    chat_msgs.add(new_msg);

                }
                return MongoUpdateDocumentInCollectionNoSQL(table_chats, convId, new Document("msgs", chat_msgs));

            }

        }
        return false;
    }

    protected String getMessageJson(String message) {
        String lastMatch = null;
        Matcher matcher = Pattern.compile("\\{.*?\\}").matcher(message);
        while (matcher.find()) {
            try {
                lastMatch = matcher.group();
            } catch (Exception e) {
                return null;
            }
        }
        return lastMatch;
    }

    protected boolean messageDeletionMongo(long channel_id, List<Map<String, Object>> the_messages, long msg_id) {
        MongoDeleteDataFromCollectionNoSQL(table_reactions, new Document("channel_id", channel_id)
                .append("msg_id", msg_id).append("post_id", 0l));

        return MongoUpdateDocumentInCollectionNoSQL(table_chats, new Document("channel_id", channel_id),
                new Document("msgs", the_messages));
    }

    protected boolean messageDeletionSQL(long channel_id, long message_id, long sender_id) {
        List<Object> condition_data = new ArrayList<>();
        condition_data.add(channel_id);
        condition_data.add(message_id);

        deleteDataSQL(table_reactions, "channel_id = ? AND msg_id = ? AND post_id = 0", condition_data);

        condition_data.clear();
        condition_data.add(channel_id);
        condition_data.add(message_id);
        condition_data.add(sender_id);

        return deleteDataSQL(table_chats,"channel_id = ? AND msg_id = ? AND send_by = ?", condition_data);
    }

    protected boolean handleReactionsMongo(long channel_id, long message_id, long post_id, String reaction, long actor_id) {
        Document data = new Document("channel_id", channel_id)
                .append("reaction", reaction)
                .append("msg_id", message_id).append("post_id", post_id).append("member_id", actor_id);

        List<Map<String, Object>> reaction_data = MongoReadCollectionNoSQL(table_reactions, data, false);
        try {
            if (!reaction_data.isEmpty()) {
                reaction_data.get(0).get("reaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return MongoAddDataToCollectionNoSQL(table_reactions, data,null);
    }

    protected Map<String, Boolean> calculateGroupSettings(long group_id) {
        Map<String, Boolean> settings = new HashMap<>();
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> group_settings = getDataSQL(table_groups, "settings",
                    "id = ?", condition_data, null, "", 0);

            if (group_settings == null || group_settings.isEmpty()) {
                return null;
            }

            if (group_settings.get("settings") == null || group_settings.get("settings").isEmpty()) {
                return settings;
            }

            String settings_value_text = String.valueOf(group_settings.get("settings").get(0));
            if (settings_value_text.isBlank() || settings_value_text.equals("null")) {
                return settings;
            }

            // expecting:  "option": true/false, ..

            Map<String, Object> settings_data = API.jwtService.getDataNoEncryption(settings_value_text);
            if (settings_data == null) {
                return null;
            }

            return settings_data.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> (Boolean) entry.getValue(),
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));



        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> group_data = MongoReadCollectionNoSQL(table_groups,
                    new Document("id", group_id), true, "settings");

            if (group_data == null || group_data.isEmpty() || group_data.get(0) == null) {
                return null;
            }

            if (group_data.get(0).isEmpty() || group_data.get(0).get("settings") == null) {
                return settings;
            }

            String settings_value_text = String.valueOf(group_data.get(0).get("settings"));
            if (settings_value_text.isBlank() || settings_value_text.equals("null")) {
                return settings;
            }

            Map<String, Object> settings_data = API.jwtService.getDataNoEncryption(settings_value_text);
            if (settings_data == null) {
                return null;
            }

            return settings_data.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> (Boolean) entry.getValue(),
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));
        }
        return null;
    }

    protected boolean checkGroupAllows(Map<String, Boolean> group_settings, List<String> need_to_allow) {
        if (group_settings == null) { return false; }
        if (group_settings.isEmpty()) {
            return true;
        }
        short match = 0;
        for (Map.Entry<String, Boolean> settings : group_settings.entrySet()) {
            if (need_to_allow.contains(settings.getKey()) && settings.getValue()) {
                match++;
            }
        }
        return match == need_to_allow.size();
    }

    protected boolean checkOwner(long user_id, long group_id) {
        if (isSQL()) {
            List<Object> condition_data = new ArrayList<>();
            condition_data.add(group_id);

            Map<String, List<Object>> group_data = getDataSQL(table_groups, "owner_id",
                    "id = ?", condition_data, null, "", 0);

            try {
                return Long.parseLong(String.valueOf(group_data.get("owner_id").get(0))) == user_id;

            } catch (Exception e) {
                return false;
            }

        } else if (mongoClient != null && mongoDatabase != null) {
            List<Map<String, Object>> group_data = MongoReadCollectionNoSQL(table_groups,
                    new Document("id", group_id), true, "owner_id");

            try {
                return Long.parseLong(String.valueOf(group_data.get(0).get("owner_id"))) == user_id;

            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }


    protected boolean updateGroupLogs(long actor_id, long group_id, String log_message, LocalDateTime now,
                                    String log_type) {
        if (isSQL()) {
            List<Object> log_data = new ArrayList<>();
            log_data.add(group_id);
            log_data.add(actor_id);
            log_data.add(log_type);
            log_data.add(log_message);
            log_data.add(now.truncatedTo(ChronoUnit.MINUTES));

            return addDataSQL(table_group_logs,"group_id, actor_id, log_type, log_message, acted_at",
                    "?, ?, ?, ?, ?", log_data);

        } else if (mongoClient != null && mongoDatabase != null) {

            List<Map<String, Object>> collection = getCollectionMongo(table_groups, "logs",
                    new Document("id", group_id));

            Map<String, Object> log = new HashMap<>();
            log.put("actor_id", actor_id);
            log.put("log_type", log_type);
            log.put("log_message", log_message);
            log.put("acted_at", now.format(formatter));

            collection.add(log);

            return MongoUpdateDocumentInCollectionNoSQL(table_groups,new Document("id", group_id),
                    new Document("logs", collection));
        }

        return false;
    }

    protected boolean handleMemberLeaveGroupMongo(long member_id, long group_id, String leave_type, String log_message,
                                                  LocalDateTime now, Document filter,
                                                  List<Map<String, Object>> all_members) {
        for (int i = 0; i < all_members.size(); i++) {
            if (Long.valueOf(String.valueOf(all_members.get(i).get("member_id"))) == member_id) {
                all_members.remove(i);
                break;
            }
        }

        return MongoUpdateDocumentInCollectionNoSQL(table_groups,
                filter, new Document("members", all_members)) &&
                updateGroupLogs(member_id, group_id, log_message, now, leave_type);
    }


    protected List<Map<String, Object>> MongoUpdateValueInCollection(List<Map<String, Object>> collection,
                                                                          String entry_id, long check_id,
                                                                          String old_value, String new_value,
                                                                          boolean toRemove) {
        try {
            for (int i = 0; i < collection.size(); i++) {
                Map<String, Object> entry = collection.get(i);
                if (Long.valueOf(String.valueOf(entry.get(entry_id))) == check_id) {
                    if (toRemove) {
                        collection.remove(i);
                    } else {
                        entry.put(old_value, new_value);
                    }
                    break;
                }
            }
            return collection;

        } catch (Exception e) {
            return collection;
        }
    }

    protected boolean isSQL() {
        return postgressql_connection != null || mysql_connection != null;
    }

    public boolean isMongo() {
        return mongoClient != null && mongoDatabase != null;
    }

    protected boolean handleReactionsSQL(long channel_id, long message_id, long post_id, String reaction, long actor_id) {
        List<Object> addData = new ArrayList<>();
        addData.add(channel_id);
        addData.add(reaction);
        addData.add(message_id);
        addData.add(post_id);
        addData.add(actor_id);

        /*
        Map<String, List<Object>> reaction_data = getDataSQL(table_reactions,
                "reaction",
                "channel_id = ? AND reaction = ? AND msg_id = ? AND post_id = ? AND member_id = ?", addData,
                null, "", 0);

        if (reaction_data == null || !reaction_data.get("reaction").isEmpty()) {
            // user already reacted
            return false;
        }

         */

        return addDataSQL(table_reactions, "channel_id, reaction, msg_id, post_id, member_id",
                "?, ?, ?, ?, ?", addData);
    }


}
