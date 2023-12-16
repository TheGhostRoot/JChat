package jchat.app_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jchat.app_api.API;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JwtService {
    private final String GlobalSECRET_KEY = API.readGlobalSignFromConfig();

    private final SecretKey GlobalSignInKey = Keys.hmacShaKeyFor(GlobalSECRET_KEY.getBytes(StandardCharsets.UTF_8));


    public Map<String, Object> getData(String jwt, String EncryptionKey, String SignKey) {
        Claims all = EncryptionKey == null && SignKey == null ? getGlobalClaims(API.criptionService.GlobalDecrypt(jwt)) :
                getUserClaims(API.criptionService.UserDecrypt(jwt, EncryptionKey), SignKey);
        return null != all ? new HashMap<>(all) : null;
    }

    public Map<String, Object> getDataNoEncryption(String jwt) {
        Claims all = getGlobalClaims(jwt);
        return null != all ? new HashMap<>(all) : null;
    }

    private Claims getGlobalClaims(String jwt) {
        if (jwt == null) { return null; }
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(GlobalSignInKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Claims getUserClaims(String jwt, String SignKey) {
        if (jwt == null) { return null; }
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignKey)))
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public String generateGlobalJwt(Map<String, Object> claims, boolean encrypted) {
        return encrypted ? API.criptionService.GlobalEncrypt(Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS512)    // For python we use HS256  , but we will change it to ES512
                .compact()) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS512)
                .compact();

    }

    public String generateJwtForDB(Map<String, Object> claims) {
        return Jwts.builder().claims(claims)
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateUserJwt(Map<String, Object> claims, String SignatureKey, String EncryptionKey) {
        return EncryptionKey != null ? API.criptionService.UserEncrypt(Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS512)
                .compact(), EncryptionKey) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS512)
                .compact();

    }

}
