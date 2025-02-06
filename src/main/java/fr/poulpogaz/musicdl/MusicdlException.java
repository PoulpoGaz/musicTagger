package fr.poulpogaz.musicdl;

public class MusicdlException extends RuntimeException {

    public MusicdlException() {
    }

    public MusicdlException(String message) {
        super(message);
    }

    public MusicdlException(String message, Throwable cause) {
        super(message, cause);
    }

    public MusicdlException(Throwable cause) {
        super(cause);
    }

    public MusicdlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
