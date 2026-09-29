package ua.fictionallibrary.digital_lib.author.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorRepository;
import ua.fictionallibrary.digital_lib.author.AuthorService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository repository;

    public AuthorServiceImpl(AuthorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Set<Author> findOrCreate(Collection<String> names) {
        if (names == null) return new LinkedHashSet<>();
        return names.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .map(name -> repository.findByName(name)
                        .orElseGet(() -> repository.save(new Author(name))))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
