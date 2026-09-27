package ua.fictionallibrary.digital_lib.physicalbook;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBook;

import java.util.UUID;

public interface PhysicalBookRepository extends JpaRepository<PhysicalBook, UUID> {

}
