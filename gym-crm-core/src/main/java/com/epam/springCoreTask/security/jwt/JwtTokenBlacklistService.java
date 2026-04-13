package com.epam.springCoreTask.security.jwt;

import java.util.Date;

public interface JwtTokenBlacklistService {
    void blacklist(String token, Date expiration);
    boolean isBlacklisted(String token);
}
