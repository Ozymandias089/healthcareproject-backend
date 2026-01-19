// 파일명: ImageController.java
package com.hcproj.healthcareprojectbackend.image.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    // Toast UI Editor에서 이미지를 올릴 때 호출되는 API
    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(@RequestParam("image") MultipartFile image) {
        String imageUrl = imageService.upload(image);
        return ApiResponse.ok(imageUrl);
    }
}