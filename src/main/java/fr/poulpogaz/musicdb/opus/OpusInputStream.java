package fr.poulpogaz.musicdb.opus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class OpusInputStream implements Closeable {

    private static final byte[] OPUS_TAGS = "OpusTags".getBytes(StandardCharsets.UTF_8);

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
        READ_OPUS_HEAD,      // next state: READ_VENDOR_LENGTH
        READ_VENDOR_LENGTH,  // previous: READ_OPUS_HEAD, next state: READ_VENDOR
        READ_VENDOR,         // previous: READ_OPUS_HEAD, READ_VENDOR_LENGTH, next state: READ_COMMENT_COUNT
        READ_COMMENT_COUNT,  // previous: READ_VENDOR
        READ_COMMENT_LENGTH,
        READ_COMMENT,
        READ_COMMENT_VALUE,
        READ_OGG_PAGE
    }











    private static final AtomicInteger COUNTER = new AtomicInteger();


    public static void main(String[] args) throws IOException {
        testSingleThread();
    }

    private static void testSingleThread() throws IOException {
        long time = System.currentTimeMillis();
        Files.walkFileTree(Path.of("musics"), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                readOpus(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        long time2 = System.currentTimeMillis();
        System.out.println("Total time: " + (time2 - time));
    }


    private static void readOpus(Path path) {
        // System.out.println(COUNTER.incrementAndGet());

        try (OpusInputStream file = new OpusInputStream(path)) {
            file.readOpusHead();
            // System.out.println(head.getPage());
            // System.out.println(head);
            // LimitedInputStream vis = file.vendorInputStream();
            // System.out.println(new String(vis.readAllBytes(), StandardCharsets.UTF_8));

            LimitedInputStream lis = file.vendorInputStream();
            lis.skipNBytes(lis.remainingBytes());

            long c = file.readCommentCount();
            // System.out.println(c);

            for (long i = 0; i < c; i++) {
                String key = file.readKey();

                if (key.equals("METADATA_BLOCK_PICTURE")) {
                    InputStream picIS = Base64.getDecoder().wrap(file.valueInputStream());
                    MetadataPicture pic = MetadataPicture.fromInputStream(picIS);
                    // System.out.println(pic);
                    //Files.write(Path.of("pic.png"), pic.getData());
                } else {
                    file.readValue();
                    // System.out.println(key + " = " + file.readValue());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
