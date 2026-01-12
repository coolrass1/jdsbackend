package com.skk.jdsbackend.security;

import com.skk.jdsbackend.entity.Permission;
import com.skk.jdsbackend.entity.Role;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RolePermissionMapping {
    
    private static final Map<Role, Set<Permission>> rolePermissions = new HashMap<>();
    
    static {
        // ADMIN has all permissions
        rolePermissions.put(Role.ADMIN, Set.of(
            Permission.CASE_READ, Permission.CASE_WRITE, Permission.CASE_DELETE,
            Permission.CLIENT_READ, Permission.CLIENT_WRITE, Permission.CLIENT_DELETE,
            Permission.DOCUMENT_READ, Permission.DOCUMENT_WRITE, Permission.DOCUMENT_DELETE, Permission.DOCUMENT_SIGN,
            Permission.USER_READ, Permission.USER_WRITE, Permission.USER_DELETE,
            Permission.TASK_READ, Permission.TASK_WRITE, Permission.TASK_DELETE, Permission.TASK_ASSIGN,
            Permission.NOTE_READ, Permission.NOTE_WRITE, Permission.NOTE_DELETE,
            Permission.ANALYTICS_VIEW,
            Permission.SYSTEM_ADMIN
        ));
        
        // CASE_WORKER has limited permissions
        rolePermissions.put(Role.CASE_WORKER, Set.of(
            Permission.CASE_READ, Permission.CASE_WRITE,
            Permission.CLIENT_READ, Permission.CLIENT_WRITE,
            Permission.DOCUMENT_READ, Permission.DOCUMENT_WRITE,
            Permission.TASK_READ, Permission.TASK_WRITE,
            Permission.NOTE_READ, Permission.NOTE_WRITE
        ));
        
        // SUPERVISOR has more permissions than case worker
        rolePermissions.put(Role.SUPERVISOR, Set.of(
            Permission.CASE_READ, Permission.CASE_WRITE, Permission.CASE_DELETE,
            Permission.CLIENT_READ, Permission.CLIENT_WRITE,
            Permission.DOCUMENT_READ, Permission.DOCUMENT_WRITE, Permission.DOCUMENT_SIGN,
            Permission.USER_READ,
            Permission.TASK_READ, Permission.TASK_WRITE, Permission.TASK_DELETE, Permission.TASK_ASSIGN,
            Permission.NOTE_READ, Permission.NOTE_WRITE, Permission.NOTE_DELETE,
            Permission.ANALYTICS_VIEW
        ));
    }
    
    public Set<Permission> getPermissionsForRole(Role role) {
        return rolePermissions.getOrDefault(role, Set.of());
    }
    
    public boolean hasPermission(Set<Role> roles, Permission permission) {
        return roles.stream()
                .flatMap(role -> getPermissionsForRole(role).stream())
                .anyMatch(p -> p == permission);
    }
}
