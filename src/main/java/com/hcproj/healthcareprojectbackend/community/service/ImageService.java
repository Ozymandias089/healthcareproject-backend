// 파일명: ImageService.java
package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    // 저장할 로컬 경로 (프로젝트 루트/uploads 폴더)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT); // ErrorCode에 추가 필요
        }

        try {
            // 1. 폴더가 없으면 생성
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 2. 파일명 중복 방지 (UUID 사용)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 3. 파일 저장
            File dest = new File(uploadDir + savedFilename);
            file.transferTo(dest);

            // 4. 접근 가능한 URL 반환 (예: /images/uuid.jpg)
            return "/images/" + savedFilename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}