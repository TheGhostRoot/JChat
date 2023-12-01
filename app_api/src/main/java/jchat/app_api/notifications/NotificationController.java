package jchat.app_api.notifications;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/notifications")
public class NotificationController {


    @GetMapping
    public String getNotificationsFroUser(HttpServletRequest request) {
        // only session
        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        Map<String, List<Object>> all_notifications = API.databaseHandler.getNotifications(user_id);
        if (all_notifications == null) {
            return null;
        }

        API.databaseHandler.removeNotifications(user_id);

        Map<String, Object> claims = new HashMap<>();
        claims.put("notifi", all_notifications);

        return API.jwtService.generateUserJwt(claims, String.valueOf(user_data.get(API.DB_SIGN_KEY)),
                String.valueOf(user_data.get(API.DB_ENCRYP_KEY)));
    }
}
