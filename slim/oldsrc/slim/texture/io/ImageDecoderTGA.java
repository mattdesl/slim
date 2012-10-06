/*
 * Copyright (c) 2008-2012, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package slim.texture.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import slim.texture.Texture;

/**
 *
 * @author Matthias Mann
 */
public class ImageDecoderTGA extends ImageDecoder {

    private byte[] header;
    private int[] palette;
    private boolean flipY;
    private int startPos;

    public ImageDecoderTGA(URL url) {
        super(url);
    }

    @Override
    public boolean open() throws IOException {
        inputStream = url.openStream();
        header = new byte[18];
        readFully(header);
        skipFully(hdrByte(0)); // skip image ID
        switch(hdrByte(1)) {
            case 0:
                break;
            case 1:
                if(hdrWord(3) != 0) {
                    throw new IOException("Invalid palette begin");
                }
                palette = decodePalette(hdrWord(5), hdrByte(7));
                break;
            default:
                throw new IOException("Unknow palette mode");
        }
        switch(hdrByte(2)) {
            case 1:
                if(palette == null) {
                    throw new IOException("Palette require for index image");
                }
                if(hdrByte(16) != 8) {
                    throw new IOException("Only 8 bit indexed images supported");
                }
                format = Texture.Format.BGRA;
                break;
            case 2:
                if(hdrByte(16) != 24 && hdrByte(16) != 32) {
                    throw new IOException("Unsupported bits per pixel");
                }
                format = Texture.Format.BGRA;
                break;
            case 3:
                if(hdrByte(16) != 8) {
                    throw new IOException("Only 8 bit monochrome images supported");
                }
                format = Texture.Format.LUMINANCE;
                break;
            case 10:
                if(hdrByte(16) != 24 && hdrByte(16) != 32) {
                    throw new IOException("Unsupported bits per pixel");
                }
                format = Texture.Format.BGRA;
                break;
            default:
                throw new IOException("Unsupported format: " + hdrByte(2));
        }
        width = hdrWord(12);
        height = hdrWord(14);
        if((hdrByte(17) & (1 << 4)) != 0) {
            throw new IOException("Unsupported X origin");
        }
        flipY = (hdrByte(17) & (1 << 5)) == 0;
        return true;
    }

    @Override
    public void decode(ByteBuffer bb) throws IOException {
        startPos = bb.position();
        switch(hdrByte(2)) {
            case 1:
                decodePAL(bb);
                break;
            case 2:
                if(hdrByte(16) == 24) {
                    decode24(bb);
                } else {
                    decodeSimple(bb);
                }
                break;
            case 3:
                decodeSimple(bb);
                break;
            case 10:
                decodeRLE(bb, hdrByte(16) == 32);
                break;
            default:
                throw new AssertionError();
        }
        bb.position(startPos + height * width * format.getBytesPerPixel());
    }

    private int hdrByte(int idx) {
        return header[idx] & 255;
    }

    private int hdrWord(int idx) {
        return (hdrByte(idx + 1) << 8) | hdrByte(idx);
    }

    private int[] decodePalette(int entries, int bitsPerEntry) throws IOException {
        int[] result = new int[entries];
        byte[] tmp;
        switch(bitsPerEntry) {
            case 24:
                tmp = new byte[entries * 3];
                readFully(tmp);
                for(int i = 0; i < entries; i++) {
                    result[i] = 0xFF000000 |
                            ((tmp[i * 3 + 2] & 255) << 16) |
                            ((tmp[i * 3 + 1] & 255) <<  8) |
                            ((tmp[i * 3    ] & 255)      );
                }
                return result;
            case 32:
                tmp = new byte[entries * 4];
                readFully(tmp);
                for(int i = 0; i < entries; i++) {
                    result[i] =
                            ((tmp[i * 3 + 3] & 255) << 24) |
                            ((tmp[i * 3 + 2] & 255) << 16) |
                            ((tmp[i * 3 + 1] & 255) <<  8) |
                            ((tmp[i * 3    ] & 255)      );
                }
                return result;
            default:
                throw new IOException("Unsupported bits per palette entry");
        }
    }

    private int pixelOffset(int y) {
        if(flipY) {
            y = height - y - 1;
        }
        return y * width;
    }

    private void setPos(ByteBuffer buf, int y) {
        buf.position(startPos + pixelOffset(y) * format.getBytesPerPixel());
    }

    private void decodeSimple(ByteBuffer bb) throws IOException {
        byte[] tmp = new byte[width * format.getBytesPerPixel()];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            bb.put(tmp);
        }
    }

    private void decode24(ByteBuffer bb) throws IOException {
        byte[] tmp = new byte[width * 3];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            for(int x = 0, w = width * 3; x < w; x += 3) {
                bb.put(tmp[x]).put(tmp[x + 1]).put(tmp[x + 2]).put((byte)255);
            }
        }
    }

    private void decodeRLE(ByteBuffer bb, boolean bgra) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            for(int x = 0, w = width; x < w;) {
                int ctrl = bis.read();
                x += (ctrl & 127) + 1;
                if((ctrl & 128) != 0) {
                    byte b = (byte)bis.read();
                    byte g = (byte)bis.read();
                    byte r = (byte)bis.read();
                    byte a = (byte)(bgra ? bis.read() : 255);
                    ctrl &= 127;
                    do {
                        bb.put(b).put(g).put(r).put(a);
                    } while(ctrl-- > 0);
                } else {
                    ctrl &= 127;
                    do {
                        byte b = (byte)bis.read();
                        byte g = (byte)bis.read();
                        byte r = (byte)bis.read();
                        byte a = (byte)(bgra ? bis.read() : 255);
                        bb.put(b).put(g).put(r).put(a);
                    } while(ctrl-- > 0);
                }
            }
        }
    }

    private void decodePAL(ByteBuffer bb) throws IOException {
        IntBuffer ib = bb.order(ByteOrder.LITTLE_ENDIAN).slice().asIntBuffer();
        byte[] tmp = new byte[width];
        int[] pal = this.palette;
        for(int y = 0; y < height; y++) {
            ib.position(pixelOffset(y));
            readFully(tmp);
            for(int x = 0, w = width; x < w; x++) {
                ib.put(pal[tmp[x]]);
            }
        }
    }
}
