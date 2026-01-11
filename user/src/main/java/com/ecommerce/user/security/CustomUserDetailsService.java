package com.ecommerce.user.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return UserPrincipal.create(user);
    }

    public static class UserPrincipal implements UserDetails {
        private String id;
        private String email;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean enabled;

        public UserPrincipal(String id, String email, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
            this.enabled = enabled;
        }

        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }

            return new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    authorities,
                    user.getEnabled()
            );
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
