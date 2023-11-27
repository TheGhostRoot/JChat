package jchat.app_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchat.app_api.API;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            response.sendError(403);
            return;
        }

        String GlobalEncodedSessID = request.getHeader("SessionID");

        if (GlobalEncodedSessID != null) {
            String given_user_session_id_str = API.criptionService.GlobalDecrypt(GlobalEncodedSessID);
            if (null == given_user_session_id_str) {
                response.sendError(403);
                return;
            }

            long given_user_session_id;
            try {
                given_user_session_id = Long.parseLong(given_user_session_id_str);
            } catch (Exception e) {
                response.sendError(403);
                return;
            }

            Long user_id = API.databaseHandler.getUserIDbySessionID(given_user_session_id, API.get_IP(request));
            if (user_id == null) {
                response.sendError(403);
                return;
            }

            Map<String, Object> userByID = API.databaseHandler.getUserByID(user_id);
            if (userByID == null) {
                response.sendError(403);
                return;
            }

            Map<String, Object> data = API.jwtService.getData(authHeader,
                    String.valueOf(userByID.get("encryption_key")), String.valueOf(userByID.get("sign_key")));
            if (data == null) {
                response.sendError(403);
                return;
            }

            if (data.isEmpty()) {
                response.sendError(403);
                return;
            }
        }


        if (null == API.jwtService.getData(authHeader)) {
            response.sendError(403);
            return;
        }

        try {
            String decr = API.criptionService.GlobalDecrypt(request.getHeader("CapctchaID"));
            if (decr == null) {
                throw new Exception();
            }
            Long.parseLong(decr);
        } catch (Exception e) {
            response.sendError(403);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
