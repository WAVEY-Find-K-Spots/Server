package com.Wavey.WaveyService.domain.user.dto;

import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;

public record UserResponse(Long id, String name, String email, String provider, Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProvider(), user.getRole());
    }
}
