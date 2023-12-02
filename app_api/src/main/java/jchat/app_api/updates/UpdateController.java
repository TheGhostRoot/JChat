package jchat.app_api.updates;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;


@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/update")
public class UpdateController {


    @GetMapping
    public Map<String, Object> getAppStats(HttpServletRequest request) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mode", API.stats);
        stats.put("version", API.API_VERSION);

        return stats;
    }


    @PostMapping
    public Map<String, Object> getServerLoad(HttpServletRequest request) {
        Runtime rt = Runtime.getRuntime();

        long total_mem = rt.totalMemory();

        File file = new File("/");
        long totalSpace = file.getTotalSpace();

        Map<String, Object> load = new HashMap<>();
        load.put("total_ram_jvm", total_mem);
        load.put("used_ram_jvm", total_mem - rt.freeMemory());

        load.put("total_disk_bytes", totalSpace);
        load.put("used_disk_bytes", totalSpace - file.getFreeSpace());

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        load.put("cpu_usage", operatingSystemMXBean.getSystemLoadAverage());
        load.put("arch", operatingSystemMXBean.getArch());

        return load;
    }




}
