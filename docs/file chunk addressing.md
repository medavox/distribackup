Consistently Mapping Chunks to Variable Length Files
=====

- files <= 256KB in length are a single chunk
- files <= 1GB are split into 256KB chunks
- for larger files, the first 1GB is still split into 256KB chunks
- then chunks after that are 1MB in size

chunksize (default: 256KB) is defined by the amount of data the slowest upload rate (in the likely network context: 4G, dialup, broadband, fibre, LAN, Gigabit) can send in 1 second.

It is static for each archive collection.

Each file entry consists of the following:

* path and file name
* size
* simple (scalable algo) whole-file hash
* hash of each chunk we know the file has, from the above algorithm
