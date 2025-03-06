package fr.poulpogaz.musicdl.opus;

import fr.poulpogaz.musicdl.utils.LimitedInputStream;
import fr.poulpogaz.musicdl.utils.LimitedReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

// https://datatracker.ietf.org/doc/html/rfc7845
public class OpusInputStream implements Closeable {

    public static final byte[] OPUS_TAGS = "OpusTags".getBytes(StandardCharsets.UTF_8);

    private OpusHead head;

    private final OggInputStream oggis;
    private State state = State.READ_OPUS_HEAD;

    private PacketInputStream pis;
    private CommentInputStream cis;

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
        if (head != null) {
            return head;
        }

        checkState(State.READ_OPUS_HEAD);

        OggPage page = oggis.nextPage();
        if (page == null || !page.isFirstPage()) {
            throw new IOException("No first page");
        }

        head = new OpusHead(page);
        state = State.READ_VENDOR_LENGTH;
        return head;
    }


    private void readOpusTags() throws IOException {
        pis = new PacketInputStream(oggis);
        for (byte b : OPUS_TAGS) {
            IOUtils.assertByte((byte) pis.read(), b);
        }
    }



    public int readVendorLength() throws IOException {
        checkState(State.READ_VENDOR_LENGTH);
        readOpusTags();
        vendorLength = IOUtils.getInt(pis);
        state = State.READ_VENDOR;
        return vendorLength;
    }

    public String readVendor() throws IOException {
        checkState(State.READ_VENDOR);
        String vendor = IOUtils.readString(pis, vendorLength);
        readVendorNextState();
        return vendor;
    }

    public LimitedInputStream vendorInputStream() throws IOException {
        checkState(State.READ_VENDOR);
        return getOrCreateCommentInputStream(vendorLength, this::readVendorNextState);
    }

    public LimitedReader vendorReader() throws IOException {
        return new LimitedReader(vendorInputStream(), StandardCharsets.UTF_8);
    }

    private void readVendorNextState() {
        state = State.READ_COMMENT_COUNT;
    }

    public void skipVendor() throws IOException {
        checkState(State.READ_VENDOR);
        pis.skipNBytes(vendorLength);
        state = State.READ_COMMENT_COUNT;
    }



    public long readCommentCount() throws IOException {
        checkState(State.READ_COMMENT_COUNT);

        commentCount = Integer.toUnsignedLong(IOUtils.getInt(pis));
        if (commentCount != 0) {
            state = State.READ_COMMENT_LENGTH;
        } else {
            state = State.READ_OPUS_TAG_PADDING;
        }

        return commentCount;
    }

    public void skipComments() throws IOException {
        if (state == State.READ_COMMENT_COUNT) {
            readCommentCount();
        }
        if (state == State.READ_COMMENT || state == State.READ_COMMENT_LENGTH || state == State.READ_COMMENT_VALUE) {
            while (commentCount > 0) {
                skipComment();
            }

            state = State.READ_OPUS_TAG_PADDING;
        } else {
            throw new IOException();
        }
    }





    public long readCommentLength() throws IOException {
        checkState(State.READ_COMMENT_LENGTH);

        nextCommentLength = Integer.toUnsignedLong(IOUtils.getInt(pis));
        state = State.READ_COMMENT;
        return nextCommentLength;
    }

    public String readComment() throws IOException {
        checkState(State.READ_COMMENT);

        String comment = IOUtils.readString(pis, nextCommentLength);
        readCommentNextState();
        return comment;
    }

    public LimitedInputStream commentInputStream() throws IOException {
        checkState(State.READ_COMMENT);
        return getOrCreateCommentInputStream(nextCommentLength, this::readCommentNextState);
    }

    public LimitedReader commentReader() throws IOException {
        return new LimitedReader(commentInputStream(), StandardCharsets.UTF_8);
    }

    public void skipComment() throws IOException {
        checkState(State.READ_COMMENT);
        if (cis == null) {
            commentInputStream();
        } else if (cis.remainingBytes() == 0) {
            cis.close();
            commentInputStream();
        }
        cis.close();
    }

    /**
     * <a href="https://www.xiph.org/vorbis/doc/v-comment.html">v-comment</a>
     * A case-insensitive field name that may consist of ASCII 0x20 through
     * 0x7D, 0x3D ('=') excluded. ASCII 0x41 through 0x5A inclusive (A-Z) is
     * to be considered equivalent to ASCII 0x61 through 0x7A inclusive (a-z).
     */
    public String readKey() throws IOException {
        checkState(State.READ_COMMENT);

        cis = getOrCreateCommentInputStream(nextCommentLength, null);
        String key = cis.readKey();
        state = State.READ_COMMENT_VALUE;
        return key;
    }

    public InputStream keyInputStream() throws IOException {
        checkState(State.READ_COMMENT);
        return null;
    }

    public String readValue() throws IOException {
        checkState(State.READ_COMMENT_VALUE);

        String key = IOUtils.readString(cis, cis.remainingBytes());
        readCommentNextState();
        return key;
    }

    public LimitedInputStream valueInputStream() throws IOException {
        checkState(State.READ_COMMENT_VALUE);
        cis.setCloseAction(this::readCommentNextState);
        return cis;
    }

    public LimitedReader valueReader() throws IOException {
        return new LimitedReader(valueInputStream(), StandardCharsets.UTF_8);
    }

    private void readCommentNextState() {
        commentCount--;
        if (commentCount == 0) {
            state = State.READ_OPUS_TAG_PADDING;
        } else {
            state = State.READ_COMMENT_LENGTH;
        }
    }


    public InputStream opusTagPadding() throws IOException {
        checkState(State.READ_OPUS_TAG_PADDING);
        return pis;
    }

    public long skipOpusTagPadding() throws IOException {
        checkState(State.READ_OPUS_TAG_PADDING);
        return pis.skip(Long.MAX_VALUE);
    }


    private CommentInputStream getOrCreateCommentInputStream(long length, Runnable closeAction) throws IOException {
        if (cis == null) {
            cis = new CommentInputStream(pis, length);
        } else {
            cis.close();
            cis.setRemaining(length);
        }
        cis.setCloseAction(closeAction);

        return cis;
    }


    public long currentPagePosition() {
        return oggis.currentPagePosition();
    }

    public int positionInPage() {
        if (state == State.READ_OPUS_HEAD || state == State.READ_OGG_PAGE) {
            throw new IllegalStateException();
        }

        return pis.getPositionInPage();
    }


    public OggPage readPage() throws IOException {
        checkState(State.READ_OGG_PAGE);
        return oggis.nextPage();
    }


    public double fileLength() throws IOException {
        OggPage lastPage = oggis.readLastPage(head.getPage().getBitstreamSerialNumber());

        return head.computeStreamLength(lastPage);
    }


    private void checkState(State expectedState) throws IOException {
        switch (expectedState) {
            case READ_VENDOR -> {
                if (state == State.READ_VENDOR_LENGTH) {
                    readVendorLength();
                }
            }
            case READ_COMMENT -> {
                if (state == State.READ_COMMENT_LENGTH) {
                    readCommentLength();
                } else if (state == State.READ_COMMENT || state == State.READ_COMMENT_VALUE) {
                    if (cis != null) {
                        cis.close();
                    }
                    state = State.READ_COMMENT;
                }
            }
            case READ_OGG_PAGE -> {
                if (state == State.READ_OPUS_TAG_PADDING) {
                    pis.skip(Long.MAX_VALUE);
                    state = State.READ_OGG_PAGE;
                    return;
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
        READ_OPUS_TAG_PADDING,
        READ_OGG_PAGE
    }
}
