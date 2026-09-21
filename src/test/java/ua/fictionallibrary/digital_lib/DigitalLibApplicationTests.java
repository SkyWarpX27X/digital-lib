package ua.fictionallibrary.digital_lib;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class DigitalLibApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void verifyArchitecture(){
		ApplicationModules.of(DigitalLibApplication.class).verify();
	}

}
