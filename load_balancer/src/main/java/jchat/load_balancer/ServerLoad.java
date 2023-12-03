package jchat.load_balancer;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerLoad {

    private ObjectMapper objectMapper;

    public ServerLoad() {
        objectMapper = new ObjectMapper();
    }

    public void setServerLoad() {
        Map<String, Map<String, Object>> server_load = new HashMap<>();
        for (String ip : LoadBalancer.servers) {
            try {
                Map<String, Object> map = objectMapper.readValue(RequestManager.post(ip + "/api/v1/update", null),
                        new TypeReference<Map<String, Object>>() {
                        });

                if ((LoadBalancer.critical_ram  != 0l &&
                        LoadBalancer.critical_ram <= Long.parseLong(String.valueOf(map.get("used_ram_jvm")))) ||
                        (LoadBalancer.critical_cpu != 0.0 &&
                                LoadBalancer.critical_cpu <= Double.parseDouble(String.valueOf(map.get("cpu_usage")))) ||
                        (LoadBalancer.critical_disk != 0l
                                && LoadBalancer.critical_disk <= Long.parseLong(String.valueOf(map.get("used_disk_bytes"))))) {
                    continue;

                }

                server_load.put(ip, map);

            } catch (Exception e) {}
        }

        server_load = sortServers(server_load);

        List<String> list = new ArrayList<>();
        list.addAll(server_load.keySet());

        if (LoadBalancer.useServer1) {
            LoadBalancer.servers2 = list;
            LoadBalancer.useServer1 = false;

        } else {
            LoadBalancer.servers1 = list;
            LoadBalancer.useServer1 = true;
        }
    }

    public Map<String, Map<String, Object>> sortServers(Map<String, Map<String, Object>> mainMap) {
        List<Map.Entry<String, Map<String, Object>>> entryList = new ArrayList<>(mainMap.entrySet());

        entryList.sort(Comparator.comparing(entry -> {
            Map<String, Object> nestedMap = entry.getValue();

            Long usedRam = (Long) nestedMap.get("used_ram_jvm");
            Double cpuUsage = (Double) nestedMap.get("cpu_usage");
            Long usedDiskBytes = (Long) nestedMap.get("used_disk_bytes");

            int compareResult = usedRam.compareTo(0L);
            if (compareResult == 0) {
                compareResult = cpuUsage.compareTo(0.0);
                if (compareResult == 0) {
                    compareResult = usedDiskBytes.compareTo(0L);
                }
            }
            return compareResult;
        }));

        Map<String, Map<String, Object>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
