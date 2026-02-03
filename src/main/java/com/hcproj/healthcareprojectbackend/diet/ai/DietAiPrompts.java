package com.hcproj.healthcareprojectbackend.diet.ai;

import com.hcproj.healthcareprojectbackend.global.ai.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DietAiPrompts {

    private final PromptLoader promptLoader;

    @Value("${ai.prompt.diet.week.system}")
    private String systemPath;

    @Value("${ai.prompt.diet.week.user}")
    private String userPath;

    public String system() {
        return promptLoader.loadClasspath(systemPath);
    }

    public String user(LocalDate startDate, List<String> allergies, String note, String allowedFoodsJson) {
        String template = promptLoader.loadClasspath(userPath);

        String safeNote = (note == null || note.isBlank()) ? "NONE" : note;
        String safeAllergies = (allergies == null) ? "[]" : allergies.toString();

        // 템플릿 안에 {startDate} 같은 placeholder를 써도 되고,
        // 지금처럼 formatted 기반으로 만들어도 됨.
        return template.formatted(startDate, safeAllergies, safeNote, allowedFoodsJson);
    }
}
