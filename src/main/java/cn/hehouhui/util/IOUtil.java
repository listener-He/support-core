package cn.hehouhui.util;

import cn.hehouhui.exception.PluginException;
import cn.hehouhui.io.InMemoryFile;
import cn.hehouhui.io.StreamFilter;

import java.io.*;

/**
 * io流工具
 *
 * @author HeHui
 * @date 2024-11-26 09:35
 */
public class IOUtil {

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     *
     * @param inputStream
     *            输入流，读取完毕会将流关闭
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream) {
        return read(inputStream, true);
    }

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     *
     * @param inputStream
     *            输入流
     * @param close
     *            是否关闭，true表示读取完毕关闭流
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream, boolean close) {
        return read(inputStream, 4096, close);
    }

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     *
     * @param inputStream
     *            输入流
     * @param bufferSize
     *            buffer大小
     * @param close
     *            是否关闭，true表示读取完毕关闭流
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream, int bufferSize, boolean close) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
        byte[] buffer = new byte[bufferSize];
        int len;
        try (InputStream ignored = close ? inputStream : null) {
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new PluginException("IO Exception occurred", e);
        }
    }

    /**
     * 将数据写入到输出流中
     *
     * @param outputStream
     *            输出流
     * @param data
     *            要写出的数据，不能为null
     */
    public static void write(OutputStream outputStream, byte[] data) {
        write(outputStream, data, 0, data.length);
    }

    /**
     * 将数据写入到输出流中
     *
     * @param outputStream
     *            输出流
     * @param data
     *            要写出的数据，不能为null
     * @param offset
     *            要写出的数据的起始位置
     * @param len
     *            要写出数据的长度
     */
    public static void write(OutputStream outputStream, byte[] data, int offset, int len) {
        try {
            outputStream.write(data, offset, len);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将输入流中的内容写入到输出流
     *
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     */
    public static void write(OutputStream outputStream, InputStream inputStream) {
        write(outputStream, inputStream, false);
    }

    /**
     * 将输入流中的内容写入到输出流
     *
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     * @param close
     *            是否关闭输入流，true表示读取完毕关闭输入流，注意，这个关闭的是输入流，不是输出流
     */
    public static void write(OutputStream outputStream, InputStream inputStream, boolean close) {
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (close) {
                close(inputStream);
            }
        }
    }

    /**
     * 关闭指定资源
     *
     * @param closeable
     *            要关闭的资源
     */
    public static void close(Closeable closeable) {
        Assert.argNotNull(closeable, "closeable");
        try {
            closeable.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将输入流copy到memory file，如果输入流超过传入limit大小，将会写入本地临时文件
     *
     * @param stream
     *            输入流
     * @param bufferSize
     *            buffer size
     * @param limit
     *            内存中保留的最大buffer
     * @param streamFilter
     *            stream filter
     * @return 新的输入流
     * @throws IOException
     *             IO异常
     */
    public static InputStream copy(InputStream stream, int bufferSize, int limit, StreamFilter streamFilter)
        throws IOException {
        if (stream == null) {
            throw new NullPointerException("stream或者buffer不能为null");
        } else if (bufferSize <= 0 || limit <= 0 || limit < bufferSize) {
            throw new IllegalArgumentException("bufferSize、limit不能小于0，bufferSize不能大于limit");
        }

        InMemoryFile memoryFile = new InMemoryFile(bufferSize, limit, streamFilter);

        int len;
        byte[] buffer = new byte[bufferSize];

        while ((len = stream.read(buffer, 0, buffer.length)) > 0) {
            memoryFile.write(buffer, 0, len);
        }

        memoryFile.writeFinish();
        return memoryFile.getDataAsInputStream();
    }

    /**
     * 关闭一个或多个流对象
     *
     * @param closeable
     *            可关闭的流对象列表
     *
     * @throws IOException
     *             IOException
     */
    public static void close(Closeable... closeable) throws IOException {
        if (closeable != null) {
            for (Closeable c : closeable) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    /**
     * 关闭一个或多个流对象
     */
    public static void closeQuietly(Closeable... closeable) {
        try {
            close(closeable);
        } catch (IOException e) {
            // do nothing
        }
    }
}
