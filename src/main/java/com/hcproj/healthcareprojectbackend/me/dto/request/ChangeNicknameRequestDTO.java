package com.hcproj.healthcareprojectbackend.me.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ChangeNicknameRequestDTO(@NotNull @Valid String nickname) {}
