package jchat.app_api.profiles;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/profile")
public class ProfilesController {

    @GetMapping
    public String getProfile(HttpServletRequest request) {
        // only session
        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        String user_encryp_key = String.valueOf(user_data.get(API.DB_ENCRYP_KEY));
        String user_sign_key = String.valueOf(user_data.get(API.DB_SIGN_KEY));

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), user_encryp_key,
                user_sign_key);
        if (data == null) {
            return null;
        }

        Map<String, Object> profile_data = API.databaseHandler.getProfile(user_id);
        if (profile_data == null) {
            return null;
        }

        return API.jwtService.generateUserJwt(profile_data, user_sign_key, user_encryp_key);
    }


    @PatchMapping
    public String updateProfile(HttpServletRequest request) {
        // only session
        Long user_id = API.getUserID_SessionOnly(request);
        if (user_id == null) {
            return null;
        }

        Map<String, Object> user_data = API.databaseHandler.getUserByID(user_id);
        if (user_data == null) {
            return null;
        }

        String user_encryp_key = String.valueOf(user_data.get(API.DB_ENCRYP_KEY));
        String user_sign_key = String.valueOf(user_data.get(API.DB_SIGN_KEY));

        Map<String, Object> data = API.jwtService.getData(request.getHeader(API.REQ_HEADER_AUTH), user_encryp_key,
                user_sign_key);
        if (data == null || !data.containsKey("modif")) {
            return null;
        }

        switch (String.valueOf(data.get("modif"))) {
            case "pfp" -> {
                // update Pfp
                if (!data.containsKey("pfp")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfilePfp(user_id, String.valueOf(data.get("pfp"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "banner" -> {
                if (!data.containsKey("banner")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfileBanner(user_id, String.valueOf(data.get("banner"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "pets" -> {
                if (!data.containsKey("pets")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfilePets(user_id, String.valueOf(data.get("pets"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "coins" -> {
                if (!data.containsKey("coins")) {
                    return null;
                }
                int coins;
                try {
                    coins = Integer.parseInt(String.valueOf(data.get("coins")));
                } catch (Exception e) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfileCoins(user_id, coins));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "badges" -> {
                if (!data.containsKey("badges")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfileBadges(user_id, String.valueOf(data.get("badges"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "animations" -> {
                if (!data.containsKey("animations")) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("stats", API.databaseHandler.updateProfileAnimations(user_id, String.valueOf(data.get("animations"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }

}
