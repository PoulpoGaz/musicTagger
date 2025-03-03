package fr.poulpogaz.musicdl.opus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;

public class OpusMetadataWriter {

    private static final Logger LOGGER = LogManager.getLogger(OpusMetadataWriter.class);

    // content of each metadata page will take up to PAGE_DATA_SIZE bytes
    // except the content of the last page which can take up to PAGE_DATA_SIZE + WIGGLE_ROOM bytes
    // PAGE_DATA_SIZE should be a multiple of 255
    private static final int PAGE_DATA_SIZE = 4096 / 255 * 255;
    private static final int WIGGLE_ROOM = 2048;
    private static final int STANDARD_PAGE_SIZE = OggPage.MIN_HEADER_SIZE + PAGE_DATA_SIZE / 255 + PAGE_DATA_SIZE;

    private final Path path;

    private final CommentBytes commentBytes = new CommentBytes();

    private int commentCountPosition;
    private int commentCount = 0;

    public OpusMetadataWriter(Path path) {
        this.path = path;

        try {
            commentBytes.write(OpusInputStream.OPUS_TAGS);
        } catch (IOException _) {}
    }

    public void setVendor(String vendor) {
        // TODO: check
        try {
            // write vendor
            byte[] bytes = vendor.getBytes(StandardCharsets.UTF_8);
            IOUtils.writeIntL(commentBytes, bytes.length);
            commentBytes.write(bytes);

            // save comment count position for later
            commentCountPosition = commentBytes.getCount();
            commentBytes.skip(4);
        } catch (IOException _) {}
    }

    public void addComment(String key, String value) {
        // TODO: check key
        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalStateException("use addCoverArt");
        }
        try {
            commentCount++;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

            IOUtils.writeIntL(commentBytes, keyBytes.length + 1 + valueBytes.length);
            commentBytes.writeBytes(keyBytes);
            commentBytes.write('=');
            commentBytes.writeBytes(valueBytes);
        } catch (IOException _) {}
    }

    public void addCoverArt(BufferedImage image, String description, CoverType type) throws IOException {
        commentCount++;

        // value
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        OutputStream base64 = Base64.getEncoder().wrap(imageBytes);

        // write metadata picture header
        IOUtils.writeIntB(base64, type.ordinal());
        IOUtils.writeStringWithLength(base64, "image/png");
        if (description != null) {
            IOUtils.writeStringWithLength(base64, description);
        } else {
            IOUtils.writeIntB(base64, 0);
        }
        IOUtils.writeIntB(base64, image.getWidth());
        IOUtils.writeIntB(base64, image.getHeight());
        IOUtils.writeIntB(base64, image.getColorModel().getPixelSize());
        if (image.getColorModel() instanceof IndexColorModel model) {
            IOUtils.writeIntB(base64, model.getMapSize());
        } else {
            IOUtils.writeIntB(base64, 0);
        }

        // write image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        IOUtils.writeIntB(base64, baos.size());
        baos.writeTo(base64);
        base64.close();


        // write comment
        byte[] keyBytes = "METADATA_BLOCK_PICTURE".getBytes(StandardCharsets.UTF_8);

        IOUtils.writeIntL(commentBytes, keyBytes.length + 1 + imageBytes.size());
        commentBytes.writeBytes(keyBytes);
        commentBytes.write('=');
        imageBytes.writeTo(commentBytes);
    }

    public void write() throws IOException {
        commentBytes.writeIntAt(commentCountPosition, commentCount);

        LOGGER.debug("Overwriting comments in {}", path);
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            OggInputStream ois = new OggInputStream(fc);
            OpusInputStream opus = new OpusInputStream(ois);

            OpusHead head = opus.readOpusHead();
            OggPage headPage = head.getPage();
            int headSeqNumber = headPage.getPageSequenceNumber();
            int bitstreamSerialNumber = headPage.getBitstreamSerialNumber();
            int commentPos = headPage.getPageSize();

            LOGGER.debug("Comment found at {}, head sequence number {}, serial number {}",
                         commentPos, headSeqNumber, bitstreamSerialNumber);

            // compute size of current comment size
            // opus tag; vendor length; vendor; comment count
            long commentSize = 8 + 4 + opus.readVendorLength() + 4;
            opus.skipVendor();

            long n = opus.readCommentCount();
            for (int i = 0; i < n; i++) {
                // comment length ; comment
                commentSize += 4 + opus.readCommentLength();
                opus.skipComment();
            }

            LOGGER.debug("Comment size: {}, new comment size: {}", commentSize, commentBytes.getCount());

            long padding = hasPadding(opus);
            if (padding >= 0) {
                addPadding(commentSize, padding);
            } else {
                LOGGER.debug("Preserving padding");
            }

            long currentCommentEncodedLength = ois.currentPagePosition() - headPage.getPageSize();
            int pageCount = commentBytes.pageCount();
            int commentEncodedLength = commentBytes.pageEncodedLength();

            LOGGER.debug("Comment size {} - new {}. Encoded {} - new {}",
                         commentSize, commentBytes.getCount(), currentCommentEncodedLength, commentEncodedLength);

            // resize file if necessary
            // it also numbers pages after opus tag
            renumberAndResize(fc, ois, currentCommentEncodedLength, commentEncodedLength, ois.nextPage(), headSeqNumber + pageCount + 1);

            // insert opus tag
            insertComment(fc, ois.buffer, commentPos, bitstreamSerialNumber, headSeqNumber + 1);
        }
    }

    private long hasPadding(OpusInputStream opus) throws IOException {
        InputStream is = opus.opusTagPadding();
        int r = is.read();

        if (r < 0) {
            return 0; // no padding
        } else if ((r & 0x1) == 0) {
            return 1 + opus.skipOpusTagPadding(); // preserve flag is set to zero
        } else {
            is.transferTo(commentBytes);
            return -1;
        }
    }

    private void addPadding(long currentCommentSize, long currentPaddingSize) {
        int newCommentSize = commentBytes.getCount();
        long currentOpusTagSize = currentCommentSize + currentPaddingSize;

        int low = 1024;
        int high = 10 * low;

        long newPadding = currentOpusTagSize - newCommentSize;
        if (newPadding < low || newPadding > high) {
            newPadding = (low + high) / 2;
            LOGGER.debug("Adding {} bytes of padding", newPadding);
        } else {
            LOGGER.debug("Using {} bytes of already present padding", newPadding);
        }

        for (int i = 0; i < (int) newPadding; i++) {
            commentBytes.write(0);
        }
    }

    private void insertComment(FileChannel fc, ByteBuffer tmp, int commentPos, int bitstreamNum, int pageSeqNum) throws IOException {
        int pageCount = commentBytes.pageCount();
        int commentEncodedLength = commentBytes.pageEncodedLength();

        ByteBuffer comment = ByteBuffer.wrap(commentBytes.getBuffer(), 0, commentBytes.getCount());
        comment.limit(0);

        fc.position(commentPos);

        int pageSize = STANDARD_PAGE_SIZE;
        int dataSize = PAGE_DATA_SIZE;
        for (int i = 0; i < pageCount; i++, pageSeqNum++) {
            boolean lastPage = i + 1 >= pageCount;
            boolean firstPage = i == 0;

            if (lastPage) { // last page
                pageSize = commentEncodedLength - (pageCount - 1) * STANDARD_PAGE_SIZE;
                dataSize = commentBytes.getCount() - comment.position();
            }

            comment.limit(comment.limit() + dataSize);

            tmp.clear();
            writePageHeader(tmp, firstPage, lastPage, bitstreamNum, pageSeqNum, dataSize);
            tmp.put(comment);

            tmp.putInt(22, CRC32.getCRC(tmp, 0, pageSize));

            tmp.flip();
            fc.write(tmp);
        }
    }

    private void writePageHeader(ByteBuffer dest, boolean firstPage, boolean lastPage, int bitstreamSerialNumber, int pageSedNum, int dataSize) {
        dest.put(OggPage.MAGIC_HEADER_BYTES);
        dest.put((byte) 0); // version
        dest.put((byte) (firstPage ? 0 : 1)); // header type
        dest.putLong(lastPage ? 0 : -1); // granule position
        dest.putInt(bitstreamSerialNumber);
        dest.putInt(pageSedNum);
        dest.putInt(0); // CRC 32

        if (dataSize == 0) {
            throw new IllegalStateException();
        }
        int oggSegmentCount = (dataSize - 1) / 255 + 1;
        dest.put((byte) oggSegmentCount);

        for (int i = 0; i < oggSegmentCount - 1; i++) {
            dest.put((byte) 255);
        }
        dest.put((byte) (dataSize - (oggSegmentCount - 1) * 255));
    }

    private void renumberAndResize(FileChannel fc, OggInputStream ois,
                                   long currentOpusTagLength, long newOpusTagLength,
                                   OggPage firstPage, int firstPageNewSequenceNumber) throws IOException {
        if (firstPage == null) {
            // only need to resize
            if (currentOpusTagLength != newOpusTagLength) {
                long newSize = fc.size() + newOpusTagLength - currentOpusTagLength;
                fc.truncate(newSize);
            }

        } else {
            boolean renumber = firstPage.getPageSequenceNumber() != firstPageNewSequenceNumber;
            if (currentOpusTagLength == newOpusTagLength
                    && !renumber) {
                return;
            }

            long audioDataPos = ois.currentPagePosition();
            long newAudioDataPos = audioDataPos + newOpusTagLength - currentOpusTagLength;

            LOGGER.debug("Moving audio data from {} to {}", audioDataPos, newAudioDataPos);

            if (newOpusTagLength < currentOpusTagLength) {
                IOUtils.shrink(fc, audioDataPos, newAudioDataPos);
            } else {
                IOUtils.grow(fc, audioDataPos, newAudioDataPos);
            }

            // renumber
            fc.position(newAudioDataPos);
            renumber(fc, ois, firstPageNewSequenceNumber);
        }
    }

    private void renumber(FileChannel fc, OggInputStream ois, int seqNum) throws IOException {
        ByteBuffer buff = ois.buffer.limit(0);

        long writePos = fc.position();
        OggPage page = new OggPage();
        while (ois.nextPage(page) != null) {
            buff.position(buff.position() - page.getPageSize());
            buff.putInt(buff.position() + 18, seqNum);
            buff.putInt(buff.position() + 22, 0);
            buff.putInt(buff.position() + 22, CRC32.getCRC(buff, buff.position(), page.getPageSize()));

            int lim = buff.limit();
            buff.limit(buff.position() + page.getPageSize());
            fc.write(buff, writePos);
            writePos += page.getPageSize();

            buff.limit(lim);

            seqNum++;
        }
    }

    private static class CommentBytes extends ByteArrayOutputStream {

        public void writeIntAt(int pos, int value) {
            IOUtils.writeInt(buf, pos, value);
        }

        public void skip(int byteCount) {
            int minLength = count + byteCount;
            if (count + byteCount >= buf.length) {
                // a bit dirty
                if (minLength > 1) {
                    count += minLength - 1;
                }
                write(0); // will grow the buffer
            } else {
                count += byteCount;
            }
        }

        public byte[] getBuffer() {
            return buf;
        }

        public int getCount() {
            return count;
        }

        public int pageEncodedLength() {
            int pageCount = pageCount();

            // the number of bytes written if the content of all bytes take PAGE_DATA_SIZE bytes
            int covered = pageCount * PAGE_DATA_SIZE;

            if (covered != count) {
                // last page is bigger or smaller
                int lastPageSize = count - (pageCount - 1) * PAGE_DATA_SIZE;
                int oggSegmentCount = (lastPageSize - 1) / 255 + 1;

                return (pageCount - 1) * STANDARD_PAGE_SIZE +
                        OggPage.MIN_HEADER_SIZE + oggSegmentCount + lastPageSize;
            } else {
                return pageCount * STANDARD_PAGE_SIZE;
            }
        }

        public int pageCount() {
            if (count == 0) {
                return 0;
            }

            int mod = count % PAGE_DATA_SIZE;
            int pageCount = (count - 1) / PAGE_DATA_SIZE + 1;

            if (pageCount > 1 && 0 < mod && mod <= WIGGLE_ROOM) {
                return pageCount - 1;
            } else {
                return pageCount;
            }
        }
    }

    public static void main(String[] args) {
        try {
            Path target = Path.of("copy.opus");
            Files.copy(Path.of("Fate · Ending [FakeIt · Hiroyuki Sawano].opus"), target,
                       StandardCopyOption.REPLACE_EXISTING);
            OpusMetadataWriter omw = new OpusMetadataWriter(target);
            omw.setVendor("Lavf59.27.100");
            omw.addComment("Encoder", "Lavf59.27.100");
            omw.addComment("hello", "world");
            omw.addComment("hello2", "world2");
            omw.write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
