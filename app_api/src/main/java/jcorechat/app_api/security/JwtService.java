package jcorechat.app_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jcorechat.app_api.API;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;


@Service
public class JwtService {

    private final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

    private final String GlobalSECRET_KEY = "hGqlbRo8IbgSh24eblzVZWnOk9Iue9cXKegLhnHAGyKV9HkKhmYQPE2QBpxfJmfri9UO7iAj9mZhJhm6E4Fx4Wxv5m/cHaxKASn0duiwBMHYt0ZEa6ViOFr2b62hVBfSQS3xvC0XDqRx+5rAG+vDwvoAUTSsT9Owhd9KJnrWEmJv0rrpY0+4qQbcRKbPhWJrB3ULWjnQuRvJS2Hwr7P/AvIrnFngC9QtNDOvLj/lzG9gHA5MSHws+/a2ZAe2mAI0AAvfYEPwemZy0r9JhHhqi+zcpFTarRqTEP51fXtjwRSoLgcbXxIbh5awM6h05+83NQV8L3cMfpANOyNATO/bBqzg+nU+y69AtVmpjXZpMaqXFAhUqVoVsuHP2Nc6UhPfjkps5Pt6Ho2kjEJotf1cDBXX6RTTxhJ95aL/lHKpNVw/sEBuzwyOqFwp1BMNuzED";

    private final SecretKey GlobalSignInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(GlobalSECRET_KEY));

    public JwtService() {
        API.logger.info("Global Sign Key: "+Base64.getEncoder().encodeToString(GlobalSignInKey.getEncoded()));
    }

    public Map<String, Object> getData(final String jwt, final String... key) {
        // key[0]  - user encryption key
        // key[1]  - user sign key
        Claims all = key.length != 2 ? getGlobalClaims(API.cription.GlobalDecrypt(jwt)) :
                getUserClaims(API.cription.UserDecrypt(jwt, key[0]), key[1]);
        return null != all ? new HashMap<>(all) : null;
    }
    private Claims getGlobalClaims(final String jwt) {
        if (jwt == null) { return null; }
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith((SecretKey) GlobalSignInKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Claims getUserClaims(final String jwt, final String SignKey) {
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

    public String generateGlobalJwt(final Map<String, Object> claims, final boolean encrypted) {
        return encrypted ? API.cription.GlobalEncrypt(Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS512)    // For python we use HS256  , but we will change it to ES512
                .compact()) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(GlobalSignInKey, SignatureAlgorithm.HS512)
                .compact();

    }

    public String generateUserJwt(final Map<String, Object> claims,
                                  final String SignatureKey, final boolean encrypted, final String... key) {
        return encrypted && key.length == 1 ? API.cription.UserEncrypt(Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS512)
                .compact(), key[0]) : Jwts.builder().claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SignatureKey)), SignatureAlgorithm.HS512)
                .compact();

    }


    private String generateKey() {
        StringBuilder sb = new StringBuilder(50);

        for (int i = 0; i < 50; i++) {
            // Math.random();
            sb.append(AlphaNumericString.charAt((int) (AlphaNumericString.length() * API.random.nextDouble())));
        }

        return sb.toString();
    }

    public String generateRandomUserKey() {
        String key = generateKey();
        while (API.sign_user_keys.containsValue(key)) {
            key = generateKey();
        }
        return key;
    }

}
