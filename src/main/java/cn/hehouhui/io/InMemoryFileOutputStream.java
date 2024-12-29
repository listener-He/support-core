package cn.hehouhui.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 内存文件输出流
 *
 * @author HeHui
 * @date 2024-11-28 15:59
 */
public class InMemoryFileOutputStream extends OutputStream {

    private final InMemoryFile file;

    public InMemoryFileOutputStream(InMemoryFile file) {
        this.file = file;
    }

    @Override
    public void write(int b) throws IOException {
        file.write((byte)b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }
}
