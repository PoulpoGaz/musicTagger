package fr.poulpogaz.musicdl.utils;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class LimitedReader extends InputStreamReader {

    private final LimitedInputStream in;

    public LimitedReader(LimitedInputStream in) {
        super(in);
        this.in = in;
    }

    public LimitedReader(LimitedInputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in, charsetName);
        this.in = in;
    }

    public LimitedReader(LimitedInputStream in, Charset cs) {
        super(in, cs);
        this.in = in;
    }

    public LimitedReader(LimitedInputStream in, CharsetDecoder dec) {
        super(in, dec);
        this.in = in;
    }

    public long remainingBytes() {
        return in.remainingBytes();
    }
}
