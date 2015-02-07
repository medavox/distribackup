package com.medavox.distribackup.connections;

import com.medavox.distribackup.filesystem.FileInfo;

/**A Wrapper class around a FileInfo[], allowing it to be used as a Communicable.*/
public class FileInfoListWrapper implements Communicable
{
    private FileInfo[] fi;
    public FileInfoListWrapper(FileInfo[] fi)
    {
        this.fi = fi;
    }
    public FileInfo[] getFileInfoList()
    {
        return fi;
    }
}
