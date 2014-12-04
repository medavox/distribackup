<!-- MAX_CONNECTIONS
pipe
	peer
		ip address, port
listener -->

each file tree will need its own UUID and friendly name (which can be the name of the root directory)

the above may be completely wrong, based on our misunderstanding of tcp

min block size (256KB)
-----
* Mediocre domestic internet upload speed is still 1Mbps (128KB/s)
* so use that as a baseline for minimum useful chunk size

max block size is 16MB
-------
* a reasonable amount of data to have in memory at once, especially for RasPis

each update to a file will increment the revision of number of that file.

Types of Message extension
==========================

    file block
        file name/path
        index/offset from filestart
        checksum?
        length?
        payload:byte[]
        revision number:int/long
    filesend
        file metadata, eg {filename, mod time, access time, permissions(?), owner/group(?)}
    version info
    upgrade or gtfo

<!-- //'haveblock' advertisements to fellow subscribed mirrors -->
    block/file requests to other peers (broadcast to all known peers)

    request for peers (broadcast/individual)
    peer list
    some kind of consistency check with the publisher (checksum/length check over all files?)
    checksum request
    checksum data (per-file or whole tree)
    filesystem state
    //version/revision numbers for each block or update

* Make sure to use a raised value for the connection backlog on the listener socket.
    * This will allow more connections to be queued up before the OS returns "Connection refused",
should your server application become momentarily busy and unable to respond to incoming connections quickly enough.

If using ServerSocket in Java, specify the desired backlog as an argument to the constructor:
    ServerSocket server = new ServerSocket(listenPort, 50);

when there's a new publisher, each node can choose to join the new version of this swarm, or stick with the old, static on

need to build in support for different swarms

Step-By-Step
============
1. handshake - protocol/version number exchange, min java version
2. publisher adds this new peer to subscriber list
3. publisher sends newpeer list of other known peers
4. subscriber (sends to publisher/broadcasts to network) local file tree listing, and checksum per file
5. publisher/network sends any changed files
6. Connections for file transfer
