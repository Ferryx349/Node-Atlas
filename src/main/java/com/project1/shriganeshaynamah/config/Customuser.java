package com.project1.shriganeshaynamah.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project1.shriganeshaynamah.user.User;


public class Customuser implements UserDetails {
   private final  User us;

    public Customuser(User us) {
        this.us = us;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      SimpleGrantedAuthority ss=new SimpleGrantedAuthority("ROLE_" +us.getRole());
      return List.of(ss);
    }

   

    @Override
    public String getPassword() {
        return us.getPassword();
    }

    @Override
    public String getUsername() {
        return us.getName();
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
        return "true".equalsIgnoreCase(us.getEnable());
    }
}
