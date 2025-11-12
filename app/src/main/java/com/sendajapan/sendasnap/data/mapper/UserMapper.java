package com.sendajapan.sendasnap.data.mapper;

import com.sendajapan.sendasnap.data.dto.UserDto;
import com.sendajapan.sendasnap.models.UserData;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static UserData toDomain(UserDto dto) {
        if (dto == null) {
            return null;
        }

        UserData user = new UserData();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(normalizeString(dto.getName()));
        user.setRole(normalizeString(dto.getRole()));
        user.setEmail(normalizeString(dto.getEmail()));
        user.setPhone(normalizeString(dto.getPhone()));
        user.setAvisId(normalizeString(dto.getAvisId()));
        user.setAvatar(normalizeString(dto.getAvatar()));
        user.setAvatarUrl(normalizeString(dto.getAvatarUrl()));
        user.setEmailVerifiedAt(normalizeString(dto.getEmailVerifiedAt()));
        user.setCreatedAt(normalizeString(dto.getCreatedAt()));
        user.setUpdatedAt(normalizeString(dto.getUpdatedAt()));
        return user;
    }

    public static List<UserData> toDomainList(List<UserDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<UserData> users = new ArrayList<>();
        for (UserDto dto : dtos) {
            UserData user = toDomain(dto);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    private static String normalizeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

