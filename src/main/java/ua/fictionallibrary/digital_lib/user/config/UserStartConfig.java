package ua.fictionallibrary.digital_lib.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserRole;

@Configuration(proxyBeanMethods = false)
public class UserStartConfig {

    @Bean
    @Profile("prod")
    public CommandLineRunner initAdmin(UserRepository repository,
                                       PasswordEncoder encoder,
                                       @Value("${app.admin.username}") String username,
                                       @Value("${app.admin.password}") String password) {
        return args -> {
            if (!repository.existsByUsername(username)) {
                repository.save(new User(
                        username,
                        encoder.encode(password),
                        "Адміністратор",
                        "Системи",
                        "",
                        UserRole.ADMIN,
                        true));
            }
        };
    }

    @Bean
    @Profile("dev")
    public CommandLineRunner init(UserRepository repository, PasswordEncoder encoder) {
        return args -> {
            repository.save(new User(
                    "username666",
                    encoder.encode("abba123"),
                    "Вадим",
                    "Вадименко",
                    "Вадимович",
                    UserRole.READER,
                    true));
            repository.save(new User(
                    "rogt",
                    encoder.encode("1m_1nl0veW!thMy__C@r"),
                    "Роджер",
                    "Тейлор",
                    "",
                    UserRole.BOOK_DELIVERY_DEPARTMENT,
                    true));
            repository.save(new User(
                    "vsauce",
                    encoder.encode("yourPasswordIsSecure_OrIsIt?"),
                    "Майкл",
                    "Стівенс",
                    "",
                    UserRole.DIGITAL_ARCHIVE_DEPARTMENT,
                    true));
            repository.save(new User(
                    "queen",
                    encoder.encode("M@m@@@@@@@!!!!___"),
                    "Фредді",
                    "Меркьюрі",
                    "Булсара",
                    UserRole.ADMIN,
                    true));
        };
    }
}
