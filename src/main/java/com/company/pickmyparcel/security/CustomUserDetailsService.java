package com.company.pickmyparcel.security;

import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String contactNumber) //will have in future email/id
            throws UsernameNotFoundException {
        // Let people login with either username or email FUTURE
        Optional<User> user = userRepository.findById(contactNumber);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found with contact number : " + contactNumber);
        }
        else {
            return UserPrincipal.create(user.get());
        }
    }
}
