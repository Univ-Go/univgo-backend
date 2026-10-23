package com.univgo.backend.users.infrastructure.adapter.in.web.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmation) {
}
