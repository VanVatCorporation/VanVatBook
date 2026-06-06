package com.vanvatcorporation.vanvatsach.dynamiclibs.auth;

public class LoginRequest {
    private String accountUsername;
    private String accountPassword;

    public LoginRequest(String accountUsername, String accountPassword) {
        this.accountUsername = accountUsername;
        this.accountPassword = accountPassword;
    }

    // Getters and Setters
    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public String getAccountPassword() { return accountPassword; }
    public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }
}
