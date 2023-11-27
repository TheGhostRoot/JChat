package jchat.app_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jchat.app_api.API;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JwtService {
    private final String GlobalSECRET_KEY = "hGqlbRo8IbgSh24eblzVZWnOk9Iue9cXKegLhnHAGyKV9HkKhmYQPE2QBpxfJmfri9UO7iAj9mZhJhm6E4Fx4Wxv5m/cHaxKASn0duiwBMHYt0ZEa6ViOFr2b62hVBfSQS3xvC0XDqRx+5rAG+vDwvoAUTSsT9Owhd9KJnrWEmJv0rrpY0+4qQbcRKbPhWJrB3ULWjnQuRvJS2Hwr7P/AvIrnFngC9QtNDOvLj/lzG9gHA5MSHws+/a2ZAe2mAI0AAvfYEPwemZy0r9JhHhqi+zcpFTarRqTEP51fXtjwRSoLgcbXxIbh5awM6h05+83NQV8L3cMfpANOyNATO/bBqzg+nU+y69AtVmpjXZpMaqXFAhUqVoVsuHP2Nc6UhPfjkps5Pt6Ho2kjEJotf1cDBXX6RTTxhJ95aL/lHKpNVw/sEBuzwyOqFwp1BMNuzED";

    private final SecretKey GlobalSignInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(GlobalSECRET_KEY));

    public JwtService() {
        API.logger.info("Global Sign Key: "+Base64.getEncoder().encodeToString(GlobalSignInKey.getEncoded()));
    }

    public Map<String, Object> getData(String jwt, String... key) {
        // key[0]  - user encryption key
        // key[1]  - user sign key
        Claims all = key.length != 2 ? getGlobalClaims(API.criptionService.GlobalDecrypt(jwt)) :
                getUserClaims(API.criptionService.UserDecrypt(jwt, key[0]), key[1]);
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
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS256)    // For python we use HS256  , but we will change it to ES512
                .compact()) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateJwtForDB(Map<String, Object> claims) {
        return Jwts.builder().claims(claims)
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateUserJwt(Map<String, Object> claims,
                                  String SignatureKey, boolean encrypted, String... key) {
        return encrypted && key.length == 1 ? API.criptionService.UserEncrypt(Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS256)
                .compact(), key[0]) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS256)
                .compact();

    }

}
