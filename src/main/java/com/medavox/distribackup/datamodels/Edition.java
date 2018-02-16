package com.medavox.distribackup.datamodels;

import org.jetbrains.annotations.Nullable;

/**represents a snapshot of the state of the archive. A version.*/
public class Edition {

    /**The hash of the previous commit.
     * Usually commits have a single parent, but may have more due to a merge*/
    public final Hash[] parentCommits;

/**- Zero or more hashes of File Entries -- the archive's state at this commit*/
    public final Hash[] allFilesInArchiveAtTimeOfCommitCreation;

    public Edition(@Nullable Hash parent, Hash... files) {
        allFilesInArchiveAtTimeOfCommitCreation = files;
        parentCommits = new Hash[]{parent};
    }

    public Edition(Hash[] parents, Hash... files) {
        allFilesInArchiveAtTimeOfCommitCreation = files;
        parentCommits = parents;
    }
}
