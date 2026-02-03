package com.hcproj.healthcareprojectbackend.global.mail.template;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class EmailTemplateLoader {
    public String render(String classpathLocation, Map<String, String> variables) {
        try {
            var resource = new ClassPathResource(classpathLocation);
            String html = resource.getContentAsString(StandardCharsets.UTF_8);

            for (var e : variables.entrySet()) {
                html = html.replace("{{" + e.getKey() + "}}", e.getValue());
            }

            return html;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render email template: " + classpathLocation, e);
        }
    }
}
