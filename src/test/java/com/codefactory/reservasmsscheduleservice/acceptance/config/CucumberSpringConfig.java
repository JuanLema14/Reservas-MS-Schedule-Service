package com.codefactory.reservasmsscheduleservice.acceptance.config;

import com.codefactory.reservasmsscheduleservice.service.ScheduleBlockService;
import com.codefactory.reservasmsscheduleservice.service.WorkScheduleService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfig {

    @MockBean
    WorkScheduleService workScheduleService;

    @MockBean
    ScheduleBlockService scheduleBlockService;
}