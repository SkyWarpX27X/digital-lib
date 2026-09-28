package ua.fictionallibrary.digital_lib.digitizedbook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DigitizedBookRepository extends JpaRepository<DigitizedBook, UUID> {

}
