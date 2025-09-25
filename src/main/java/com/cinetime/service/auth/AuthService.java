package com.cinetime.service.auth;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.ResetPasswordRequestEmail;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final MailService mailService; // sizin kullandığınız servis
    private final SecureRandom rnd = new SecureRandom();
    private final PasswordEncoder encoder;


    public void forgotPassword(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String code = String.format("%06d", rnd.nextInt(1_000_000));

        // 1) Erste write to DB (overwrite)
        user.setResetPasswordCode(code);
        // user.setResetPasswordCodeExpiresAt(Instant.now().plus(10, MINUTES));
        userRepository.save(user);

        // 2) e-posta send (DB kaydı)
        mailService.sendResetCode(email, code);
    }

    public String resetPassword(ResetPasswordRequestEmail req) {
        String email = req.getEmail().trim().toLowerCase();
        String code  = req.getCode().trim();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String savedCode = user.getResetPasswordCode();
        if (savedCode == null || savedCode.isBlank()) {
            throw new ConflictException(ErrorMessages.RESET_CODE_REQUIRED);
        }
        if (!savedCode.equals(code)) {
            throw new ConflictException(ErrorMessages.INVALID_RESET_CODE);
        }

        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setResetPasswordCode(null); // kodu tüket
        userRepository.save(user);

        return SuccessMessages.PASSWORD_RESET_SUCCESS;
    }


}
