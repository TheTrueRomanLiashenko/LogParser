package com.romanliashenko.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

public interface QLQuery {
    Set<Object> execute(String query) throws IOException, ParseException;
}