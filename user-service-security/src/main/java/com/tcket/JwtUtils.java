package com.tcket;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import java.util.Date;
import java.util.Map;
import java.util.UUID;


public class JwtUtils {

    @Inject
    private JwtBuilder jwtBuilder;



    private static final long EXPIRATION_TIME = 3600000;
    private static final String SECRET_KEY = "ThisIsASecureSecretKeyWithAtLeastSixtyFourCharactersforSecureConnection!!123456";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String username) {
        Map<String, Object> claims = Map.of("username", username, "roles", "admin", "sessionId", UUID.randomUUID().toString());
        return jwtBuilder
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
