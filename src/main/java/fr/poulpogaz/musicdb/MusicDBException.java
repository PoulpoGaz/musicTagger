package fr.poulpogaz.musicdb;

public class MusicDBException extends RuntimeException {

    public MusicDBException() {
    }

    public MusicDBException(String message) {
        super(message);
    }

    public MusicDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public MusicDBException(Throwable cause) {
        super(cause);
    }

    public MusicDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
