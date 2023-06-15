package com.flip.entity.dto;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flip.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 已认证（登录）的用户相关信息
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoggedUser implements UserDetails {

    private User user;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (ObjectUtil.isNotNull(user.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }
        if (ObjectUtil.isNotNull(user.getAuthority())) {
            user.getAuthority().getAuthorities().forEach(authority -> {
                authorities.add(new SimpleGrantedAuthority(authority));
            });
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
