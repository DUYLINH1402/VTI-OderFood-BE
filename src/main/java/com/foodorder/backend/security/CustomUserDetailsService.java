// LOAD USER TỪ DB KHI LOGIN HOẶC XỬ LÝ TOKEN

package com.foodorder.backend.security;

import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("USER_NOT_FOUND"));
        return new CustomUserDetails(user);
    }
}
