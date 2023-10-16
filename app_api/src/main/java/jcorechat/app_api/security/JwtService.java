package jcorechat.app_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jcorechat.app_api.API;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtService {

    private final String SECRET_KEY = "hGqlbRo8IbgSh24eblzVZWnOk9Iue9cXKegLhnHAGyKV9HkKhmYQPE2QBpxfJmfri9UO7iAj9mZhJhm6E4Fx4Wxv5m/cHaxKASn0duiwBMHYt0ZEa6ViOFr2b62hVBfSQS3xvC0XDqRx+5rAG+vDwvoAUTSsT9Owhd9KJnrWEmJv0rrpY0+4qQbcRKbPhWJrB3ULWjnQuRvJS2Hwr7P/AvIrnFngC9QtNDOvLj/lzG9gHA5MSHws+/a2ZAe2mAI0AAvfYEPwemZy0r9JhHhqi+zcpFTarRqTEP51fXtjwRSoLgcbXxIbh5awM6h05+83NQV8L3cMfpANOyNATO/bBqzg+nU+y69AtVmpjXZpMaqXFAhUqVoVsuHP2Nc6UhPfjkps5Pt6Ho2kjEJotf1cDBXX6RTTxhJ95aL/lHKpNVw/sEBuzwyOqFwp1BMNuzED";

    private final Key SignInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

    public Map<String, Object> getData(String jwt) {
        Claims all = getAllClaims(jwt);
        return null != all ? new HashMap<>(all) : new HashMap<>();
    }

    private Claims getAllClaims(String jwt) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SignInKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }



    public String generateEncryptJwt(Map<String, Object> claims, String data) {

        if (!API.cription.canRun()) { return null; }

        return API.cription.encrypt(Jwts.builder().claims(claims)
                .subject(data)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignInKey, SignatureAlgorithm.ES512)
                .compact());

    }

    public String generateJwt(Map<String, Object> claims, String data) {
        if (!API.cription.canRun()) { return null; }
        return Jwts.builder().claims(claims)
                .subject(data)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignInKey, SignatureAlgorithm.ES512)
                .compact();

    }

    public String generateJwt(String data) {
        return generateJwt(new HashMap<>(), data);
    }

    private <T> T getClaim(String jwt, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaims(jwt);
        return null != claims ? claimsResolver.apply(claims) : null;
    }

}
