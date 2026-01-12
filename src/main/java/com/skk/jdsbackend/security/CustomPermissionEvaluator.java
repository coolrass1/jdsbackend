package com.skk.jdsbackend.security;

import com.skk.jdsbackend.entity.Permission;
import com.skk.jdsbackend.entity.Role;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final RolePermissionMapping rolePermissionMapping;

    public CustomPermissionEvaluator(RolePermissionMapping rolePermissionMapping) {
        this.rolePermissionMapping = rolePermissionMapping;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        return hasPrivilege(authentication, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        return hasPrivilege(authentication, permission.toString());
    }

    private boolean hasPrivilege(Authentication authentication, String permissionName) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Get user roles from authorities
        Set<Role> roles = authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        
        try {
            Permission permission = Permission.valueOf(permissionName);
            return rolePermissionMapping.hasPermission(roles, permission);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
