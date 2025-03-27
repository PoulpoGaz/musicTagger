package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.MTException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorHandler {

    private final List<String> errors = new ArrayList<>();
    private final int maxErrors;

    public ErrorHandler(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    public void report(String error) {
        if (errors.size() >= maxErrors) {
            throw new MTException("Too many errors. Abort");
        }

        errors.add(error);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
