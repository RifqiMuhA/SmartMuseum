package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.concrete.Boss;
import org.example.smartmuseum.model.concrete.Staff;
import org.example.smartmuseum.model.concrete.Visitor;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserService {
    private ConcurrentMap<Integer, BaseUser> activeUsers;

    public UserService() {
        this.activeUsers = new ConcurrentHashMap<>();
    }

    public BaseUser authenticate(String username, String password) {
        // In real implementation, this would check against database
        System.out.println("Authenticating user: " + username);

        // Mock authentication - in real app, verify against database
        if ("admin".equals(username)) {
            Boss boss = new Boss(1, username);
            activeUsers.put(1, boss);
            return boss;
        } else if ("staff1".equals(username)) {
            Staff staff = new Staff(2, username, "Gallery Assistant");
            activeUsers.put(2, staff);
            return staff;
        } else if ("visitor1".equals(username)) {
            Visitor visitor = new Visitor(3, username);
            activeUsers.put(3, visitor);
            return visitor;
        }

        return null;
    }

    public boolean register(User userData) {
        System.out.println("Registering new user: " + userData.getUsername());
        // In real implementation, would save to database
        return true;
    }

    public BaseUser getUser(int userId) {
        return activeUsers.get(userId);
    }

    public void removeActiveUser(int userId) {
        activeUsers.remove(userId);
    }

    public int getActiveUserCount() {
        return activeUsers.size();
    }

    public ConcurrentMap<Integer, BaseUser> getActiveUsers() {
        return activeUsers;
    }
}
