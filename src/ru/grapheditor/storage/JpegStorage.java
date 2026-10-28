package ru.grapheditor.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JpegStorage extends RasterImageStorage {

    private static final int MARKER_SOI = 0xD8;
    private static final int MARKER_SOS = 0xDA;
    private static final int MARKER_COM = 0xFE;
    private static final int MAX_SEGMENT = 65533;

    @Override
    public String extension() {
        return "jpg";
    }

    @Override
    public String formatName() {
        return "JPEG с метаданными (*.jpg)";
    }

    @Override
    protected byte[] embed(byte[] image, byte[] payload) throws IOException {
        if (payload.length > MAX_SEGMENT) {
            throw new IOException("Метаданные слишком велики для JPEG");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(image, 0, 2);
        out.write(0xFF);
        out.write(MARKER_COM);
        out.write((payload.length + 2) >>> 8);
        out.write(payload.length + 2);
        out.write(payload);
        out.write(image, 2, image.length - 2);
        return out.toByteArray();
    }

    @Override
    protected byte[] extract(byte[] image) throws IOException {
        if (image.length < 4 || (image[0] & 0xFF) != 0xFF || (image[1] & 0xFF) != MARKER_SOI) {
            throw new IOException("Это не JPEG-файл");
        }
        int offset = 2;
        while (offset + 4 <= image.length) {
            if ((image[offset] & 0xFF) != 0xFF) {
                break;
            }
            int marker = image[offset + 1] & 0xFF;
            if (marker == MARKER_SOS) {
                break;
            }
            if (marker == MARKER_SOI || (marker >= 0xD0 && marker <= 0xD9) || marker == 0x01) {
                offset += 2;
                continue;
            }
            int len = ((image[offset + 2] & 0xFF) << 8) | (image[offset + 3] & 0xFF);
            if (len < 2 || offset + 2 + len > image.length) {
                break;
            }
            if (marker == MARKER_COM) {
                byte[] payload = new byte[len - 2];
                System.arraycopy(image, offset + 4, payload, 0, payload.length);
                return payload;
            }
            offset += 2 + len;
        }
        return null;
    }
}
