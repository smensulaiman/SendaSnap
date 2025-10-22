package com.sendajapan.sendasnap.models;

public class User {
    private String username;
    private String email;
    private boolean isLoggedIn;
    private long loginTime;

    // Constructor
    public User() {
        this.isLoggedIn = false;
        this.loginTime = 0;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.isLoggedIn = true;
        this.loginTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
        if (loggedIn) {
            this.loginTime = System.currentTimeMillis();
        } else {
            this.loginTime = 0;
        }
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }
}
