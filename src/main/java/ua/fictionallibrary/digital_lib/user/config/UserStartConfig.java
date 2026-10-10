package ua.fictionallibrary.digital_lib.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserRole;

@Configuration(proxyBeanMethods = false)
public class UserStartConfig {

    @Bean
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
