package com.play.com.play.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Date;

public class TokenHandler {

    private static final Key key = MacProvider.generateKey();
    private static final String SUBJECT = "PlaygoundAppTokenSubject";
    private static final String ISSUER = "PlaygroundApp/admin";

    private static final String EXPIRED = "EXPIRED";
    private static final String VALID = "VALID";
    private static final String INVALID = "INVALID";

    public static String getValidStatus() {
        return VALID;
    }

    public static String getInvalidStatus() {
        return INVALID;
    }

    public static String getExpiredStatus() {
        return EXPIRED;
    }

    public static final long SESSION_TIMEOUT = 300000; // 5 minutes

    public static String refreshToken(String oldToken) {
        String subject = getSubjectFromToken(oldToken);

        return buildToken(subject);
    }

    private static String getSubjectFromToken(String jwt) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody().getSubject();
    }

    private static String buildToken(String subject) {
        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + SESSION_TIMEOUT);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(ISSUER)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public static String getAuthToken(String subject) {
        String token = buildToken(subject);

        return token;
    }

    public static String verifyToken(String token) {

        Jws<Claims> jws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
        String subject = jws.getBody().getSubject();
        String issuer = jws.getBody().getIssuer();
        Date exp = jws.getBody().getExpiration();
        Date issuedAt = jws.getBody().getIssuedAt();
        String algorithm = jws.getHeader().getAlgorithm();

        if (StringUtils.equals(issuer, ISSUER) && StringUtils.equals(algorithm, SignatureAlgorithm.HS512.getValue())) {

            long expMinusNow = Math.abs(exp.getTime() - System.currentTimeMillis());
            long tokenAge = Math.abs(exp.getTime() - issuedAt.getTime());

            if (tokenAge <= SESSION_TIMEOUT && expMinusNow < SESSION_TIMEOUT && exp.after(new Date())) {
                return VALID;
            } else {
                return EXPIRED;
            }

        } else {
            return INVALID;
        }
    }


}
