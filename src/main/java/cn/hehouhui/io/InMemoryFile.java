package cn.hehouhui.io;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.util.Assert;
import cn.hehouhui.util.IOUtil;
import cn.hehouhui.util.ReferenceUtil;
import cn.hehouhui.util.StrUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存文件，当数据小于指定阈值时，使用内存缓存，当数据大于指定阈值时，将文件写出到磁盘；
 * <p>
 * 当数据写出完毕时允许以输入流的形式获取该数据；
 * <p>
 * 非线程安全，允许写入和读取在不同线程
 *
 * @author HeHui
 * @date 2024-11-28 13:30
 */
public class InMemoryFile implements Closeable {

    /**
     * 当缓存超过该值时
     */
    private final int limit;

    /**
     * 数据过滤器，写出数据时先经过该过滤器
     */
    private final StreamFilter filter;

    private OutputStream outputStream;

    private volatile File file;

    private volatile byte[] buffer;

    private volatile AtomicInteger index;

    private volatile boolean close;

    private volatile boolean release;

    /**
     * 数据长度
     */
    private volatile int len;

    private volatile Charset charset;

    /**
     * 将数据包装为内存文件
     *
     * @param data 数据
     *
     * @return 内存文件
     */
    public static InMemoryFile wrap(byte[] data) {
        InMemoryFile inMemoryFile = new InMemoryFile(data.length, data.length);
        inMemoryFile.buffer = data;
        inMemoryFile.index = new AtomicInteger(data.length);
        inMemoryFile.len = data.length;
        inMemoryFile.close = true;
        return inMemoryFile;
    }

    public InMemoryFile(int initBuffer, int limit) {
        this(initBuffer, limit, null);
    }

    public InMemoryFile(int initBuffer, int limit, StreamFilter filter) {
        this.limit = limit;
        this.buffer = new byte[initBuffer];
        this.filter = filter;
        this.index = new AtomicInteger(0);
        this.close = false;
        this.release = false;
        this.len = 0;
    }

    public void write(byte data) throws IOException {
        write(new byte[] {data}, 0, 1);
    }

    public void write(byte[] d) throws IOException {
        write(d, 0, d.length);
    }

    public void write(byte[] d, int o, int l) throws IOException {
        if (d == null) {
            throw new NullPointerException();
        } else if (o < 0 || l < 0 || l > d.length - o) {
            throw new IndexOutOfBoundsException();
        } else if (l == 0) {
            return;
        } else if (close) {
            throw new IOException("文件已经关闭，无法写入");
        }

        Assert.assertTrue(this.len + l > 0, StrUtil.format("当前累计写出数据过大，无法继续写入, len: [{}], l: [{}]", this.len, l),
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);

        ByteBufferRef ref = new ByteBufferRef(d, o, l);
        if (filter != null) {
            ref = this.filter.filter(ref);
        }

        write0(ref);
    }

    /**
     * 将数据刷出，如果当前文件已经映射到磁盘则将剩余未写出数据写出到磁盘
     *
     * @throws IOException IO异常
     */
    public void flush() throws IOException {
        check();

        if (index.get() > 0 && outputStream != null) {
            outputStream.write(buffer, 0, index.get());
        }

        if (outputStream != null) {
            outputStream.flush();
        }
    }

    /**
     * 关闭输出，关闭输出后可以以输入流的形式获取数据
     *
     * @throws IOException IO异常
     */
    public void writeFinish() throws IOException {
        if (close) {
            return;
        }

        ByteBufferRef ref = null;
        if (filter != null) {
            ref = filter.finish();
        }

        if (ref != null) {
            write0(ref);
        }

        flush();

        if (outputStream != null) {
            outputStream.close();
        }

        close = true;
    }

    /**
     * 判断当前数据是否全在内存
     *
     * @return 如果当前数据全在内存则返回true
     */
    public boolean inMemory() {
        return file == null;
    }

    /**
     * 以输入流的形式获取数据
     *
     * @return 数据输入流
     *
     * @throws IOException IO异常
     */
    public InputStream getDataAsInputStream() throws IOException {
        if (!close) {
            throw new IOException("当前输出流还未关闭，无法获取输入流");
        }

        check();

        if (file == null) {
            return new ByteArrayInputStream(buffer, 0, index.get());
        } else {
            InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
            // 这里包一下的意义在于，外部可能把InMemoryFile引用释放了，但是还持有inputStream的引用，还希望继续读取，此时如果我们没有地方持有
            // InMemoryFile的引用的话临时文件就会被删除，所以我们这里使用一个内部类将InputStream包一下，同时内部类会持有InMemoryFile的
            // 引用，这样直到外部没有InMemoryFile的显式引用，同时所有InputStream被关闭后，临时文件才会被删除
            return new InMemoryFileInputStream(inputStream, this);
        }
    }

    @Override
    public void close() throws IOException {
        if (release) {
            return;
        }

        release = true;

        if (buffer != null) {
            buffer = null;
        }

        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }

        if (file != null) {
            if (file.exists()) {
                Assert.assertTrue(file.delete(), StrUtil.format("临时文件删除失败, [{}]", file.getAbsolutePath()),
                    ExceptionProviderConst.IllegalStateExceptionProvider);
            }
            file = null;
        }
    }

    /**
     * 以数组的形式获取数据
     *
     * @return 数据
     *
     * @throws IOException IO异常
     */
    public byte[] getData() throws IOException {
        if (file == null) {
            int i = index.get();
            if (i == buffer.length) {
                return buffer;
            } else if (i == 0) {
                return new byte[0];
            } else {
                return Arrays.copyOf(buffer, i);
            }
        } else {
            Assert.assertTrue(file.length() <= Integer.MAX_VALUE, "文件过大，不支持读取",
                ExceptionProviderConst.UnsupportedOperationExceptionProvider);
            InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
            return IOUtil.read(inputStream, (int) file.length(), true);
        }
    }

    /**
     * 获取数据长度
     *
     * @return 数据长度
     */
    public int getLen() {
        return len;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * 写入数据
     *
     * @param ref 要写入的数据
     *
     * @throws IOException IO异常
     */
    private void write0(ByteBufferRef ref) throws IOException {
        check();
        byte[] data = ref.getData();
        int offset = ref.getOffset();
        int len = ref.getLen();

        int i = index.get();
        int writeable = Math.min(len, buffer.length - i);
        if (writeable < len) {
            if ((limit - i) >= len) {
                // 扩容后足够写入本次数据，则扩容，并将数据写入
                int newBufferSize = Math.max(Math.min(buffer.length * 3 / 2, limit), len + i);
                newBufferSize = (limit - newBufferSize) < (newBufferSize / 10 * 3) ? limit : newBufferSize;
                byte[] newBuffer = new byte[newBufferSize];
                System.arraycopy(buffer, 0, newBuffer, 0, i);
                System.arraycopy(data, offset, newBuffer, i, len);
                buffer = newBuffer;
                index.set(i + len);
            } else {
                // 扩容后超过limit了，将数据填满，写出到文件，然后继续填充
                if (outputStream == null) {
                    file = File.createTempFile("WriteCache", ".tmp");
                    String filePath = file.getAbsolutePath();
                    outputStream = new FileOutputStream(file);
                    // 兜底释放文件，防止用户忘记释放
                    ReferenceUtil.listenDestroy(file, () -> new File(filePath).delete());
                }

                if (i > 0) {
                    outputStream.write(buffer, 0, i);
                    index.set(0);
                }

                outputStream.write(data, offset, len);

                // 直接扩容到最大
                if (buffer.length < limit) {
                    buffer = new byte[limit];
                }
            }
        } else {
            System.arraycopy(data, offset, buffer, i, len);
            index.set(i + len);
        }

        this.len += len;
    }

    private void check() throws IOException {
        if (release) {
            throw new IOException("当前资源已经释放");
        }
    }

    private static final class InMemoryFileInputStream extends InputStream {

        private final InputStream inputStream;

        private volatile InMemoryFile inMemoryFile;

        public InMemoryFileInputStream(InputStream inputStream, InMemoryFile inMemoryFile) {
            this.inputStream = inputStream;
            this.inMemoryFile = inMemoryFile;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return inputStream.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
            inMemoryFile = null;
        }

    }

}
