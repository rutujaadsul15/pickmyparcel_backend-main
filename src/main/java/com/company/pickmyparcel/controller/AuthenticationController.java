package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.exception.EntityNotFoundException;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.requests.LoginRequest;
import com.company.pickmyparcel.model.requests.PasswordResetOTPRequest;
import com.company.pickmyparcel.model.requests.PasswordResetRequest;
import com.company.pickmyparcel.security.JwtAuthenticationResponse;
import com.company.pickmyparcel.security.JwtTokenProvider;
import com.company.pickmyparcel.security.UserPrincipal;
import com.company.pickmyparcel.service.DealServiceImpl;
import com.company.pickmyparcel.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user")
public class AuthenticationController {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationController.class.getName());
    @Autowired
    private UserServiceImpl userServiceimpl;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;


    @GetMapping("/sendRegisterOTP")
    public ResponseEntity<Boolean> sendRegisterOTP(@RequestParam @Valid String mobileNo) {
        boolean result = userServiceimpl.sendRegisterOtp(mobileNo);
        if (!result) {
            LOGGER.info("Unable to send register OTP to new user with mobile number : " + mobileNo);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sendPasswordResetOTP")
    public ResponseEntity<Boolean> sendPasswordResetOTP(@RequestParam String mobileNo) {
        boolean result = userServiceimpl.sendPasswordResetOTP(mobileNo);
        if (!result) {
            LOGGER.info("Unable to send Password Reset OTP to user with mobile number : " + mobileNo);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verifyPasswordResetOTP")
    public ResponseEntity<Boolean> verifyPasswordResetOTP(@RequestBody PasswordResetOTPRequest passwordResetOTPRequest) {
        boolean result = userServiceimpl.verifyPasswordResetOTP(passwordResetOTPRequest);
        if (!result) {
            LOGGER.info("Unable to verify Password Reset for mobile number : " + passwordResetOTPRequest.getMobileNo());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submitNewPassword")
    public ResponseEntity<Boolean> submitNewPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        boolean result = userServiceimpl.submitNewPassword(passwordResetRequest);
        if (!result) {
            LOGGER.info("Unable to updated password");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@RequestParam String registerOtp, @RequestBody @Valid User userRequest) {
        User user = userServiceimpl.registerUser(registerOtp, userRequest);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    private JwtAuthenticationResponse loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        User user = userServiceimpl.getUserByContactNumber(loginRequest.getContactNo());
        if (user != null) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getContactNo(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            DealServiceImpl.Pickmyparcel_LoggedIn_users("User has signed in : " + user.getFirstName() + " "
                    + user.getLastName() + " Contact No : " + user.getContactNo());
            return new JwtAuthenticationResponse(jwt, tokenProvider.getUserIdFromJWT(jwt), userPrincipal.getUserRole(), user.getFirstName(), user.getLastName());
        } else {
            throw new EntityNotFoundException("User", "Not Found in Database");
        }
    }
}
