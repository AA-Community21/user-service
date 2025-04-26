package com.tcket;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtBuilder jwtUtils() {
        return Jwts.builder();
    }

    @Bean
    public JwtUtils getJwtUtils(){
        return new JwtUtils();
    }

}
