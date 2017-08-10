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

Right, so instead:

We have a content-addressable filesystem, just like Git's with blobs in. 
The blobs are comprised of HashChunks and their hashes.
They can also be diff, with a reference to another HashChunk. This also has its own SHA1 hash/address.
diffs can point to other diffs. Creating the diff-chain structure again, as described above.
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
