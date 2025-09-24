package com.cinetime.service.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.UserCreateRequest;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // U06 - Update Authenticated User
    public UserResponse updateAuthenticatedUser(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = (User) userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

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

        User user = (User) userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_DELETE_NOT_ALLOWED);
        }

        userRepository.delete(user);
        return SuccessMessages.USER_DELETED;
    }

    // U08 - Get Authenticated User
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<UserResponse> searchUsers(String q, Pageable pageable) {
        Page<User> users;

        if (q == null || q.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    q, q, q, pageable
            );
        }

        return users.map(UserMapper::toResponse);
    }



    // U09 - Get users (ADMIN or EMPLOYEE)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
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
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public UserResponse updateUserByAdminOrEmployee(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_UPDATE_NOT_ALLOWED);
        }

        UserMapper.updateEntityFromRequest(request, user);
        userRepository.save(user);

        return UserMapper.toResponse(user);
    }

   // U11 – Delete User by Admin or Employee
   @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
   public UserResponse deleteUserByAdminOrEmployee(Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

       if (Boolean.TRUE.equals(user.getBuiltIn())) {
           throw new ConflictException(ErrorMessages.BUILT_IN_USER_DELETE_NOT_ALLOWED);
       }

       userRepository.delete(user);

       return UserMapper.toResponse(user);
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

}




