package com.hcproj.healthcareprojectbackend.pt.bootstrap;

import com.hcproj.healthcareprojectbackend.pt.service.PtJanusKeyPoolInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PtJanusKeyPoolInitializer implements CommandLineRunner {

    private final PtJanusKeyPoolInitService initService;

    @Override
    public void run(String... args) {
        initService.initIfEmpty();
    }
}
