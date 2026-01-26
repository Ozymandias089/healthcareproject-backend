package com.hcproj.healthcareprojectbackend.workout.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.workout.dto.request.AiWorkoutRoutinesPutRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.AiWorkoutRoutinesResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.service.WorkoutAiRoutinesFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WorkoutAiController {

    private final WorkoutAiRoutinesFacade facade;

    @PutMapping("/workouts/ai/routines")
    public ApiResponse<AiWorkoutRoutinesResponseDTO> replaceRoutines(
            @CurrentUserId Long userId,
            @Valid @RequestBody AiWorkoutRoutinesPutRequestDTO request
    ) {
        return ApiResponse.ok(facade.replaceRoutines(userId, request));
    }
}
