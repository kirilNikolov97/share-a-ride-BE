package com.knikolov.sharearide.dto;

/**
 * DTO used when user wants to change password
 */
public class PasswordChange {

    private String oldPassword;
    private String newPassword;

    public PasswordChange() {
    }

    public PasswordChange(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
