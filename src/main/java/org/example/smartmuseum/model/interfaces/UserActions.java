package org.example.smartmuseum.model.interfaces;

public interface UserActions {
    boolean login(String username, String password);
    void logout();
    boolean changePassword(String oldPassword, String newPassword);
    void updateProfile(Object profile);
}