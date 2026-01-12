package com.skk.jdsbackend.security;

import com.skk.jdsbackend.entity.Role;
import com.skk.jdsbackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void testBuildWithAdminRole() {
        // Create a user with ADMIN role
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.ADMIN));

        // Build UserDetails
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Verify basic info
        assertEquals("admin", userDetails.getUsername());
        assertEquals("admin@example.com", userDetails.getEmail());
        assertEquals(1L, userDetails.getId());

        // Verify authorities include both role and permissions
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Should have ROLE_ADMIN
        assertTrue(authorities.contains("ROLE_ADMIN"));

        // Should have admin permissions
        assertTrue(authorities.contains("SYSTEM_ADMIN"));
        assertTrue(authorities.contains("CASE_DELETE"));
        assertTrue(authorities.contains("USER_DELETE"));
        assertTrue(authorities.contains("DOCUMENT_SIGN"));

        System.out.println("Admin authorities: " + authorities);
    }

    @Test
    void testBuildWithCaseWorkerRole() {
        User user = new User();
        user.setId(2L);
        user.setUsername("caseworker");
        user.setEmail("caseworker@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.CASE_WORKER));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Should have ROLE_CASE_WORKER
        assertTrue(authorities.contains("ROLE_CASE_WORKER"));

        // Should have basic permissions
        assertTrue(authorities.contains("CASE_READ"));
        assertTrue(authorities.contains("CASE_WRITE"));
        assertTrue(authorities.contains("DOCUMENT_READ"));

        // Should NOT have delete or admin permissions
        assertFalse(authorities.contains("CASE_DELETE"));
        assertFalse(authorities.contains("SYSTEM_ADMIN"));
        assertFalse(authorities.contains("USER_DELETE"));
        assertFalse(authorities.contains("DOCUMENT_SIGN"));

        System.out.println("Case Worker authorities: " + authorities);
    }

    @Test
    void testBuildWithMultipleRoles() {
        User user = new User();
        user.setId(3L);
        user.setUsername("supervisor");
        user.setEmail("supervisor@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.CASE_WORKER, Role.SUPERVISOR));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Should have both role authorities
        assertTrue(authorities.contains("ROLE_CASE_WORKER"));
        assertTrue(authorities.contains("ROLE_SUPERVISOR"));

        // Should have permissions from both roles (union)
        assertTrue(authorities.contains("CASE_READ"));
        assertTrue(authorities.contains("CASE_WRITE"));
        assertTrue(authorities.contains("CASE_DELETE")); // From SUPERVISOR
        assertTrue(authorities.contains("DOCUMENT_SIGN")); // From SUPERVISOR
        assertTrue(authorities.contains("ANALYTICS_VIEW")); // From SUPERVISOR

        // Should still not have system admin
        assertFalse(authorities.contains("SYSTEM_ADMIN"));
        assertFalse(authorities.contains("USER_DELETE"));

        System.out.println("Multi-role authorities: " + authorities);
    }

    @Test
    void testUserAccountStatus() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.CASE_WORKER));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // All account status should be true
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }
}
