package com.hcproj.healthcareprojectbackend.me.dto.response;

import com.hcproj.healthcareprojectbackend.me.dto.internal.InjuryDTO;

import java.util.List;

public record UserInjuriesResponseDTO(List<InjuryDTO> injuries) {}
