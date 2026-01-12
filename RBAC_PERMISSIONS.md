# Permission-Based RBAC Implementation

## Overview
Your application now supports granular permission-based access control in addition to role-based access.

## Components Created

1. **Permission.java** - Enum defining all available permissions
2. **RolePermissionMapping.java** - Maps roles to their permissions
3. **CustomPermissionEvaluator.java** - Evaluates permissions for SpEL expressions
4. **MethodSecurityConfig.java** - Configures method security with custom evaluator

## Usage Examples

### Using hasAuthority (Recommended)
```java
@PreAuthorize("hasAuthority('CASE_WRITE')")
public ResponseEntity<?> createCase() { }

@PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
public ResponseEntity<?> deleteDocument() { }
```

### Using hasPermission (Custom Evaluator)
```java
@PreAuthorize("hasPermission(null, 'CASE_WRITE')")
public ResponseEntity<?> createCase() { }
```

### Combining Multiple Permissions
```java
@PreAuthorize("hasAuthority('CASE_READ') and hasAuthority('CLIENT_READ')")
public ResponseEntity<?> getCaseWithClient() { }

@PreAuthorize("hasAnyAuthority('CASE_DELETE', 'SYSTEM_ADMIN')")
public ResponseEntity<?> deleteCase() { }
```

### Still Using Roles (Backward Compatible)
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminOnly() { }
```

## Permission Matrix

| Role | Permissions |
|------|-------------|
| **ADMIN** | All permissions including SYSTEM_ADMIN |
| **SUPERVISOR** | Most permissions except user management and system admin |
| **CASE_WORKER** | Basic read/write for cases, clients, documents, tasks, notes |

## Updating Permissions

To change role permissions, edit [RolePermissionMapping.java](src/main/java/com/skk/jdsbackend/security/RolePermissionMapping.java):

```java
rolePermissions.put(Role.CASE_WORKER, Set.of(
    Permission.CASE_READ,
    Permission.CASE_WRITE
    // Add more permissions
));
```

## Migration Guide

### Before (Role-based)
```java
@PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
```

### After (Permission-based)
```java
@PreAuthorize("hasAuthority('CASE_WRITE')")
```

## Benefits

✅ **Fine-grained control** - Check specific permissions, not just roles  
✅ **Easier to maintain** - Change permissions without modifying controllers  
✅ **Better separation** - Business logic separated from authorization  
✅ **Flexible** - Users can have multiple roles with combined permissions  
✅ **Backward compatible** - Can still use hasRole() checks
