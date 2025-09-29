package com.cinetime.service.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.*;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.RoleService;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.SecurityHelper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final JavaMailSender mailSender;
    private final MailHelper mailHelper;
    private final SecurityHelper securityHelper;
    @Value("${app.mail.from}") private String mailForm;
    @Value("${app.mail.reset.subject}") private String resetSubject;
    @Value("${app.mail.reset.template}") private String resetTemplateHtml;


    // U06 - Update Authenticated User
    public UserResponse updateAuthenticatedUser(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = securityHelper.loadByLoginProperty(username);

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_UPDATE_NOT_ALLOWED);
        }

        UserMapper.updateEntityFromRequest(request, user);
        userRepository.save(user);

        return UserMapper.toResponse(user);
    }

    // U07 - Delete Authenticated User
    public String deleteAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = securityHelper.loadByLoginProperty(username);

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_DELETE_NOT_ALLOWED);
        }

        userRepository.delete(user);
        return SuccessMessages.USER_DELETED;
    }

    // U08 - Get Authenticated User
    public ResponseMessage<Page<UserResponse>> searchUsers(String q, Pageable pageable) {
        String k = (q == null) ? "" : q.trim();
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository
                .findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
                        q, q, q, q, pageable);

        Page<UserResponse> body = page.map(UserMapper::toResponse);

        return ResponseMessage.<Page<UserResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.USERS_LISTED) // örn: "Users listed successfully"
                .returnBody(body)
                .build();
    }


    // U09 - Get users (ADMIN or EMPLOYEE)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    // U02 - User Register
    public UserResponse saveUser(UserRegisterRequest req) {

        // 1) unique
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ConflictException(ErrorMessages.EMAIL_NOT_UNIQUE);

        if (userRepository.existsByPhoneNumber(req.getPhone()))
            throw new ConflictException(ErrorMessages.PHONE_NUMBER_NOT_UNIQUE);

        // 2) request -> entity (STATIC mapper)
        User user = UserMapper.fromRegisterRequest(req);   // <<< static call
        user.setEmail(user.getEmail().trim().toLowerCase());

        // 3) encode password
        user.setPassword(encoder.encode(user.getPassword()));

        // 4) default role = MEMBER
        Role member = roleRepository.findByRoleName(RoleName.MEMBER)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MEMBER_ROLE_MISSING));
        user.setRoles(Set.of(member));

        // 5) save + map to response (STATIC mapper)
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);               // <<< static call while mapper is not component
    }

    //U10-Update user by ADMIN or EMPLOYEE
    @Transactional
    public ResponseMessage<UserResponse> updateUserByAdminOrEmployee(Long userId, UserUpdateRequest request) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(target.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_UPDATE_NOT_ALLOWED);
        }

        Authentication caller = SecurityContextHolder.getContext().getAuthentication();

        // Employee can only  MEMBER update.
        if (securityHelper.isCallerEmployee(caller) && !securityHelper.userHasRole(target, RoleName.MEMBER)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DANIED);
        }

        UserMapper.updateUserFromRequest(request, target);
        userRepository.save(target);
        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_UPDATED)
                .httpStatus(HttpStatus.OK)
                .returnBody(UserMapper.toResponse(target))
                .build();
    }

    // U11 – Delete User by Admin or Employee
    @Transactional
    public ResponseMessage<UserResponse> deleteUserByAdminOrEmployee(Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

       if (Boolean.TRUE.equals(user.getBuiltIn())) {
           throw new ConflictException(ErrorMessages.BUILT_IN_USER_DELETE_NOT_ALLOWED);
       }

       Authentication caller = SecurityContextHolder.getContext().getAuthentication();

       if (securityHelper.isCallerEmployee(caller) && !securityHelper.userHasRole(user, RoleName.MEMBER)) {
           throw new AccessDeniedException(ErrorMessages.ACCESS_DANIED);
       }
        UserResponse body = UserMapper.toResponse(user);
       userRepository.delete(user);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_DELETED)
                .httpStatus(HttpStatus.OK)
                .returnBody(body)
                .build();
   }



    public ResponseMessage<UserCreateResponse> createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException(ErrorMessages.EMAIL_NOT_UNIQUE);

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new ConflictException(ErrorMessages.PHONE_NUMBER_NOT_UNIQUE);

        User user = userMapper.mapUserCreateRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Role role;
        if (isAdmin) {
            user.setBuiltIn(request.isBuiltIn());
            role = roleService.getRole(request.getRole());
        } else {
            user.setBuiltIn(false);
            role = roleService.getRole(RoleName.MEMBER);
        }
        user.setRoles(Set.of(role));
        userRepository.save(user);
        UserCreateResponse response = userMapper.mapUserToUserCreateResponse(user);

        return ResponseMessage.<UserCreateResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .returnBody(response)
                .build();
    }

    //U04-Reset Password
    @Transactional
    public void resetPasswordForAuthenticatedUser(ResetPasswordRequest request) {
        User user = getCurrentUser();

        // 1) Verify old password
        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessages.OLD_PASSWORD_MISMATCH); // use your constant
        }

        // 2) Optional extra validations (keep if useful; remove if you prefer minimal)
        if (encoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessages.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 3) Update
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Depending on your UserDetails implementation:
        // If principal holds email/phone/ID, adapt this logic accordingly.
        // Example assumes username = email:
        String username = auth.getName();
        return securityHelper.loadByLoginProperty(username);
    }

    //U03 Forgot-Reset Password Email
    public String forgotPassword(String rawEmail) {
        final String email = rawEmail.trim().toLowerCase();

        userRepository.findByLoginProperty(email).ifPresent(user -> {
            String code = generateSixDigitCode();
            user.setResetPasswordCode(code);
            userRepository.save(user);

            mailHelper.sendResetCodeEmail(user.getEmail(), code); // <—
        });

        return SuccessMessages.FORGOT_PASSWORD_EMAIL_SENT;
    }

    private String generateSixDigitCode() {
        var r = new java.security.SecureRandom();
        return String.format("%06d", r.nextInt(1_000_000));
    }


    public String resetPassword(ResetPasswordRequestEmail req) {
        final String email = req.getEmail().trim().toLowerCase();
        final String code  = req.getCode().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String savedCode = user.getResetPasswordCode();
        if (savedCode == null || savedCode.isBlank())
            throw new BadRequestException(ErrorMessages.RESET_CODE_REQUIRED);

        if (!savedCode.equals(code))
            throw new BadRequestException(ErrorMessages.INVALID_RESET_CODE);

        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setResetPasswordCode(null); // kod tüket
        userRepository.save(user);

        return SuccessMessages.PASSWORD_RESET_SUCCESS;
    }


}




