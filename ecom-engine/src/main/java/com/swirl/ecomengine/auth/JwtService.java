package com.swirl.ecomengine.auth;

import com.swirl.ecomengine.user.User;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String generateToken(User user) {
        return "dummy-jwt-token";
    }
}
