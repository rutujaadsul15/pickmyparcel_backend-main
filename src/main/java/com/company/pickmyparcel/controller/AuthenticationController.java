package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.requests.LoginRequest;
import com.company.pickmyparcel.security.JwtAuthenticationResponse;
import com.company.pickmyparcel.service.UserServiceImpl;
import com.company.pickmyparcel.security.JwtTokenProvider;
import com.company.pickmyparcel.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user")
public class AuthenticationController {
    @Autowired
    private UserServiceImpl userServiceimpl;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@RequestBody @Valid User userRequest) {
        User user = userServiceimpl.registerUser(userRequest);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    private JwtAuthenticationResponse loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getContactNo(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userServiceimpl.getUserByContactNumber(loginRequest.getContactNo());
        return new JwtAuthenticationResponse(jwt, tokenProvider.getUserIdFromJWT(jwt), userPrincipal.getUserRole(), user.getFirstName(), user.getLastName());
    }
}
