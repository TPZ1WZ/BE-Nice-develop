package com.proj.webprojrct.auth;

import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.entity.UserToken;
import com.proj.webprojrct.user.repository.UserTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationService {

    private final UserTokenRepository confirmationTokenRepository;

    public ConfirmationService(UserTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public Optional<UserToken> getConfirmationByTokenAndEmail(long token, String email) {
        return this.confirmationTokenRepository.findByTokenAndUserEmail(token, email);
    }

    public void ConfirmToken(long tokenNumber, String email) {
        this.confirmationTokenRepository.updateConfirmedAt(tokenNumber, LocalDateTime.now(), email);
    }

    public User findUserByResetPasswordToken(Long token) {
        return this.confirmationTokenRepository.findUserTokenByToken(token)
                .map(UserToken::getUser)
                .orElse(null);
    }

    public void removeToken(Long token) {
        this.confirmationTokenRepository.deleteUserTokenByToken(token);
    }

}
