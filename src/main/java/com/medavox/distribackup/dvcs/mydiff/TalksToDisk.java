package com.medavox.distribackup.dvcs.mydiff;

import java.io.File;

public interface TalksToDisk {
    OffsetMatchingSequence compareWithOffset(File a, File b, long bOffsetFromA);
}
