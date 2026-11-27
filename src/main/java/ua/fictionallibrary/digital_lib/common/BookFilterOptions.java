package ua.fictionallibrary.digital_lib.common;

import java.util.List;

public record BookFilterOptions(List<String> topics, List<String> languages, List<String> resourceTypes) { }
