package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity implements UserDetails, OAuth2User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Transient
    private Map<String, Object> attributes;

    // Default constructor required by JPA
    protected Account() {
        this.attributes = new HashMap<>();
    }

    // Constructor for creating a new account
    public Account(String provider, String email, String name, String nickname, String profileImage, Role role) {
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
        this.attributes = new HashMap<>();
    }

    public enum Role {
        USER, ADMIN
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // OAuth2 users don't have passwords
        return null;
    }

    @Override
    public String getUsername() {
        // Using email as the username
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

    // OAuth2User implementation
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    // Update profile information
    public void updateProfile(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
    }

    // Update nickname
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
