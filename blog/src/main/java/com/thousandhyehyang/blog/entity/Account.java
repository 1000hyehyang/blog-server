package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

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

    // Default constructor required by JPA
    protected Account() {
    }

    // Constructor for creating a new account
    public Account(String provider, String email, String name, String nickname, String profileImage, Role role) {
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
    }

    public enum Role {
        USER, ADMIN
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
