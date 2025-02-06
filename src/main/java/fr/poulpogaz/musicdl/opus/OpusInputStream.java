package fr.poulpogaz.musicdl.opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

public class OpusInputStream implements Closeable {

    public static final byte[] OPUS_TAGS = "OpusTags".getBytes(StandardCharsets.UTF_8);

    private final OggInputStream oggis;
    private State state = State.READ_OPUS_HEAD;

    private PacketInputStream pis;
    private LimitedInputStream lis;

    private int vendorLength;
    private long commentCount;
    private long nextCommentLength;

    public OpusInputStream(Path path) throws IOException {
        this(new OggInputStream(path));
    }

    public OpusInputStream(OggInputStream oggis) {
        this.oggis = Objects.requireNonNull(oggis);
    }

    public OpusHead readOpusHead() throws IOException {
        checkState(State.READ_OPUS_HEAD, true);

        OggPage page = oggis.nextPage();
        if (page == null || !page.isFirstPage()) {
            throw new IOException("No first page");
        }

        OpusHead head = new OpusHead(page);
        state = State.READ_VENDOR_LENGTH;
        return head;
    }


    private void readOpusTags() throws IOException {
        pis = new PacketInputStream(oggis);
        for (byte b : OPUS_TAGS) {
            IOUtils.assertByte((byte) pis.read(), b);
        }
    }

    public int readVendorStringLength() throws IOException {
        checkState(State.READ_VENDOR_LENGTH, true);
        readOpusTags();
        vendorLength = IOUtils.getInt(pis);
        state = State.READ_VENDOR;
        return vendorLength;
    }

    public String readVendor() throws IOException {
        checkState(State.READ_VENDOR, true);
        String vendor = IOUtils.readString(pis, vendorLength);
        readVendorNextState();
        return vendor;
    }

    public LimitedInputStream vendorInputStream() throws IOException {
        checkState(State.READ_VENDOR, true);
        return getOrCreateLimitedInputStream(vendorLength, this::readVendorNextState);
    }

    public LimitedReader vendorReader() throws IOException {
        return new LimitedReader(vendorInputStream(), StandardCharsets.UTF_8);
    }

    private void readVendorNextState() {
        state = State.READ_COMMENT_COUNT;
    }



    public long readCommentCount() throws IOException {
        checkState(State.READ_COMMENT_COUNT, true);

        commentCount = Integer.toUnsignedLong(IOUtils.getInt(pis));
        if (commentCount != 0) {
            state = State.READ_COMMENT_LENGTH;
        } else {
            state = State.READ_OGG_PAGE;
        }

        return commentCount;
    }


    public long readCommentLength() throws IOException {
        checkState(State.READ_COMMENT_LENGTH, true);

        nextCommentLength = Integer.toUnsignedLong(IOUtils.getInt(pis));
        state = State.READ_COMMENT;
        return nextCommentLength;
    }


    public String readComment() throws IOException {
        checkState(State.READ_COMMENT, true);

        String comment = IOUtils.readString(pis, nextCommentLength);
        readCommentNextState();
        return comment;
    }

    public LimitedInputStream commentInputStream() throws IOException {
        checkState(State.READ_COMMENT, true);
        return getOrCreateLimitedInputStream(nextCommentLength, this::readCommentNextState);
    }

    public LimitedReader commentReader() throws IOException {
        return new LimitedReader(commentInputStream(), StandardCharsets.UTF_8);
    }




    public String readKey() throws IOException {
        checkState(State.READ_COMMENT, true);

        lis = getOrCreateLimitedInputStream(nextCommentLength, null);
        String key = lis.readKey();
        state = State.READ_COMMENT_VALUE;
        return key;
    }

    public String readValue() throws IOException {
        checkState(State.READ_COMMENT_VALUE, false);

        String key = IOUtils.readString(lis, lis.remainingBytes());
        readCommentNextState();
        return key;
    }

    public LimitedInputStream valueInputStream() throws IOException {
        checkState(State.READ_COMMENT_VALUE, false);
        lis.setCloseAction(this::readCommentNextState);
        return lis;
    }

    public LimitedReader valueReader() throws IOException {
        return new LimitedReader(valueInputStream(), StandardCharsets.UTF_8);
    }

    private void readCommentNextState() {
        commentCount--;
        if (commentCount == 0) {
            state = State.READ_OGG_PAGE;
        } else {
            state = State.READ_COMMENT_LENGTH;
        }
    }


    private LimitedInputStream getOrCreateLimitedInputStream(long length, Runnable closeAction) throws IOException {
        if (lis == null) {
            lis = new LimitedInputStream(pis, length);
        } else {
            if (lis.remainingBytes() > 0) {
                lis.close();
            }
            lis.setRemaining(length);
        }
        lis.setCloseAction(closeAction);

        return lis;
    }

    public OggPage readPage() throws IOException {
        checkState(State.READ_OGG_PAGE, true);
        return oggis.nextPage();
    }


    private void checkState(State expectedState, boolean close) throws IOException {
        if (close && lis != null) {
            lis.close();
        }

        switch (expectedState) {
            case READ_VENDOR -> {
                if (state == State.READ_VENDOR_LENGTH) {
                    readVendorStringLength();
                }
            }
            case READ_COMMENT -> {
                if (state == State.READ_COMMENT_LENGTH) {
                    readCommentLength();
                }
            }
        }

        if (state != expectedState) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void close() throws IOException {
        oggis.close();
    }




    private enum State {
        READ_OPUS_HEAD,
        READ_VENDOR_LENGTH,
        READ_VENDOR,
        READ_COMMENT_COUNT,
        READ_COMMENT_LENGTH,
        READ_COMMENT,
        READ_COMMENT_VALUE,
        READ_OGG_PAGE
    }
}
