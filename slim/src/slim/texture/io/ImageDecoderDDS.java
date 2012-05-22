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
import slim.texture.Texture2D;
import slim.texture.io.PNGDecoder.Format;

/**
 *
 * @author davedes
 */
public class ImageDecoderDDS extends ImageDecoder {
    
    private DDSDecoder decoder;
    
    public ImageDecoderDDS(URL url) {
        super(url);
    }

    @Override
    public boolean open() throws IOException {
        inputStream = url.openStream();
        decoder = new DDSDecoder(inputStream);
        width = decoder.getWidth();
        height = decoder.getHeight();
        format = toTextureFormat(decoder.getFormat());
        return true;
    }
    
    private Texture.Format toTextureFormat(DDSDecoder.Format fmt) {
    	switch (fmt) {
        case LUMINANCE: return Texture.Format.LUMINANCE;
        case LUMINANCE_ALPHA: return Texture.Format.LUMINANCE_ALPHA;
        case RGB: return Texture.Format.RGB;
        case RGB_DXT1: return Texture.Format.COMPRESSED_RGB_DXT1;
        case RGBA_DXT1: return Texture.Format.COMPRESSED_RGBA_DXT1;
        case RGBA_DXT3: return Texture.Format.COMPRESSED_RGBA_DXT3;
        case RGBA_DXT5: return Texture.Format.COMPRESSED_RGBA_DXT5;
        default: throw new UnsupportedOperationException("texture format "+fmt+" not supported");
        }
    }

    @Override
    public void decode(ByteBuffer bb) throws IOException {
        decoder.decode(bb);
    }
}
