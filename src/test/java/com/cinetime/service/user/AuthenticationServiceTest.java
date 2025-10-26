package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.security.jwt.JwtService;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private SecurityHelper securityHelper;
    @Mock private Authentication authentication;

    @InjectMocks private AuthenticationService authenticationService;

    @BeforeEach void setup() { SecurityContextHolder.clearContext(); }
    @AfterEach  void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    void authenticate_ok() {
        // given
        var req = new LoginRequest();
        req.setPhoneOrEmail("user@example.com");
        req.setPassword("P@ssw0rd!");

        // 🔹 SecurityHelper User entity döndürür
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        when(securityHelper.loadByLoginProperty("user@example.com")).thenReturn(mockUser);

        // 🔹 Authentication.getPrincipal() -> UserDetails döner
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_MEMBER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        doReturn(authorities).when(userDetails).getAuthorities();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(authentication)).thenReturn("jwt-token-123");

        // when
        AuthenticationResponse res = authenticationService.authenticate(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getToken()).isEqualTo("jwt-token-123");
        assertThat(res.getUser()).isNotNull();
        assertThat(res.getUser().getUsername()).isEqualTo("user@example.com");
        assertThat(res.getUser().getRoles()).containsExactlyInAnyOrder("ROLE_MEMBER", "ROLE_ADMIN");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);

        verify(securityHelper).loadByLoginProperty("user@example.com");
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(authentication);
    }

    @Test
    void authenticate_badCredentials_propagatesException() {
        // given
        var req = new LoginRequest();
        req.setPhoneOrEmail("wrong@example.com");
        req.setPassword("bad");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when / then
        assertThatThrownBy(() -> authenticationService.authenticate(req))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).generateToken(any());
    }
}
