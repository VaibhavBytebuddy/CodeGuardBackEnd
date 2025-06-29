package com.codeguard.backend.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a safe key
    }


    public String generateToken(String email)
    {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractEmail(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJwt(token)
                .getBody()
                .getSubject();
    }
    public boolean isTokenValid(String token)
    {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJwt(token);
               return true;
        }catch (JwtException e)
        {
            return false;
        }
    }
}
