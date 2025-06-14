package org.example.smartmuseum.model.enums;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    BOSS("boss", Arrays.asList("ALL_PERMISSIONS")),
    STAFF("staff", Arrays.asList("MANAGE_ARTWORKS", "ASSIST_VISITORS", "CHECK_ATTENDANCE")),
    VISITOR("visitor", Arrays.asList("BROWSE_ARTWORKS", "JOIN_AUCTIONS", "USE_PHOTOBOOTH"));

    private final String roleName;
    private final List<String> permissions;

    UserRole(String roleName, List<String> permissions) {
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains("ALL_PERMISSIONS") || permissions.contains(permission);
    }

    @Override
    public String toString() {
        return roleName;
    }
}