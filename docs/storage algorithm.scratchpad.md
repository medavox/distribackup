Attempt 1
=========

the publisher always has the current and all previous versions of the Archive,
so it also knows the diffs from all previous versions to the current one.
therefore, we don't need to use anything like the rsync algorithm for minimal, differential file transfers

using a content-addressable database like git's objects database means we often duplicate bytes: 1 copy for the live, normal files, 1 copy for the objects database.
Instead, can we use the live, normal file as the 'basis' file, and store diffs of that, which when applied, produce older versions?


so like this:-

Normal File, accessible on disk		(version 3)
	|
	V
diff to version 2 ---> version 2 when applied
	|
	V
diff from version 2 to version 1 ---> version 1 when applied

we store this chain of diffs, and when we want to move between versions,  
we apply or un-apply them successively (the diffs contain enough info to be reversible)


because the live file is also our datastore,
we need to know when it changes, and how, so we can create a new diff from the changes to the new one. Shit.

OK, so we DO always need a second copy on-disk of the data, to compare against for changes.

Unless we can use the archive copy(s) on the network as the data store? but how then would we record versioning?
No. each node must be a standalone copy of the entire archive, and all its previous versions.

-----

Attempt 2
=========

Right, so instead:

We have a content-addressable filesystem, just like Git's, with blobs in. 
The blobs are comprised of HashChunks and their hashes.
They can also be diff, with a reference to another HashChunk. This also has its own SHA1 hash/address.
Diffs can point to other diffs, creating the diff-chain structure again, as described above.
Versions of the archive filesystem are described by a version number, and something like Git's tree structure:

one or more FileMetaData objects, and recursive references to other trees

a FileMetaData object consists of the following
filename
a list of blobs or diffs, in order, which comprise the file contents

WAIT!
====
if HashChunks are reordered in a modification, that could mean that larger HashChunks could appear before smaller ones!
part of the constraint for increasing-size hashchunks is that their size increases predictably, 
and are always calculated the same, given the same file size. The reordering ability breaks this.


BTW: We _DO_ need the archives to support merging, in the event of a network partition. 
The two halves of the network could each re-elect a different Publisher
(or the half without the original Publisher could elect a new one),
and move forward with completely different changes to the archive.
If/when the network is healed, the diverged histories will need to be MERGED.


Git stores every version of every file as a whole, only shrinking them when added to packfiles.
Custom packfiles are also created to transfer only the differences between repos; but these are generated specially each time,
	to transfer the minimum data across the network.

We, however, always know the exact difference between archive versions.
We  don't need to produce unique packfiles for every inter-node transfer (hopefully; barring local damage to an archive).
However, we could do better than a single packfile per archive version increment.
Each file diff, so individual diffs can be transferred from different nodes

In order to avoid wasting CPU cycles on re-calculating diffs on the same content,
i don't want to store packed copies of whole files on disk, then compress them after the fact.

Instead of what git does, how about this:

Only the most recent version of a file in the commit history is whole;
every older version is only stored as a diff from the next newer version. Diffs going back.
These diffs should be the same diffs as were sent to update a local archive, reusing the calculations.
This means the diff format must be reversible.

Recap
=====

So, to recap intended features so far:

* Each node has a full version history, implemented slightly differently than git
	- the metadata contains a special copy of each file in the repo: how the file looked, as of the last commit
	- only the last commit to change file contains a full version of it; every modifying commit before it merely stores the diff
	between that version and the newer.
* Needs the ability to merge diverged network histories in the event of a healed network partition
	- Should only be rarely necessary, so doesn't need to be 'cheap'. But must be possible. And not too difficult for humans
* So only the Publisher can modify the files, but every subscriber has access to the full modification history
* No index/staging area
; all files in a repository are committed upon modification (after a cooldown)
* No branches; there aren't supposed to be multiple concurrent variants of the archive. Multiple concurrent
* 99% of the time, the modification history is therefore almost always completely linear.
* Our diff/delta format must be completely reversible, if any existing one is not already
* The commit history graph must be highly resistant to tampering, probably using signed keys to guarantee that 
	1. the commit was authored by the publisher 
	2. at the time stated.
	-merkle hashes?

Should we still use chunking???

---------

Observation: because of the way Git stores objects on disk, it gets the following ability extra: objects which are the same in different files can be reused, only storing the data once. So if two files' contents are identical, the data is only stored once.

We can't do that, because every object is stored as its current version, and a chain of diffs going back into the past

Solution: store the SHA-1 hash of the whole file with the diff, not just the hash of the diff.

-----

We don't need Chunks That Change anymore; ie chunks will not be used as a mechanism for sharing updates.
Chunks will now only be used to share basefiles.
New peers asking for full copies of all files (with no old data) will receive different chunks of the same version of the same file from different peers.
Sharing changes to these basefiles will be done by sharing the Deltas, and the accompanying trees and commits.

It would still be even more efficient to store the basefiles with some sort of similarity checking between them: deduplicating similar files within the archive. Bup's algorithm can help with this  (if I can understand it)

Also, in the case that the Publisher is authoring content which Subscribers wait for the next, finished version of (eg a blog or a vlog), automatic commits would not be a good idea. The Publisher should then instead be able to commit when they want.
