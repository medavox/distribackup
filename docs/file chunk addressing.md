Rationale
=========

Large files (>1GB) in a collection shared over a distributed network (such as ours, or bitorrent) 
benefit from larger chunk sizes, to reduce the number of hashes needed to address all the bytes in the file.

Smaller files (<1GB), however, benefit from smaller chunk sizes, to allow us to send less data when a chunk has changed.

These two requirements are at odds with each other; a sensible halfway-point could be found, 
if we knew what kind of data we were sharing in advance.

BitTorrent sets the chunk size per-torrent, proportional to the size of the files in the torrent.

Unfortunately, unlike BitTorrent we don't have the luxury of setting the chunk size statically, 
because **the size of our files may change over time**.

Therefore we need a chunk-splitting algorithm which allows files to grow and shrink in size,
without:-

a. producing an (unmanagably large | computationally expensive) 
number of hashes & small chunks for large files in the archive,
(eg ~30 million SHA1 hashing operations on all the 4K pieces of a 120GB file)
or
b. being forced to send the entirety of smaller files every time a small change is made to them,
because the chunk size has been set to a large size, to accommodate the aforementioned larger files
(eg sending the whole of ~40MB files every time they're modified, in an archive with 10GB files in)

We need a chunk size which is always small enough to fit in system memory during transfer, 
even for resource-constrained devices and environments (Raspberry Pis and mobile devices).


Consistently Mapping Chunks to Variable Length Files
=====

chunksize (default: 256KB) is defined by the amount of data the slowest upload rate
(in the likely network context: 4G, dialup, broadband, fibre, LAN, Gigabit) can send in 1 second.

It is immutable for each archive collection.

Each file entry consists of the following:

* path and file name
* size
* simple (scalable algo) whole-file hash
* one hash for each chunk we know the file has, from the above algorithm

Proposed Algorithm
======

* first 256 HashChunks are 256KB in size, addressing files up to 64MB
* next 192 HashChunks are 1MB in size, addressing files up to 256MB
* next 192 HashChunks are 4MB in size, addressing files up to 1GB
* next 192 HashChunks are 16MB in size, addressing files up to 4GB
* next 192 HashChunks are 64MB in size, addressing files up to 16GB
* next 192 HashChunks are 256MB in size, addressing files up to 64GB
* next 192 HashChunks are 1GB in size, addressing files up to 256GB
* ...
* next 192 HashChunks are `4*n` in size, addressing files up to `4*prevsize`

file size boundaries for HashChunk size = 2^26, 2^28, 2^30...  
HashChunk sizes = 2^18, 2^20, 2^22...  
number of HashChunks = 256, 192, 192, 192, 192...  

start with 2^18 byte size  
then 2^20 size  

to work out the number of HashChunks for a file of size 112GB,

divide file size 112GB by 256 = 448MB

work out highest chunk boundary
int x = number of times you have to shift right, in order to make your number = 1
divide this by 2, then multiply by 2, to get the highest even power of 2 in the original number
the highest even power of 2 = our last chunk size  boundary

The last HashChunk of a file will not usually be the full size.

Algorithm Weakness
-----------------

Small changes made to the end of a large file will incur a large penalty: a large-size chunk will need to be re-sent.
Perhaps a sub-chunk should be used, where large-size HashChunks are split into further hashed chunks, 
to work out which part(s) of it have changed.

This is still better than keeping all HashChunks at a constant size (generating lots of smallish HashChunks for large files),
because we only have to hash a subset of the large file (the modified HashChunk).

Hashchunks and Memchunks
------

for network and memory reasons, we should also use memchunks:
maxChunksSize of 256MB or 1GB keeps the number of hashes for large files down to a sensible number,
but it's too much data to keep in RAM at one time, or to send down the network as one piece (probably)
