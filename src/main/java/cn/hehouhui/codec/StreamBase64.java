package cn.hehouhui.codec;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.util.Assert;

import java.util.Arrays;

/**
 * 流式base64解码器，允许分批解码；
 * <p>
 * PS: 如果不需要分批节码，请直接使用{@link java.util.Base64}
 *
 * @author HeHui
 * @date 2024-11-28 15:53
 */
public class StreamBase64 {

    private StreamBase64() {
        throw new AssertionError();
    }

    /**
     * This array is a lookup table that translates 6-bit positive integer index values into their "Base64 Alphabet"
     * equivalents as specified in "Table 1: The Base64 Alphabet" of RFC 2045 (and RFC 4648).
     */
    private static final char[] toBase64 =
        {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    /**
     * It's the lookup table for "URL and Filename safe Base64" as specified in Table 2 of the RFC 4648, with the '+'
     * and '/' changed to '-' and '_'. This table is used when BASE64_URL is specified.
     */
    private static final char[] toBase64URL =
        {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    /**
     * 创建一个decoder
     *
     * @return decoder
     */
    public static Decoder newDecoder() {
        return new Decoder(false, false);
    }

    /**
     * 非线程安全
     */
    public static final class Decoder {

        private static final byte[] EMPTY = new byte[0];

        private static final int[] fromBase64 = new int[256];

        static {
            Arrays.fill(fromBase64, -1);
            for (int i = 0; i < toBase64.length; i++) {
                fromBase64[toBase64[i]] = i;
            }
            fromBase64['='] = -2;
        }

        private static final int[] fromBase64URL = new int[256];

        static {
            Arrays.fill(fromBase64URL, -1);
            for (int i = 0; i < toBase64URL.length; i++) {
                fromBase64URL[toBase64URL[i]] = i;
            }
            fromBase64URL['='] = -2;
        }

        private final boolean isURL;

        private final boolean isMIME;

        private final byte[] legacy;

        private int legacyLen;

        /**
         * 当前整体offset
         */
        private long offset;

        /**
         * base64流是否结束，true表示已经结束
         */
        private boolean finish;

        private Decoder(boolean isURL, boolean isMIME) {
            this.isURL = isURL;
            this.isMIME = isMIME;
            this.legacy = new byte[3];
            this.legacyLen = 0;
            this.finish = false;
        }

        /**
         * 结束流
         *
         * @return 最后的数据，如果还有的话
         */
        public byte[] doFinal() {
            return doFinal(EMPTY);
        }

        /**
         * 结束流
         *
         * @param data
         *            最后更新到流中的数据
         * @return 最后的数据，如果还有的话
         */
        public byte[] doFinal(byte[] data) {
            return doFinal(data, 0, data.length);
        }

        /**
         * 结束流
         *
         * @param data
         *            最后更新到流中的数据
         * @param offset
         *            offset
         * @param len
         *            len
         * @return 最后的数据，如果还有的话
         */
        public byte[] doFinal(byte[] data, int offset, int len) {
            int resultLen = outLength(data, offset, len, true);
            if (resultLen == 0) {
                return EMPTY;
            }

            byte[] result = new byte[resultLen];
            update0(data, offset, len, result, 0, true);
            finish = true;
            return result;
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @return 本次解析结果
         */
        public byte[] update(byte[] src) {
            int offset = 0;
            int len = src.length;
            byte[] result = new byte[outLength(src, offset, len, false)];
            update0(src, offset, len, result, 0, false);
            return result;
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @param offset
         *            源数据起始位置
         * @return 本次解析结果
         */
        public byte[] update(byte[] src, int offset) {
            int len = src.length - offset;
            byte[] result = new byte[outLength(src, offset, len, false)];
            update0(src, offset, len, result, 0, false);
            return result;
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @param offset
         *            源数据起始位置
         * @param len
         *            长度
         * @return 本次解析结果
         */
        public byte[] update(byte[] src, int offset, int len) {
            byte[] result = new byte[outLength(src, offset, len, false)];
            update0(src, offset, len, result, 0, false);
            return result;
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @param offset
         *            源数据起始位置
         * @param len
         *            长度
         * @param dst
         *            目标数组，需要可以完整存储源数据
         * @return 本次解析长度
         */
        public int update(byte[] src, int offset, int len, byte[] dst) {
            int outLen = outLength(src, offset, len, false);
            Assert.assertTrue(dst.length >= outLen, "dst len < outLen",
                ExceptionProviderConst.IllegalArgumentExceptionProvider);
            return update0(src, offset, len, dst, 0, false);
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @param offset
         *            源数据起始位置
         * @param len
         *            长度
         * @param dst
         *            目标数组，需要可以完整存储源数据
         * @param dstOffset
         *            结果写入起始位置
         * @return 本次解析长度
         */
        public int update(byte[] src, int offset, int len, byte[] dst, int dstOffset) {
            int outLen = outLength(src, offset, len, false);
            Assert.assertTrue(dst.length - dstOffset >= outLen, "dst len - dstOffset < outLen",
                ExceptionProviderConst.IllegalArgumentExceptionProvider);
            return update0(src, offset, len, dst, dstOffset, false);
        }

        /**
         * 计算outLen
         *
         * @param src
         *            src
         * @param offset
         *            offset
         * @param len
         *            len
         * @return outLen
         */
        public int outLength(byte[] src, int offset, int len) {
            return outLength(src, offset, len, false);
        }

        /**
         * 计算outLen
         *
         * @param src
         *            src
         * @param offset
         *            offset
         * @param len
         *            len
         * @param end
         *            是否结束
         * @return outLen
         */
        public int outLength(byte[] src, int offset, int len, boolean end) {
            int[] base64 = isURL ? fromBase64URL : fromBase64;
            int paddings = 0;
            int endOffset = offset + len;
            int dataLen = len + legacyLen;
            if (dataLen == 0) {
                return 0;
            }

            if (dataLen < 2) {
                return 0;
            }

            if (isMIME) {
                // scan all bytes to fill out all non-alphabet. a performance
                // trade-off of pre-scan or Arrays.copyOf
                int n = 0;
                boolean finish = false;
                if (legacyLen > 0) {
                    int legacyOffset = 0;
                    while (legacyOffset < legacyLen) {
                        int b = legacy[legacyOffset++] & 0xff;
                        if (b == '=') {
                            dataLen = legacyOffset - 1;
                            finish = true;
                            break;
                        }

                        if (base64[b] == -1) {
                            n++;
                        }
                    }
                }

                if (!finish) {
                    int i = offset;
                    while (i < endOffset) {
                        int b = src[i++] & 0xff;
                        if (b == '=') {
                            dataLen -= (endOffset - i + 1);
                            break;
                        }
                        if (base64[b] == -1) {
                            n++;
                        }
                    }
                    dataLen -= n;

                }
            } else {
                if (len > 1) {
                    if (src[endOffset - 1] == '=') {
                        paddings++;
                        if (src[endOffset - 2] == '=') {
                            paddings++;
                        }
                    }
                } else if (len == 1) {
                    if (src[0] == '=') {
                        paddings++;
                        if (legacy[legacyLen - 1] == '=') {
                            paddings++;
                        }
                    }
                } else {
                    if (legacy[legacyLen - 1] == '=') {
                        paddings++;
                        if (legacy[legacyLen - 2] == '=') {
                            paddings++;
                        }
                    }
                }
            }

            if (paddings == 0 && (dataLen & 0x3) != 0 && end) {
                // 应该有padding的，但是实际没有padding，我们主动将padding长度补充上
                paddings = 4 - (dataLen & 0x3);
                dataLen += paddings;
            }

            return 3 * (dataLen / 4) - paddings;
        }

        /**
         * base64解析
         *
         * @param src
         *            源数据
         * @param offset
         *            源数据起始位置
         * @param len
         *            长度
         * @param dst
         *            目标数组，需要可以完整存储源数据
         * @param dstOffset
         *            结果写入起始位置
         * @return 本次解析长度
         */
        private int update0(byte[] src, int offset, int len, byte[] dst, int dstOffset, boolean end) {
            int[] base64 = isURL ? fromBase64URL : fromBase64;

            int srcOffset = offset;
            int endOffset = len + offset;

            if (finish) {
                while (srcOffset < endOffset) {
                    this.offset++;
                    if (isMIME && base64[src[srcOffset++]] < 0) {
                        continue;
                    }
                    throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + this.offset);
                }
                return 0;
            } else if (len + legacyLen < 4 && !end) {
                System.arraycopy(src, offset, legacy, legacyLen, len);
                legacyLen += len;
                return 0;
            }

            int dp = dstOffset;
            int bits = 0;
            // pos of first byte of 4-byte atom
            int shiftto = 18;
            boolean finishFlag = end;

            // 先消费遗留数据
            if (legacyLen > 0) {
                int legacyOffset = 0;
                while (legacyOffset < legacyLen) {
                    int b = legacy[legacyOffset++] & 0xff;
                    if ((b = base64[b]) < 0) {
                        if (b == -2) {
                            // padding byte '='
                            // = shiftto==18 unnecessary padding
                            // x= shiftto==12 a dangling single x
                            // x to be handled together with non-padding case
                            // xx= shiftto==6&&sp==sl missing last =
                            // xx=y shiftto==6 last is not =
                            if (shiftto == 6 && ((legacyOffset == legacyLen && len == 0)
                                || (legacyOffset != legacyLen && legacy[legacyOffset++] != '=')
                                || (len != 0 && src[srcOffset++] != '=')) || shiftto == 18) {
                                // 如果当前遗留数据已经到头或者遗留数据的下一位不是= 或者是 遗留数据已经到头了，并且新数据长度为0或者新数据开头不是=
                                throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                            }

                            finishFlag = true;
                            break;
                        }
                        // skip if for rfc2045
                        if (isMIME) {
                            continue;
                        } else {
                            throw new IllegalArgumentException(
                                "Illegal base64 character " + Integer.toString(src[legacyOffset - 1], 16));
                        }
                    }
                    bits |= (b << shiftto);
                    shiftto -= 6;
                }

                this.offset += legacyOffset;

                if (finishFlag) {
                    // reached end of byte array or hit padding '=' characters.
                    if (shiftto == 6) {
                        dst[dp++] = (byte)(bits >> 16);
                    } else if (shiftto == 0) {
                        dst[dp++] = (byte)(bits >> 16);
                        dst[dp++] = (byte)(bits >> 8);
                    } else if (shiftto == 12) {
                        // dangling single "x", incorrectly encoded.
                        throw new IllegalArgumentException("Last unit does not have enough valid bits");
                    }

                    while (legacyOffset < legacyLen) {
                        this.offset++;
                        if (isMIME && base64[src[legacyOffset++]] < 0) {
                            continue;
                        }
                        throw new IllegalArgumentException(
                            "Input byte array has incorrect ending byte at " + this.offset);
                    }

                    while (srcOffset < endOffset) {
                        this.offset++;
                        if (isMIME && base64[src[srcOffset++]] < 0) {
                            continue;
                        }
                        throw new IllegalArgumentException(
                            "Input byte array has incorrect ending byte at " + this.offset);
                    }

                    legacyLen = 0;
                    return dp - dstOffset;
                }
            }

            /*
             * 如果目标数组和原数组是同一个，因为我们有遗留数据，所以在写入原数组的时候可能会将原数组中未消费的数据覆盖掉，例如遗留数组中有3个遗留数据，那么只需要从新数组中
             * 再读取1个就能解析出3个byte（base64最终就是4个byte解析为3个byte），此时新数组我们只读取了1个byte，但是却要写入3个，会导致有2个byte未消费数据被覆盖，所以我们
             * 需要通过一定的算法来解决：
             *
             * 当我们要读取1个byte，写入3个byte时，我们需要原数组中2byte的额外空间，因为base64解析时会将4byte数据解析为3byte结果，也就是每组4byte的数据就能为我们剩余1byte
             * 的额外空间，此时当我们需要2byte的额外空间时（遗留数据有3byte），我们只需要先读取1byte数据，再额外读取2组共计8byte数据，此时前9byte数据已经消费过了，而此时的
             * 结果也能刚好是9byte（3byte的遗留数据+读取的9byte数据，原文大小12byte，解析结果刚好是9byte），此时将结果写入原数组，就不会覆盖未消费数据了
             */
            int tempLen = src == dst ? Math.max(legacyLen - 1, 0) : 0;
            tempLen = tempLen == 0 ? 0 : tempLen + 1;
            tempLen = tempLen * 3;
            byte[] temp = new byte[tempLen];
            int tempWriteIndex = 0;
            dp = dp + tempLen;

            // 重新计算
            if (!end) {
                legacyLen = (legacyLen + len) & 0x3;
                if (legacyLen > 0) {
                    int newLen = len - legacyLen;
                    System.arraycopy(src, srcOffset + newLen, legacy, 0, legacyLen);
                    endOffset = offset + newLen;
                }
            }

            while (srcOffset < endOffset) {
                int b = src[srcOffset++] & 0xff;
                if ((b = base64[b]) < 0) {
                    if (b == -2) {
                        // padding byte '='
                        // = shiftto==18 unnecessary padding
                        // x= shiftto==12 a dangling single x
                        // x to be handled together with non-padding case
                        // xx= shiftto==6&&sp==sl missing last =
                        // xx=y shiftto==6 last is not =
                        if (shiftto == 6 && (srcOffset == endOffset || src[srcOffset++] != '=') || shiftto == 18) {
                            throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                        }
                        finishFlag = true;
                        break;
                    }
                    // skip if for rfc2045
                    if (isMIME) {
                        continue;
                    } else {
                        throw new IllegalArgumentException(
                            "Illegal base64 character " + Integer.toString(src[srcOffset - 1], 16));
                    }
                }
                bits |= (b << shiftto);
                shiftto -= 6;
                if (shiftto < 0) {
                    if (tempWriteIndex < tempLen) {
                        temp[tempWriteIndex++] = (byte)(bits >> 16);
                        temp[tempWriteIndex++] = (byte)(bits >> 8);
                        temp[tempWriteIndex++] = (byte)(bits);
                    } else {
                        dst[dp++] = (byte)(bits >> 16);
                        dst[dp++] = (byte)(bits >> 8);
                        dst[dp++] = (byte)(bits);
                    }
                    shiftto = 18;
                    bits = 0;
                }
            }

            if (tempLen > 0) {
                System.arraycopy(temp, 0, dst, dstOffset, tempWriteIndex);
                // 如果temp放的正好是最后一组数据，同时数据最后包含=，那之前的dp计算实际是有问题的
                dp -= (tempLen - tempWriteIndex);
            }

            // reached end of byte array or hit padding '=' characters.
            if (shiftto == 6) {
                dst[dp++] = (byte)(bits >> 16);
            } else if (shiftto == 0) {
                dst[dp++] = (byte)(bits >> 16);
                dst[dp++] = (byte)(bits >> 8);
            } else if (shiftto == 12 && finishFlag) {
                // dangling single "x", incorrectly encoded.
                throw new IllegalArgumentException("Last unit does not have enough valid bits");
            }

            this.offset += srcOffset;

            // 把遗留数据也处理完
            if (finishFlag && legacyLen > 0) {
                int o = 0;
                while (o < legacyLen) {
                    this.offset++;
                    if (isMIME && base64[legacy[o++]] < 0) {
                        continue;
                    }
                    throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + this.offset);
                }
            }

            // anything left is invalid, if is not MIME.
            // if MIME, ignore all non-base64 character
            while (srcOffset < endOffset) {
                this.offset++;
                if (isMIME && base64[src[srcOffset++]] < 0) {
                    continue;
                }
                throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + this.offset);
            }

            return dp - dstOffset;
        }

    }
}
