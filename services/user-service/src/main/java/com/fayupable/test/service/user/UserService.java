package com.fayupable.test.service.user;

import com.fayupable.test.dto.user.UserInfoDto;
import com.fayupable.test.dto.user.VerifyDto;
import com.fayupable.test.entity.user.UserContact;
import com.fayupable.test.entity.user.UserInfo;
import com.fayupable.test.entity.user.UserProfile;
import com.fayupable.test.entity.verification.VerificationCode;
import com.fayupable.test.enums.Role;
import com.fayupable.test.enums.UserStatus;
import com.fayupable.test.exception.UserAlreadyVerifiedException;
import com.fayupable.test.kafka.UserConfirmation;
import com.fayupable.test.kafka.UserProducer;
import com.fayupable.test.mapper.user.UserInfoMapper;
import com.fayupable.test.repository.IUserRepository;
import com.fayupable.test.request.login.LoginRequest;
import com.fayupable.test.request.user.*;
import com.fayupable.test.request.verification.AddVerificationCodeRequest;
import com.fayupable.test.response.JwtResponse;
import com.fayupable.test.response.LoginResponse;
import com.fayupable.test.security.jwt.JwtUtils;
import com.fayupable.test.security.user.AuthDetails;
import com.fayupable.test.service.verification.IVerificationCodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService, IAuthService {
    private final IUserRepository userRepository;
    private final UserInfoMapper userInfoMapper;
    private final IVerificationCodeService verificationCodeService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserProducer userProducer;


    @Override
    public String getEmailByUserId(UUID userId) {
        return userRepository.findEmailByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    @Override
    public String getRoleByUserId(UUID userId) {
        return userRepository.findRoleByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public UserInfoDto addUser(AddUserInfoRequest request) {
        return Optional.of(request)
                .map(this::createUserHelper)
                .map(userRepository::save)
                .map(user -> {
                    VerificationCode verificationCode = verificationCodeService.generateAndSaveVerificationCode(user.getUserId());
                    sendUserConfirmationMessage(user, verificationCode);
                    return user;
                })
                .map(userInfoMapper::fromUserInfo)
                .orElseThrow(() -> new RuntimeException("User not created!"));
    }


    private UserInfo createUserHelper(AddUserInfoRequest request) {
        UserInfo user = new UserInfo();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        user.setProfiles(new HashSet<>(createUserProfilesHelper(request.getProfiles(), user)));
        user.setContacts(new HashSet<>(createUserContactsHelper(request.getContacts(), user)));
        return user;
    }

    private List<UserContact> createUserContactsHelper(List<AddUserContactRequest> contacts, UserInfo user) {
        return contacts.stream()
                .map(contact -> {
                    UserContact userContact = new UserContact();
                    userContact.setPhoneNumber(contact.getPhoneNumber());
                    userContact.setUser(user);
                    return userContact;
                }).collect(Collectors.toList());
    }

    private List<UserProfile> createUserProfilesHelper(List<AddUserProfileRequest> profiles, UserInfo user) {
        return profiles.stream()
                .map(profile -> {
                    UserProfile userProfile = new UserProfile();
                    userProfile.setBio(profile.getBio());
                    userProfile.setAvatarUrl(profile.getAvatarUrl());
                    userProfile.setUser(user);
                    return userProfile;
                }).collect(Collectors.toList());
    }

    private boolean isUserExists(UUID userId) {
        return userRepository.existsById(userId);
    }


    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticateUser(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = generateJwt(authentication);
        UserInfo user = getUserByEmail(request.getEmail());
        return new LoginResponse("Login success", new JwtResponse(user.getUserId(), jwt));

    }

    private Authentication authenticateUser(LoginRequest request) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    }

    private String generateJwt(Authentication authentication) {
        return jwtUtils.generateTokenForUser(authentication);
    }

    private UserInfo getUserByEmail(String email) {
        Optional<UserInfo> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user.get();

    }

    @Override
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = jwtUtils.validateToken(token);
            if (isValid) {
                response.put("valid", true);
                response.put("username", jwtUtils.getUserNameFromToken(token));
                response.put("userId", jwtUtils.getUserIdFromToken(token));
            } else {
                response.put("valid", false);
                response.put("message", "Invalid token");
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Error validating token: " + e.getMessage());
        }
        return response;
    }


    @Override
    @Transactional
    public UserInfoDto verifyUser(VerifyDto code) {
        UserInfo user = findAndValidateUser(code.getEmail());
        user.setStatus(UserStatus.VERIFIED);
        checkUserVerificationStatus(user);
        verifyUserWithCode(user, code.getVerificationCode());
        return userInfoMapper.fromUserInfo(user);
    }

    private UserInfo findAndValidateUser(String email) {
        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    private void checkUserVerificationStatus(UserInfo user) {
        if (user.isVerified()) {
            throw new UserAlreadyVerifiedException("User already verified");
        }
    }

    private void verifyUserWithCode(UserInfo user, String verificationCode) {
        AddVerificationCodeRequest verificationCodeRequest = new AddVerificationCodeRequest(
                user.getUserId(),
                verificationCode
        );
        verificationCodeService.validateVerificationCode(verificationCodeRequest);
        updateUserVerificationStatus(user);
    }

    private void updateUserVerificationStatus(UserInfo user) {
        user.setVerified(true);
        userRepository.save(user);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public String resendVerificationCode() {
        UUID userId = getAuthenticatedUserId();
        UserInfo user = getUserById(userId);
        if (!user.getUserId().equals(userId)) {
            throw new SecurityException("Invalid user");
        }
        VerificationCode verificationCode = verificationCodeService.resendVerificationCode(userId);
        sendUserConfirmationMessage(user, verificationCode);
        return verificationCode.getCode();
    }

    public static UUID getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AuthDetails) {
            return ((AuthDetails) principal).getId();
        } else {
            throw new SecurityException("Invalid authentication context");
        }
    }

    private UserInfo getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    private void sendUserConfirmationMessage(UserInfo user, VerificationCode verificationCode) {
        UserConfirmation userConfirmation = new UserConfirmation();
        userConfirmation.setUserId(user.getUserId());
        userConfirmation.setEmail(user.getEmail());
        userConfirmation.setVerificationCode(verificationCode.getCode());
        userConfirmation.setVerificationCodeExpiration(verificationCode.getExpirationTime());
        userProducer.sendConfirmation(userConfirmation);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public UserInfoDto updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest, UUID userId) {
        UserInfo user = getUserById(userId);
        checkUserOwnership(userId, getAuthenticatedUserId());
        return Optional.of(updateUserInfoRequest)
                .map(request -> updateUserInfoHelper(request, user))
                .map(userRepository::save)
                .map(userInfoMapper::fromUserInfo)
                .orElseThrow(() -> new RuntimeException("User not updated!"));

    }

    private UserInfo updateUserInfoHelper(UpdateUserInfoRequest request, UserInfo user) {
        return updateBasicInfo(request, handleEmailUpdate(request, user));
    }

    private UserInfo updateBasicInfo(UpdateUserInfoRequest request, UserInfo user) {
        updatePersonalDetails(request, user);
        updateSecurityDetails(request, user);
        updateCollections(request, user);
        return user;
    }

    private void updatePersonalDetails(UpdateUserInfoRequest request, UserInfo user) {
        Optional.ofNullable(request.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(request.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(request.getUsername()).ifPresent(user::setUsername);
    }

    private void updateSecurityDetails(UpdateUserInfoRequest request, UserInfo user) {
        Optional.ofNullable(request.getPassword())
                .map(passwordEncoder::encode)
                .ifPresent(user::setPassword);
    }

    private void updateCollections(UpdateUserInfoRequest request, UserInfo user) {
        updateProfiles(request, user);
        updateContacts(request, user);
    }

    private void updateProfiles(UpdateUserInfoRequest request, UserInfo user) {
        Optional.ofNullable(request.getProfiles())
                .ifPresent(profiles -> {
                    user.getProfiles().clear();
                    user.getProfiles().addAll(updateUserProfilesHelper(profiles, user));
                });
    }

    private void updateContacts(UpdateUserInfoRequest request, UserInfo user) {
        Optional.ofNullable(request.getContacts())
                .ifPresent(contacts -> {
                    user.getContacts().clear();
                    user.getContacts().addAll(updateUserContactsHelper(contacts, user));
                });
    }

    private List<UserProfile> updateUserProfilesHelper(List<UpdateUserProfileRequest> profiles, UserInfo user) {
        return profiles.stream()
                .map(profile -> {
                    UserProfile userProfile = new UserProfile();
                    userProfile.setProfileId(profile.getProfileId());
                    userProfile.setBio(profile.getBio());
                    userProfile.setAvatarUrl(profile.getAvatarUrl());
                    userProfile.setUser(user);
                    return userProfile;
                }).collect(Collectors.toList());
    }

    private List<UserContact> updateUserContactsHelper(List<UpdateUserContactRequest> contacts, UserInfo user) {
        return contacts.stream()
                .map(contact -> {
                    UserContact userContact = new UserContact();
                    userContact.setUserContactId(contact.getContactId());
                    userContact.setPhoneNumber(contact.getPhoneNumber());
                    userContact.setUser(user);
                    return userContact;
                }).collect(Collectors.toList());
    }

    private void checkUserOwnership(UUID userId, UUID authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new SecurityException("Invalid user");
        }
    }

    private UserInfo handleEmailUpdate(UpdateUserInfoRequest request, UserInfo user) {
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            user.setEmail(request.getEmail());
            user.setVerified(false);
            user.setStatus(UserStatus.PENDING_APPROVAL);

            UserInfo savedUser = userRepository.save(user);

            VerificationCode verificationCode = verificationCodeService.generateAndSaveVerificationCode(savedUser.getUserId());

            sendUserConfirmationMessage(savedUser, verificationCode);

            return savedUser;
        }
        return user;
    }


}
