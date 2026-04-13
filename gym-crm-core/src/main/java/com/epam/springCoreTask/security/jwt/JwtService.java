package com.epam.springCoreTask.security.jwt;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(String username);
    String extractUsername(String token);
    Date extractExpiration(String token);
    boolean isTokenValid(String token, UserDetails userDetails);    
}
