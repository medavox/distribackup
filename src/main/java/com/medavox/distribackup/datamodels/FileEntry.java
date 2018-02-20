package com.medavox.distribackup.datamodels;

public final class FileEntry {
    //todo: make these types more specific (eg a File/Path type instead of String),
    //and less prone to invalidity

    /**'full' path is relative to repository root.*/
    public final String fullPathAndFileName;

    /**Unix-style three-digit octal permissions*/
    public final int octalFilePermissions;

    public final Hash hashOfFileContents;

    public FileEntry(String filePathName, int permissions, Hash hash) {
        fullPathAndFileName = filePathName;
        octalFilePermissions = permissions;
        hashOfFileContents = hash;
    }
}

