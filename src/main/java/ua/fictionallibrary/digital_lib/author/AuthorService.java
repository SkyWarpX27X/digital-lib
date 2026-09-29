package ua.fictionallibrary.digital_lib.author;

import java.util.Collection;
import java.util.Set;

public interface AuthorService {
    Set<Author> findOrCreate(Collection<String> names);
}
