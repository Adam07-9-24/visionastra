package com.visionastra.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=12345678901234567890123456789012",
		"openai.api.key=test",
		"gemini.api.key=test"
})
class VisionastraApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
