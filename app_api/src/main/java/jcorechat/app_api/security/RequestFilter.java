package jcorechat.app_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jcorechat.app_api.API;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (null == authHeader) {
            response.sendError(403);
            return;
        }

        final String GlobalEncodedSessID = request.getHeader("SessionID");

        if (null != GlobalEncodedSessID) {
            final String given_user_session_id_str = API.cription.GlobalDecrypt(GlobalEncodedSessID);
            if (null == given_user_session_id_str) {
                response.sendError(403);
                return;
            }

            long given_user_session_id;
            try {
                given_user_session_id = Long.valueOf(given_user_session_id_str);
            } catch (Exception e) {
                response.sendError(403);
                return;
            }



            final Long user_id = API.accountManager.get_UserID_By_AppSessionID(given_user_session_id);
            if (null == user_id) {
                response.sendError(403);
                return;
            }

            try {
                API.jwtService.getData(authHeader, API.accountManager.get_EncryptionKey_By_UserID(user_id),
                        API.accountManager.get_SignKey_By_UserID(user_id));
            } catch (Exception e) {
                response.sendError(403);
                return;
            }
        }


        if (null == API.jwtService.getData(authHeader)) {
            response.sendError(403);
            return;
        }

        final String GlobalEncodedCaptchaID = request.getHeader("CapctchaID");
        try {
            if (GlobalEncodedCaptchaID != null && !API.captcha_results.containsKey(Long.valueOf(API.cription.GlobalDecrypt(GlobalEncodedCaptchaID)))) {
                response.sendError(403);
                return;
            }
        } catch (Exception e) {
            response.sendError(403);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
