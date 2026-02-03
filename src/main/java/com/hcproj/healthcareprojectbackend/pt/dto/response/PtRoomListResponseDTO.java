package com.hcproj.healthcareprojectbackend.pt.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PtRoomListResponseDTO {

    private List<ItemDTO> items;
    private PageInfo pageInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDTO {
        private Long ptRoomId;
        private String title;
        private String description;
        private PtRoomType roomType;
        private PtRoomStatus status;
        private Instant scheduledAt;
        private Boolean isPrivate;
        private TrainerDTO trainer;
        private ParticipantsDTO participants;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrainerDTO {
        private String nickname;
        private String handle;
        private String profileImageUrl;
        private String bio;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantsDTO {
        private int current;
        private Integer max;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageInfo {
        private Long nextCursorId;
        private boolean hasNext;
        private int size;
    }
}