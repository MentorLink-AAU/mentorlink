package com.mentorlink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // ✅ use application-test.properties instead of MySQL
class MentorlinkBackendApplicationTests {

    @Test
    void contextLoads() {
        // This ensures Spring context starts correctly
    }
}
