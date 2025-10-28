package com.group4.evRentalBE.domain.entity;


import lombok.*;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(name = "is_verify", nullable = false)
    @Builder.Default
    private boolean isVerify = false;

    private int tokenVersion;

    // ✅ ASSOCIATION: Many-to-Many với Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ✅ ASSOCIATION: User có thể quản lý station (cho STAFF role)
    @ManyToOne
    @JoinColumn(name = "managed_station_id")
    private RentalStation managedStation;

    // ✅ COMPOSITION: User owns Documents
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public Set<String> getAllPermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    // ✅ DOCUMENT BUSINESS METHODS
    public Document getDefaultDocument() {
        return documents.stream()
                .filter(Document::isDefault)
                .findFirst()
                .orElse(null);
    }

    public List<Document> getValidDocuments() {
        return documents.stream()
                .filter(Document::isValid)
                .collect(Collectors.toList());
    }

    public void addDocument(Document document) {
        if (!documents.contains(document)) {
            documents.add(document);
            document.setUser(this);
        }
    }

    public void setDefaultDocument(Document document) {
        // Unset current default
        documents.forEach(Document::unsetDefault);
        // Set new default
        document.setAsDefault();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Thêm roles dưới dạng "ROLE_ADMIN"
        authorities.addAll(roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet()));

        // Thêm trực tiếp permissions
        authorities.addAll(
                getAllPermissions().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));

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
        return isVerify;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
