package com.example.APISkeleton.services.impls;

import com.example.APISkeleton.persistance.entities.User;
import com.example.APISkeleton.services.IAuthService;
import com.example.APISkeleton.services.IUserService;
import com.example.APISkeleton.types.JWTType;
import com.example.APISkeleton.utils.IJWTUtils;
import com.example.APISkeleton.web.dtos.requests.AuthenticateRequest;
import com.example.APISkeleton.web.dtos.requests.RefreshTokenRequest;
import com.example.APISkeleton.web.dtos.responses.BaseResponse;
import com.example.APISkeleton.web.exceptions.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements IAuthService {
    private final IUserService userService;
    private final IJWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public AuthServiceImpl(IUserService userService, IJWTUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public BaseResponse authenticate(AuthenticateRequest request) {
        Optional<User> optionalUser = userService.getOptionalUserByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        Map<String, Object> claims = Map.of(
                "id", user.getId()
        );

        String accessToken = jwtUtils.generateToken(user.getEmail(), claims, JWTType.ACCESS_TOKEN);
        String refreshToken = jwtUtils.generateToken(user.getEmail(), null, JWTType.REFRESH_TOKEN);

        Map<String, String> tokens = Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken
        );

        return BaseResponse.builder()
                .data(tokens)
                .message("Successfully authenticated")
                .success(true)
                .httpStatus(HttpStatus.CREATED)
                .build();

    }

    @Override
    public BaseResponse refreshToken(RefreshTokenRequest request) {
        Boolean isTokenValid = jwtUtils.isTokenValid(request.getRefreshToken(), JWTType.REFRESH_TOKEN);

        if (!isTokenValid) {
            throw new InvalidCredentialsException();
        }

        String email = jwtUtils.getSubjectFromToken(request.getRefreshToken());
        Map<String, Object> claims = jwtUtils.getClaimsFromToken(request.getRefreshToken());

        String accessToken = jwtUtils.generateToken(email, claims, JWTType.ACCESS_TOKEN);
        String refreshToken = jwtUtils.generateToken(email, null, JWTType.REFRESH_TOKEN);

        Map<String, String> tokens = Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken
        );

        return BaseResponse.builder()
                .data(tokens)
                .message("Successfully refreshed token")
                .success(true)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }
}
