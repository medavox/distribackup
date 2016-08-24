Consistently Mapping Chunks to Variable Length Files
=====

- files <= 256KB in length are a single chunk
- files <= 1GB are split into 256KB chunks
- for larger files, the first 1GB is still split into 256KB chunks
- then chunks after that are 1MB in size

chunksize (default: 256KB) is defined by the amount of data the slowest upload rate (in the likely network context: 4G, dialup, broadband, fibre, LAN, Gigabit) can send in 1 second.

It is immutable for each archive collection.

Each file entry consists of the following:

* path and file name
* size
* simple (scalable algo) whole-file hash
* one hash for each chunk we know the file has, from the above algorithm

how's this for an algorithm:

* first 256 chunks are 256KB in size, totalling 64MB
* next 192 chunks are 1MB in size, now totalling 256MB
* next 192 chunks are 4MB in size, now totalling 1GB
* next 192 chunks are 16MB in size, now totalling 4GB
* next 192 chunks are 64MB in size, now totalling 16GB
* next 192 chunks are 256MB in size, now totalling 64GB
* next 192 chunks are 1GB in size, now totalling 256GB
* ...
* next 192 chunks are 4*n in size, totalling 4*prevsize

file size boundaries for chunk size = 2^26, 2^28, 2^30...
chunk sizes = 2^18, 2^20, 2^22...
number of chunks = 256, 192, 192, 192, 192...

.-=
--=
===

start with 2^18 byte size
then 2^20 size

to work out number of chunks for file of size 112GB,

divide file size 112GB by 256 = 448MB

work out highest chunk boundary
int x = number of times you have to shift right, in order to make your number = 1
divide this by 2, then multiply by 2, to get the highest even power of 2 in the original number
the highest even power of 2 = our last chunk size  boundary


for network and memory reasons, we should also use sub-chunks:
maxChunksSize of 256MB or 1GB keeps the number of hashes for large files down to a sensible number,
but it's too much data to keep in RAM at one time, or to send down the network as one piece (probably)
