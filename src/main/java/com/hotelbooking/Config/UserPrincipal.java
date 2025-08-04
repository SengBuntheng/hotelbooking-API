package com.hotelbooking.Config;

import com.hotelbooking.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Example: give all users "USER" role
        return Collections.singleton(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();  // or however your User stores password
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // or add your own logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive(); // example
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // or add your own logic
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
