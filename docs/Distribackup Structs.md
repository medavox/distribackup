Distribackup Storage Objects
===================

These are all stored as a file named with its hash.
Except for Deltas, which are stored under the hash of the full file contents they construct.

Edition (formerly edition)
------
- hash of the parent edition(s?)
- Zero or more hashes of File Entries -- the archive's state at this edition

File Entry
----------
- Full path and file name
- hash of the full file contents (which is a reference to the delta)
- Unix-style three-digit octal permissions (not reduced like git; but no ownership data)

Delta
-----
- the hash of the previous file version (parent version)
- the delta from the previous version. Apply this patch to the next newer version to get the whole file
- a pointer to the file to apply this patch to

Archive State
-------------

A full copy of the archive state, as of the last edition.

All the files are separate and independent from the working directory (which users interact with), so that:

1. Publishers can make changes to the archive, and Distribackup can detect *what* has been changed, and
2. Distribackup can easily replace files which Subscribers erroneously modify/delete.

The Archive State will probably be stored in a compressed file
(in a compression format supporting random file access without full extraction), to save disk space.

Basefile
-------
A full copy of a file in the archive, as it was at the most recent edition.
Its hash is the hash used to store the latest delta: the one that produces this file.

Basefiles should always be the most recent known version of a file.
These are the core storage mechanism;
the working directory is merely for ease of viewing (on Subscribers) and ease of editing (on each archive's Publisher).

The latest delta constructs the latest version of the basefile, from the previous version.
the first stored version of a file has no delta.
un-applying (reversing the effects of) the latest delta reveals the contents of the file at the previous delta,
accessed using the parent reference of the current delta.
The delta that has no parent is the SECOND version of the file.

The first version is recreated by un-applying that first, parentless delta.

The full file is never stored directly in the hash-object datastore; only its deltas.

To update the archive (Publisher-only):
----

To update the file across the network using any older version of the basefile:
---

Given the full chain of deltas for a file, and any version of the basefile (that exists in the delta chain),  
we can construct the full copy of the file, for any version in the delta chain, by sequentially applying or un-applying deltas.

We just start by hashing the basefile we do have, and matching that hash to its corresponding delta.

To check out a previous archive version:
-----

To find files that no longer exist in the working directory:
----
Treat them as empty files, with the last delta being an instruction to delete the basefile's contents. 

Deltas don't NEED to store their own parentage; that info can be gotten from the parent edition's hash for that file.