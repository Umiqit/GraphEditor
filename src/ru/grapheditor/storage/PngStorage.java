package ru.grapheditor.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class PngStorage extends RasterImageStorage {

    private static final byte[] KEYWORD = "GraphEditor".getBytes(StandardCharsets.ISO_8859_1);

    @Override
    public String extension() {
        return "png";
    }

    @Override
    public String formatName() {
        return "PNG с метаданными (*.png)";
    }

    @Override
    protected byte[] embed(byte[] image, byte[] payload) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(image, 0, 8);
        out.write(chunk("tEXt", textPayload(payload)));
        out.write(image, 8, image.length - 8);
        return out.toByteArray();
    }

    @Override
    protected byte[] extract(byte[] image) throws IOException {
        if (image.length < 8 || (image[0] & 0xFF) != 0x89 || image[1] != 0x50) {
            throw new IOException("Это не PNG-файл");
        }
        int offset = 8;
        while (offset + 12 <= image.length) {
            int len = readInt(image, offset);
            String type = new String(image, offset + 4, 4, StandardCharsets.ISO_8859_1);
            if (type.equals("tEXt") && len > 0 && offset + 12 + len <= image.length) {
                byte[] data = new byte[len];
                System.arraycopy(image, offset + 8, data, 0, len);
                byte[] value = matchKeyword(data);
                if (value != null) {
                    return value;
                }
            }
            offset += 12 + len;
        }
        return null;
    }

    private byte[] textPayload(byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(KEYWORD, 0, KEYWORD.length);
        out.write(0);
        out.write(payload, 0, payload.length);
        return out.toByteArray();
    }

    private byte[] matchKeyword(byte[] data) {
        if (data.length <= KEYWORD.length || data[KEYWORD.length] != 0) {
            return null;
        }
        for (int i = 0; i < KEYWORD.length; i++) {
            if (data[i] != KEYWORD[i]) {
                return null;
            }
        }
        byte[] value = new byte[data.length - KEYWORD.length - 1];
        System.arraycopy(data, KEYWORD.length + 1, value, 0, value.length);
        return value;
    }

    private byte[] chunk(String type, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(intBytes(data.length));
        byte[] typeBytes = type.getBytes(StandardCharsets.ISO_8859_1);
        out.write(typeBytes);
        out.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        out.write(intBytes((int) crc.getValue()));
        return out.toByteArray();
    }

    private int readInt(byte[] b, int off) {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16)
                | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }

    private byte[] intBytes(int v) {
        return new byte[]{(byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) v};
    }
}
