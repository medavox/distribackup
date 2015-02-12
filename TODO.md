implement receiving serialised file
	type-length-value, bencode, MessagePack
	list ALL possible message types, then derive needed data types
UUID and peerinfo sending

callback to send file

0. Finish primitive dataype binary conversion methods as needed

1. Implement compound type binary conversion methods (to and from)
    * FileInfo	DONE (still needs other binary methods implementing)
    * DirInfo
    * FileData	
    * PeerInfo
    * Address
    * List
    * HList

* if someone else (not the publisher) announces they have a file you want while you're waiting, request it from them

Structure/Design:
----------------

work out if we want to keep the length fields of List and Hlist as longs 
    (unlike EVERY OTHER distribackup message object),
or turn them into TNV (type-number-value) objects,
    without a number-of-bytes length field, but with a number-of-elements field
or just keep thwem as ints

* create an evaluation framework
* finish file transfer
* make exception catching consistent: have a consistent policy
* redo package structure
* actually use the checksums to do some error-checking
* Call requestArchiveState() during Peer.receivePeerInfo()
    - implement ConnectionOperator.requestArchiveStatus()
    - implement ConnectionOperator.sendArchiveStatus()
    - implement Peer.receiveArchiveStatus()
    - implement Peer.handleArchiveStatusRequest()
    
