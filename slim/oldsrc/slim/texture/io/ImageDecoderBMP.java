/*
 * Copyright (c) 2008-2011, Matthias Mann
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

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import slim.texture.Texture;

/**
 *
 * @author Matthias Mann
 */
public class ImageDecoderBMP extends ImageDecoder {

    private static final int MIN_HDR_SIZE = 14+12;
    
    private byte[] header;
    private int bpp;
    private int startPos;
    private boolean flipY;

    public ImageDecoderBMP(URL url) {
        super(url);
    }
    
    @Override
    public boolean open() throws IOException {
        inputStream = url.openStream();
        header = new byte[14+124];
        readFully(header, 0, MIN_HDR_SIZE);

        if(hdrByte(0) != 0x42 || hdrByte(1) != 0x4D) {
            return false;
        }

        int imageStart = hdrDWord(0x0A);
        int hdrSize = hdrWord(0xE);

        if(imageStart < hdrSize) {
            return false;
        }
        
        readFully(header, MIN_HDR_SIZE, hdrSize-MIN_HDR_SIZE);

        switch(hdrSize) {
            case 12:
                if(hdrWord(0x16) != 1) {
                    // number of color planes
                    return false;
                }

                bpp = hdrWord(0x18);
                width = hdrWord(0x12);
                height = hdrWord(0x14);
                break;

            case 40:
                if(hdrWord(0x1A) != 1) {
                    // number of color planes
                    return false;
                }

                if(hdrWord(0x1E) != 0) {
                    // compression
                    return false;
                }

                bpp = hdrWord(0x1C);
                width = hdrDWord(0x12);
                height = hdrDWord(0x16);

                if(height < 0) {
                    flipY = false;
                    height = -height;
                } else {
                    flipY = true;
                }
                break;

            default:
                return false;
        }

        if(width <= 0 || height <= 0) {
            return false;
        }
        
        if(bpp != 24 && bpp != 32) {
            return false;
        }

        format = Texture.Format.BGRA;

        skipFully(imageStart - hdrSize);
        return true;
    }

    @Override
    public void decode(ByteBuffer bb) throws IOException {
        startPos = bb.position();
        switch(bpp) {
            case 24:
                decode24(bb);
                break;
            case 32:
                decodeSimple(bb);
                break;
            default:
                throw new AssertionError();
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

    private static int align4(int x) {
        return (x + 3) & ~3;
    }

    private void decodeSimple(ByteBuffer bb) throws IOException {
        int lineLen = width * format.getBytesPerPixel();
        byte[] tmp = new byte[align4(lineLen)];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            bb.put(tmp, 0, lineLen);
        }
    }

    private void decode24(ByteBuffer bb) throws IOException {
        byte[] tmp = new byte[align4(width * 3)];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            for(int x = 0, w = width * 3; x < w; x += 3) {
                bb.put(tmp[x]).put(tmp[x + 1]).put(tmp[x + 2]).put((byte)255);
            }
        }
    }

    private int hdrByte(int idx) {
        return header[idx] & 255;
    }

    private int hdrWord(int idx) {
        return (hdrByte(idx + 1) << 8) | hdrByte(idx);
    }

    private int hdrDWord(int idx) {
        return (hdrWord(idx + 2) << 16) | hdrWord(idx);
    }
}
