package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UsersResponse {

    public static class UsersData {
        @SerializedName("users")
        private List<UserData> users;

        public UsersData() {
        }

        public List<UserData> getUsers() {
            return users;
        }

        public void setUsers(List<UserData> users) {
            this.users = users;
        }
    }
}

