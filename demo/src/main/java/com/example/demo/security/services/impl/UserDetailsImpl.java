package com.example.demo.security.services.impl;

import com.example.demo.models.dao.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private String id;
    private String email;
    private String password;
    private String userProfileId;
    private String firstName;
    private String lastName;
    private String description;
    private String photoUri;

    public UserDetailsImpl(String id, String email, String password, String description, String firstName, String lastName, String photoUri) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.description = description;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUri = photoUri;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getDescription(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhotoUri()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
        return true;
    }
}
