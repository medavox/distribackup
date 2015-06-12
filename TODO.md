* if someone else (not the publisher) announces they have a file you want while you're waiting, request it from them

Structure/Design:
----------------

work out if we want to keep the length fields of List and Hlist as longs 
    (unlike EVERY OTHER distribackup message object),
or turn them into TNV (type-number-value) objects,
    without a number-of-bytes length field, but with a number-of-elements field
or just keep them as ints

* finish file transfer
    - implement file chunking, using a chunk cache
* make exception catching consistent: have a consistent policy
* redo package structure
* actually use the checksums to do some error-checking
* Implement file deletion notifications    

write logic for:
----------------

* connecting to Peer we've been told about
* asking for files which are different between our local and the global archive states
    - making the list of differences - eg localArchive.difference(globalArchive):ArchiveInfo
    - knowing what we did enough to act on updated info/state
        - checking to make sure we received a response (either the file or NO_HAZ)
        - how to know when we sent a file request, so we know which file a NO_HAZ pertains to
    - choosing a peer (or really its ConnectionOperator) to request files from
