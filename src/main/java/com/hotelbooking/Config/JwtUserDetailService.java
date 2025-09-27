package com.hotelbooking.Config;

import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.model.User; // your entity
import com.hotelbooking.Config.UserPrincipal; // your custom UserDetails implementation
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class JwtUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public JwtUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return new UserPrincipal(user);
    }



}