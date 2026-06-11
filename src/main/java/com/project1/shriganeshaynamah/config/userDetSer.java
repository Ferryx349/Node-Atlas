package com.project1.shriganeshaynamah.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.user.User;

@Service
public class userDetSer implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(userDetSer.class);

    @Autowired
    private userDao userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username);
        if (user == null) {
            log.warn("Login attempt for unknown user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new Customuser(user);
    }
}
