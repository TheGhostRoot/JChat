package jcorechat.app_api;


import java.io.FileInputStream;
import java.util.HashMap;

public class ConfigManager {


    private final int defaultPort = 25533;
    private HashMap<String, Object> config;

    private HashMap<String, Object> app_api_config;

    public ConfigManager() {
        try {
            config = API.yaml.load(new FileInputStream("config.yml"));
        } catch (final Exception e) {
            config = null;
            API.logger.error("config.yml is not found or can't read it!"); }

        app_api_config = null != config ? (HashMap<String, Object>) config.get("app_api") : null;
    }

    public ConfigManager(final String pathToConfig) {
        try {
            config = API.yaml.load(new FileInputStream(pathToConfig));
        } catch (final Exception e) {
            config = null;
            API.logger.error("{} is not found or can't read it!", pathToConfig); }

        app_api_config = null != config ? (HashMap<String, Object>) config.get("app_api") : null;
    }


    public int getServerPort() {
        if (null != app_api_config) {
            final Object portObj = app_api_config.get("port");
            return null == portObj ? defaultPort : ((Integer) portObj).intValue();
        } else {
            // no such config. Return default value: 25533
            return defaultPort;
        }
    }
}
