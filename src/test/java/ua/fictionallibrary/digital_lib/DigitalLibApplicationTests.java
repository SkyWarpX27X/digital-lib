package ua.fictionallibrary.digital_lib;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

class DigitalLibApplicationTests {

	@Test
	void verifyArchitecture(){
		ApplicationModules.of(DigitalLibApplication.class, JavaClass.Predicates.resideOutsideOfPackages(
				"ua.fictionallibrary.digital_lib.starter.."
		)).verify();
	}

}
