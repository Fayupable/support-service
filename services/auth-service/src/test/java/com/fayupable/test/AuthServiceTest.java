//package com.fayupable.test;
//
//import com.fayupable.test.dto.user.UserDto;
//import com.fayupable.test.entity.UserCredential;
//import com.fayupable.test.entity.VerificationCode;
//import com.fayupable.test.enums.Role;
//import com.fayupable.test.exception.AlreadyExistException;
//import com.fayupable.test.exception.UserNotFoundException;
//import com.fayupable.test.mapper.user.UserMapper;
//import com.fayupable.test.repository.auth.IAuthRepository;
//import com.fayupable.test.request.login.LoginRequest;
//import com.fayupable.test.request.user.AddUserRequest;
//import com.fayupable.test.security.jwt.JwtUtils;
//import com.fayupable.test.security.user.AuthDetails;
//import com.fayupable.test.service.AuthService;
//import com.fayupable.test.service.VerificationCodeService;
//import com.fayupable.test.kafka.UserProducer;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AuthServiceTest {
//
//    @Mock
//    private IAuthRepository authRepository;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @Mock
//    private JwtUtils jwtUtils;
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private VerificationCodeService verificationCodeService;
//
//    @Mock
//    private UserProducer userProducer;
//
//    @Mock
//    private AuthDetails authDetails;
//
//    @InjectMocks
//    private AuthService authService;
//
//    private UserCredential user;
//    private UUID userId;
//    private String email;
//    private String password;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID();
//        email = "test@example.com";
//        password = "password123";
//
//        user = UserCredential.builder()
//                .userId(userId)
//                .email(email)
//                .password(password)
//                .roles(Set.of(Role.ROLE_USER))
//                .build();
//
//
//        // SecurityContext Ayarla
//        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//        Authentication authentication = new UsernamePasswordAuthenticationToken(authDetails, null, authDetails.getAuthorities());
//        securityContext.setAuthentication(authentication);
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    void testAddUser_Success() {
//        AddUserRequest request = new AddUserRequest();
//        request.setEmail(email);
//        request.setPassword(password);
//
//        when(authRepository.existsByEmail(email)).thenReturn(false);
//        when(authRepository.existsByUsername(request.getUsername())).thenReturn(false);
//        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
//        when(authRepository.save(any(UserCredential.class))).thenReturn(user);
//        when(userMapper.fromUser(any(UserCredential.class))).thenReturn(new UserDto());
//
//        UserDto response = authService.addUser(request);
//
//        assertNotNull(response);
//        verify(authRepository).save(any(UserCredential.class));
//        verify(verificationCodeService).generateAndSaveVerificationCode(user.getUserId());
//    }
//
//    @Test
//    void testAddUser_UserAlreadyExists() {
//        AddUserRequest request = new AddUserRequest();
//        request.setEmail(email);
//        request.setPassword(password);
//
//        when(authRepository.existsByEmail(email)).thenReturn(true);
//
//        assertThrows(AlreadyExistException.class, () -> authService.addUser(request));
//
//        verify(authRepository, never()).save(any(UserCredential.class));
//    }
//
//    @Test
//    void testLogin_Success() {
//        LoginRequest request = new LoginRequest();
//        request.setEmail(email);
//        request.setPassword(password);
//
//        Authentication authentication = mock(Authentication.class);
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
//        when(authRepository.findByEmail(email)).thenReturn(user);
//        when(jwtUtils.generateTokenForUser(authentication)).thenReturn("mocked-jwt-token");
//
//        var response = authService.login(request);
//
//        assertNotNull(response);
//        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
//        verify(authRepository).findByEmail(email);
//    }
//
//    @Test
//    void testLogin_UserNotFound() {
//        LoginRequest request = new LoginRequest();
//        request.setEmail("wrong@example.com");
//        request.setPassword(password);
//
//        when(authRepository.findByEmail(request.getEmail())).thenReturn(null);
//
//        assertThrows(UserNotFoundException.class, () -> authService.login(request));
//    }
//
//    @Test
//    void testResendVerificationCode_Success() {
//        UUID testUserId = UUID.randomUUID();
//        VerificationCode verificationCode = new VerificationCode();
//        verificationCode.setCode("654321");
//
//        doReturn(Optional.of(user)).when(authRepository).findById(any(UUID.class));
//        when(verificationCodeService.resendVerificationCode(any(UUID.class))).thenReturn(verificationCode);
//
//        assertDoesNotThrow(() -> authService.resendVerificationCode());
//
//        verify(verificationCodeService).resendVerificationCode(any(UUID.class));
//        verify(userProducer).sendConfirmation(any());
//    }
//
//    @Test
//    void testResendVerificationCode_UserAlreadyVerified() {
//        user.setVerified(true);
//
//        when(authRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        assertThrows(RuntimeException.class, () -> authService.resendVerificationCode());
//    }
//
//    @Test
//    void testResendVerificationCode_UserNotFound() {
//        when(authRepository.findById(userId)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () -> authService.resendVerificationCode());
//    }
//}