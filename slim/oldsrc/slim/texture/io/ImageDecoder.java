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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import slim.texture.Texture;

/**
 * Async texture loader base class
 * 
 * @author Matthias Mann
 * @author davedes (some modifications)
 */
public abstract class ImageDecoder {
    
    protected final URL url;
    protected InputStream inputStream;
    protected int width;
    protected int height;
    protected Texture.Format format;

    protected ImageDecoder(URL url) {
        this.url = url;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }
    
    public int getSize() {
    	return getFormat().getBytesPerPixel() * getWidth() * getHeight();
    }

    public Texture.Format getFormat() {
        return format;
    }
    
    public int getMipMapCount() {
    	return 0;
    }

    public abstract boolean open() throws IOException;
    public abstract void decode(ByteBuffer bb) throws IOException;

    public void close() {
        if(inputStream != null) {
            try {
                inputStream.close();
            } catch(IOException ex) {
                getLogger().log(Level.SEVERE, "Can't close input stream", ex);
            }
            inputStream = null;
        }
    }

    protected final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    protected final void readFully(byte[] b, int off, int len) throws IOException {
        while(len > 0) {
            int read = inputStream.read(b, off, len);
            if(read <= 0) {
                throw new EOFException();
            }
            off += read;
            len -= read;
        }
    }

    protected final void skipFully(int amount) throws IOException {
        while(amount > 0) {
            int skipped = (int)inputStream.skip(amount);
            if(skipped <= 0) {
                throw new EOFException();
            }
            amount -= skipped;
        }
    }
    
    protected static Logger getLogger() {
        return Logger.getLogger(ImageDecoder.class.getName());
    }
}
