package fr.poulpogaz.musicdl.opus;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OpusMetadataWriter implements Closeable {

    private static final int PAGE_SIZE = 4096 / 255 * 255;

    private final FileChannel channel;
    private final OggInputStream ois;
    private long remainingComment = -1;

    private OggPage currentPage;
    private int position;

    public OpusMetadataWriter(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        this.ois = new OggInputStream(channel);

        skipToComment();
    }

    private void skipToComment() throws IOException {
        // skip OpusHead
        nextPage();

        // start reading OpusTags
        nextPage();
        IOUtils.assertBytes(currentPage.getData(), OpusInputStream.OPUS_TAGS);

        // skip vendor
        long vendorLength = currentPage.getUInt(8);
        skipBytes(vendorLength);
    }

    private void skipBytes(long vendorLength) throws IOException {
        while (vendorLength > 0) {
            int skip = (int) Math.min(vendorLength, currentPage.getPacketSize() - position);
            vendorLength -= skip;
            position += skip;

            if (vendorLength > 0) {
                nextPage();
            }
        }
    }

    public void setCommentCount(long count) throws IOException {
        if (remainingComment >= 0) {
            throw new IOException("Comment count already set");
        }

        this.remainingComment = count;
    }

    public void writeComment(String comment) {
        remainingComment--;
    }

    public void writeComment(long length, Reader r) {
        remainingComment--;
    }

    public void writePicture(MetadataPicture picture) {
        remainingComment--;
    }

    private void nextPage() throws IOException {
        currentPage = ois.nextPage();
        position = currentPage == null ? -1 : 0;
    }

    @Override
    public void close() throws IOException {

    }
}
