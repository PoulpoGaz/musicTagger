package fr.poulpogaz.musictagger;

public class MTException extends RuntimeException {

    public MTException() {
    }

    public MTException(String message) {
        super(message);
    }

    public MTException(String message, Throwable cause) {
        super(message, cause);
    }

    public MTException(Throwable cause) {
        super(cause);
    }

    public MTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
