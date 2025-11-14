package com.sendajapan.sendasnap.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserData implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("email_verified_at")
    private String emailVerifiedAt;

    @SerializedName("role")
    private String role;

    @SerializedName("phone")
    private String phone;

    @SerializedName("avis_id")
    private String avisId;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    private boolean loggedIn = true;

    public UserData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvisId() {
        return avisId;
    }

    public void setAvisId(String avisId) {
        this.avisId = avisId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserData{\n" +
                "  id=" + id + ",\n" +
                "  name='" + name + "',\n" +
                "  email='" + email + "',\n" +
                "  emailVerifiedAt='" + emailVerifiedAt + "',\n" +
                "  role='" + role + "',\n" +
                "  phone='" + phone + "',\n" +
                "  avisId='" + avisId + "',\n" +
                "  avatar='" + avatar + "',\n" +
                "  avatarUrl='" + avatarUrl + "',\n" +
                "  createdAt='" + createdAt + "',\n" +
                "  updatedAt='" + updatedAt + "',\n" +
                "  loggedIn=" + loggedIn + "\n" +
                "}";
    }
}

