package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.MusicdlException;

public class IllegalFormatException extends MusicdlException {

    public IllegalFormatException() {
    }

    public IllegalFormatException(String message) {
        super(message);
    }

    public IllegalFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalFormatException(Throwable cause) {
        super(cause);
    }

    public IllegalFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
