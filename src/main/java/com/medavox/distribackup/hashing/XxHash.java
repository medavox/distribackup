package com.medavox.distribackup.hashing;


import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.ByteBuffer;

/**fast, good-quality non-cryptographic hash
 * http://cyan4973.github.io/xxHash/*/
class XxHash extends HashingWrapper {
    private final StreamingXXHash64 hasher;
    private final XXHashFactory factory;
    public XxHash() {
        factory = XXHashFactory.safeInstance();
        hasher = factory.newStreamingHash64(0);
    }
    @Override
    public void addBytes(byte[] data) {
        hasher.update(data, 0, data.length);
    }

    @Override
    public byte[] computeHash() {
        long hash = hasher.getValue();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(hash);
        return buffer.array();
    }

    @Override
    public void reset() {
        hasher.reset();
    }
}
