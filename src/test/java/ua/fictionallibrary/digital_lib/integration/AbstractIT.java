package ua.fictionallibrary.digital_lib.integration;

import jakarta.transaction.Transactional;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;

@Transactional
@IntegrationTest
public abstract class AbstractIT extends AbstractITSupport { }
