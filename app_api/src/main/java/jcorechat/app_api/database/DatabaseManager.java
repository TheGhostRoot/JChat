package jcorechat.app_api.database;

public class DatabaseManager {

    protected static final String table_accounts = "accounts";
    protected static final String table_chats = "chats";
    protected static final String table_captchas = "captchas";
    protected static final String table_posts = "posts";
    protected static final String table_profiles = "profiles";
    protected static final String table_conversations = "conversations";


    private PostgresSQLDB postgresSQLDB = null;
    private MySQLDB mySQLDB = null;
    private MongoDB mongoDB = null;
    private ScyllaDB scyllaDB = null;



    public void setupPostgresSQL() {
        postgresSQLDB = new PostgresSQLDB();
    }

    public void setupMySQL() {
        mySQLDB = new MySQLDB();
    }

    public void setupMongoDB() {
        mongoDB = new MongoDB();
    }

    public void setupScyllaDB() {
        scyllaDB = new ScyllaDB();
    }

    public void shutDown() {
        if (postgresSQLDB != null) {
            postgresSQLDB.close();

        } else if (mySQLDB != null) {
            mySQLDB.close();

        } else if (mongoDB != null) {
            mongoDB.close();

        } else if (scyllaDB != null) {
            scyllaDB.close();

        }
    }

}
