package com.rp.authorization.controllers;

import com.rp.authorization.exceptions.ResourceNotFoundException;
import com.rp.authorization.models.entities.Role;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static com.rp.authorization.utils.Constants.*;

import com.rp.authorization.models.dtos.LoginUserDto;
import com.rp.authorization.models.dtos.CreateUserDto;
import com.rp.authorization.models.dtos.ValidateTokenDto;
import com.rp.authorization.models.response.*;
import com.rp.authorization.models.entities.UserAccount;
import com.rp.authorization.models.entities.User;
import com.rp.authorization.models.entities.RefreshToken;
import com.rp.authorization.repositories.RefreshTokenRepository;
import com.rp.authorization.services.interfaces.RoleService;
import com.rp.authorization.services.interfaces.UserService;
import com.rp.authorization.services.interfaces.UserAccountService;
import com.rp.authorization.utils.JwtTokenUtil;
import com.rp.authorization.utils.Helpers;
import com.rp.authorization.events.OnRegistrationCompleteEvent;


@Api(tags = SWG_AUTH_TAG_NAME, description = SWG_AUTH_TAG_DESCRIPTION)
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserService userService;

    private final RoleService roleService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final RefreshTokenRepository refreshTokenRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final UserAccountService userAccountService;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtTokenUtil jwtTokenUtil,
        UserService userService,
        RoleService roleService,
        RefreshTokenRepository refreshTokenRepository,
        ApplicationEventPublisher eventPublisher,
        UserAccountService userAccountService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.roleService = roleService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.eventPublisher = eventPublisher;
        this.userAccountService = userAccountService;
    }

    @ApiOperation(value = SWG_AUTH_REGISTER_OPERATION, response = BadRequestResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = SWG_AUTH_REGISTER_MESSAGE, response = UserResponse.class),
        @ApiResponse(code = 400, message = SWG_AUTH_REGISTER_ERROR, response = BadRequestResponse.class),
        @ApiResponse(code = 422, message = INVALID_DATA_MESSAGE, response = InvalidDataResponse.class),
    })
    @PostMapping(value = "/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserDto createUserDto) {
        try {
            Role roleUser = roleService.findByName(ROLE_USER);

            createUserDto.setRole(roleUser);

            User user = userService.save(createUserDto);

            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));

            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
                Map<String, String> result = new HashMap<>();
                result.put("message", SWG_AUTH_REGISTER_ERROR);

                logger.error("Register User: " + ROLE_NOT_FOUND_MESSAGE);

                return ResponseEntity.badRequest().body(result);
        }
    }

    @ApiOperation(value = SWG_AUTH_LOGIN_OPERATION, response = BadRequestResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = SWG_AUTH_LOGIN_MESSAGE, response = AuthTokenResponse.class),
        @ApiResponse(code = 400, message = SWG_AUTH_LOGIN_ERROR, response = BadRequestResponse.class),
        @ApiResponse(code = 422, message = INVALID_DATA_MESSAGE, response = InvalidDataResponse.class),
    })
    @PostMapping(value = "/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginUserDto loginUserDto)
        throws ResourceNotFoundException {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginUserDto.getEmail(),
                    loginUserDto.getPassword()
                )
        );

        User user = userService.findByEmail(loginUserDto.getEmail());
        Map<String, String> result = new HashMap<>();

        if (!user.isEnabled()) {
            result.put(DATA_KEY, ACCOUNT_DEACTIVATED_MESSAGE);

            return ResponseEntity.badRequest().body(result);
        }

        if (!user.isConfirmed()) {
            result.put(DATA_KEY, ACCOUNT_NOT_CONFIRMED_MESSAGE);

            return ResponseEntity.badRequest().body(result);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = jwtTokenUtil.createTokenFromAuth(authentication);
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        String refreshToken = Helpers.generateRandomString(25);

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

        return ResponseEntity.ok(new AuthTokenResponse(token, refreshToken, expirationDate.getTime()));
    }

    @ApiOperation(value = SWG_AUTH_CONFIRM_ACCOUNT_OPERATION, response = SuccessResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = SWG_AUTH_CONFIRM_ACCOUNT_MESSAGE, response = SuccessResponse.class),
        @ApiResponse(code = 400, message = SWG_AUTH_CONFIRM_ACCOUNT_ERROR, response = BadRequestResponse.class),
    })
    @PostMapping(value = "/confirm-account")
    public ResponseEntity<Object> confirmAccount(@Valid @RequestBody ValidateTokenDto validateTokenDto)
        throws ResourceNotFoundException {
        UserAccount userAccount = userAccountService.findByToken(validateTokenDto.getToken());
        Map<String, String> result = new HashMap<>();

        if (userAccount.isExpired()) {
            result.put(MESSAGE_KEY, TOKEN_EXPIRED_MESSAGE);

            userAccountService.delete(userAccount.getId());

            return ResponseEntity.badRequest().body(result);
        }

        userService.confirm(userAccount.getUser().getId());

        result.put(MESSAGE_KEY, ACCOUNT_CONFIRMED_MESSAGE);

        return ResponseEntity.badRequest().body(result);
    }
}
