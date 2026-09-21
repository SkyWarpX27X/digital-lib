plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ua.fictionallibrary"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.springframework.modulith:spring-modulith-bom:2.1.1"))
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.modulith:spring-modulith-starter-core")
	implementation("org.springframework.modulith:spring-modulith-events-api")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
