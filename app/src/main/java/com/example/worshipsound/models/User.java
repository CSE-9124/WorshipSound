package com.example.worshipsound.models;

/**
 * Model class representing a user
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private long createdAt;
    private boolean isDarkTheme;

    // Constructors
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isDarkTheme = false;
    }

    public User(String username, String email, String password, 
                String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isDarkTheme() { return isDarkTheme; }
    public void setDarkTheme(boolean darkTheme) { isDarkTheme = darkTheme; }

    // Helper methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    public String getDisplayName() {
        String fullName = getFullName();
        return fullName != null && !fullName.trim().isEmpty() ? fullName : username;
    }

    // Validation methods
    public boolean isValidForRegistration() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && password.length() >= 6 &&
               isValidEmail(email);
    }

    public boolean isValidForLogin() {
        return (username != null && !username.trim().isEmpty() || 
                email != null && !email.trim().isEmpty()) &&
               password != null && !password.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isDarkTheme=" + isDarkTheme +
                '}';
    }
}
