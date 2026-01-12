package com.skk.jdsbackend.security;

import com.skk.jdsbackend.entity.Permission;
import com.skk.jdsbackend.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionMappingTest {

    private RolePermissionMapping rolePermissionMapping;

    @BeforeEach
    void setUp() {
        rolePermissionMapping = new RolePermissionMapping();
    }

    @Test
    void testAdminHasAllPermissions() {
        Set<Permission> adminPermissions = rolePermissionMapping.getPermissionsForRole(Role.ADMIN);
        
        // Admin should have all permissions
        assertTrue(adminPermissions.contains(Permission.CASE_READ));
        assertTrue(adminPermissions.contains(Permission.CASE_WRITE));
        assertTrue(adminPermissions.contains(Permission.CASE_DELETE));
        assertTrue(adminPermissions.contains(Permission.SYSTEM_ADMIN));
        assertTrue(adminPermissions.contains(Permission.USER_DELETE));
        
        // Should have more than 20 permissions
        assertTrue(adminPermissions.size() >= 20);
    }

    @Test
    void testCaseWorkerHasLimitedPermissions() {
        Set<Permission> caseWorkerPermissions = rolePermissionMapping.getPermissionsForRole(Role.CASE_WORKER);
        
        // Should have basic permissions
        assertTrue(caseWorkerPermissions.contains(Permission.CASE_READ));
        assertTrue(caseWorkerPermissions.contains(Permission.CASE_WRITE));
        
        // Should NOT have delete or admin permissions
        assertFalse(caseWorkerPermissions.contains(Permission.CASE_DELETE));
        assertFalse(caseWorkerPermissions.contains(Permission.SYSTEM_ADMIN));
        assertFalse(caseWorkerPermissions.contains(Permission.USER_DELETE));
    }

    @Test
    void testSupervisorHasMorePermissionsThanCaseWorker() {
        Set<Permission> supervisorPermissions = rolePermissionMapping.getPermissionsForRole(Role.SUPERVISOR);
        Set<Permission> caseWorkerPermissions = rolePermissionMapping.getPermissionsForRole(Role.CASE_WORKER);
        
        // Supervisor should have more permissions
        assertTrue(supervisorPermissions.size() > caseWorkerPermissions.size());
        
        // Supervisor should have delete permissions
        assertTrue(supervisorPermissions.contains(Permission.CASE_DELETE));
        assertTrue(supervisorPermissions.contains(Permission.TASK_DELETE));
        
        // But not system admin
        assertFalse(supervisorPermissions.contains(Permission.SYSTEM_ADMIN));
    }

    @Test
    void testHasPermissionWithSingleRole() {
        Set<Role> adminRole = Set.of(Role.ADMIN);
        Set<Role> caseWorkerRole = Set.of(Role.CASE_WORKER);
        
        // Admin should have CASE_DELETE permission
        assertTrue(rolePermissionMapping.hasPermission(adminRole, Permission.CASE_DELETE));
        
        // Case worker should NOT have CASE_DELETE permission
        assertFalse(rolePermissionMapping.hasPermission(caseWorkerRole, Permission.CASE_DELETE));
    }

    @Test
    void testHasPermissionWithMultipleRoles() {
        // User with both CASE_WORKER and SUPERVISOR roles
        Set<Role> multipleRoles = Set.of(Role.CASE_WORKER, Role.SUPERVISOR);
        
        // Should have permissions from both roles
        assertTrue(rolePermissionMapping.hasPermission(multipleRoles, Permission.CASE_READ));
        assertTrue(rolePermissionMapping.hasPermission(multipleRoles, Permission.CASE_DELETE));
        assertTrue(rolePermissionMapping.hasPermission(multipleRoles, Permission.ANALYTICS_VIEW));
        
        // Still should not have admin permissions
        assertFalse(rolePermissionMapping.hasPermission(multipleRoles, Permission.SYSTEM_ADMIN));
    }

    @Test
    void testCaseWorkerCannotDeleteUsers() {
        Set<Role> caseWorkerRole = Set.of(Role.CASE_WORKER);
        assertFalse(rolePermissionMapping.hasPermission(caseWorkerRole, Permission.USER_DELETE));
    }

    @Test
    void testSupervisorCanViewAnalytics() {
        Set<Role> supervisorRole = Set.of(Role.SUPERVISOR);
        assertTrue(rolePermissionMapping.hasPermission(supervisorRole, Permission.ANALYTICS_VIEW));
    }

    @Test
    void testSupervisorCanSignDocuments() {
        Set<Role> supervisorRole = Set.of(Role.SUPERVISOR);
        assertTrue(rolePermissionMapping.hasPermission(supervisorRole, Permission.DOCUMENT_SIGN));
    }

    @Test
    void testCaseWorkerCannotSignDocuments() {
        Set<Role> caseWorkerRole = Set.of(Role.CASE_WORKER);
        assertFalse(rolePermissionMapping.hasPermission(caseWorkerRole, Permission.DOCUMENT_SIGN));
    }
}
