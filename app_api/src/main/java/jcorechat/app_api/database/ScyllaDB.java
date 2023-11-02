package jcorechat.app_api.database;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import jcorechat.app_api.API;

import java.net.InetSocketAddress;


import com.datastax.oss.driver.api.core.cql.ResultSet;

class ScyllaDB {

    private final String scylladb_host = "129.152.4.113";

    private final int scylladb_port = 9042;

    private final String scylladb_datacenter = "datacenter1";

    private CqlSession scylladb_session = null;

    public ScyllaDB() {
        try {
            scylladb_session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(scylladb_host, scylladb_port))
                    .withLocalDatacenter(scylladb_datacenter).build();
            // Execute a simple query
            ResultSet resultSet = scylladb_session.execute("SELECT * FROM system.local");

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


    void close() {
        try {
            scylladb_session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
