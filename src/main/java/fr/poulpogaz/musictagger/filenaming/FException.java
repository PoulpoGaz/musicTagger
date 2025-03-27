package fr.poulpogaz.musictagger.filenaming;

public class FException extends RuntimeException {

    public FException() {
    }

    public FException(String message) {
        super(message);
    }

    public FException(String message, Throwable cause) {
        super(message, cause);
    }

    public FException(Throwable cause) {
        super(cause);
    }

    public FException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
