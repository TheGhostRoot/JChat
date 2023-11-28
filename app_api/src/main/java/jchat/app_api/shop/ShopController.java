package jchat.app_api.shop;


import jakarta.servlet.http.HttpServletRequest;
import jchat.app_api.API;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(path = "/api/v"+ API.API_VERSION+"/shop")
public class ShopController {


    @GetMapping
    public String getItemsFromShop(HttpServletRequest request) {
        // i -> all items
        // a -> amount

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

        int amount;
        try {
            amount = Integer.parseInt((String) data.get("a"));
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("i", API.databaseHandler.getItemsFromShop(amount));

        return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
    }


    @PostMapping
    public String addItemToShop(HttpServletRequest request) {
        // n -> item name
        // t -> item type
        // p -> item price
        // i -> item ID

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
        if (data == null || !data.containsKey("n") || !data.containsKey("t")) {
            return null;
        }

        String item_name = String.valueOf(data.get("n"));
        String item_type = String.valueOf(data.get("t"));
        int item_price;
        try {
            item_price = Integer.parseInt(String.valueOf(data.get("p")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.addItemToShop(item_type, item_name, item_price, user_id)) {
            Long item_id = API.databaseHandler.getItemFromShopByDetails(user_id, item_name, item_type, item_price);
            if (item_id == null) {
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("i", item_id);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }
        return null;
    }


    @PatchMapping
    public String updateItemFromShop(HttpServletRequest request) {
        // m -> what to be updated about the item
        // i -> item ID
        // s -> server stats
        // n -> updated name
        // p -> updated price
        // t -> updated type

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
        if (data == null || !data.containsKey("m")) {
            return null;
        }

        long item_id;
        try {
            item_id = Long.parseLong(String.valueOf(data.get("i")));
        } catch (Exception e) {
            return null;
        }

        switch (String.valueOf(data.get("m"))) {
            case "n" -> {
                // update item name
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateItemNameInShop(item_id, String.valueOf(data.get("n"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "t" -> {
                // update item type
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateItemTypeInShop(item_id, String.valueOf(data.get("t"))));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            case "p" -> {
                // update item price
                int item_price;
                try {
                    item_price = Integer.parseInt(String.valueOf(data.get("p")));
                } catch (Exception e) {
                    return null;
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("s", API.databaseHandler.updateItemPriceInShop(item_id, item_price));

                return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
            }
            default -> {
                return null;
            }
        }
    }


    @DeleteMapping
    public String deleteItemFromShop(HttpServletRequest request) {
        // only session
        // s -> server stats
        // i -> item ID

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

        long item_id;
        try {
            item_id = Long.parseLong(String.valueOf(data.get("i")));
        } catch (Exception e) {
            return null;
        }

        if (API.databaseHandler.removeItemToShop(item_id)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("s", true);

            return API.jwtService.generateUserJwt(claims, user_sign_key, user_encryp_key);
        }
        return null;
    }
}
