package com.sendajapan.sendasnap.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UsersListResponseDto {
    @SerializedName("users")
    private List<UserDto> users;

    public UsersListResponseDto() {
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}

