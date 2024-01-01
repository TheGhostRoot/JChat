package jchat.app_api;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class JChatRequestBody {
    private Map<String, Object> data = new HashMap<>();

    @JsonAnySetter
    public void set(String name, Object value) {
        data.put(name, value);
    }

    // Add getter and setter methods if needed

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
