package com.swirl.ecomengine.auth;

import com.swirl.ecomengine.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiresAt;

    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}

