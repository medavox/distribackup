Protocol Binary Spec 
====================

Current Issues
--------------

###Length Fields In Containing Objects

Using int as the length field type could allow a string of Messages, when still strung together as a byte[] (ie a compound Message), to overflow an integer's max value.

PeerInfo's length field is an int; however, one of its containing types is a List, whose length field is a long.
This means we could have an enclosed List whose length was longer than the enclosing object, whose length is supposed to be the sum of all of its elements.

There is an inherent tension here between number of bytes from a stream (which is potentially infinite) and number of elements in an array.

Any situation where an object encloses another object, and their length fields are the same size, could cause this to happen.


###Possible Solutions:

1. Just disallow Lists inside PeerInfo objects from being so big that they overflow PeerInfo's length field.
    - Clunky, as the length measures total bytes, not number of elements, and this would be annoying to count all the time to make sure we're not going over, especially in HLists

2. Make it so that PeerInfo (and any other Message types which contain a List or HList) only count their length up to the start of the List, which must be the last element. The List's length field then denotes the length of the rest of the message

    - downside: 2 locations must be checked to find the total Message length (including List)
    
3. Lists and HLists could become TNV, dropping the length field. Variable-length elements would need their own length fields (as before), and fixed-length elements' length would be known. 

    - Downside: it would be slow to iterate through Lists of variable-length messages. Each element would need its own own length field querying, and it would be difficult to know how far through the List we are.
    The total byte-length would not be known - but would also not need to be known by Java, which is an advantage for supporting large arrays. Can't have an overflow of a value you don't explicitly store!

ID Bytes
--------

Each Object type has an ID byte, followed either by its payload (for static-length types)
or a length int (without the int type header attached, because we know what should 
follow for a certain message type), followed by its payload.

Objects inside a compound object don't need an ID byte; their type is known from the object's structure definition.
Variable-length types (such as strings) still need their length field though.

Message Lengths
---------------

Message types with a static payload length (eg bitfield, ULong) don't have (or need) a length attribute. Their length is built into the spec.

Variable-length homogenous types (String, ByteArray) are TLV; see below.

TLV
:   Type Length Value, a simple way of defining variable-length types such as Strings. Length is a Int: 32-bits, 0 to 2^31 -1. Changed from unisgned int due to Java array addressing limitations.

TLNV
:   Type Length Number Value. An extra field for array types, specifying how many elements there are. Useful for progress estimation, or simple iteration

Supporting Data Types
---------------------

The following Objects are not used as standalone Messages by Distribackup, but instead form part of compound Messages, and so are generally used without their ID Byte. Despite this, an ID Byte number is assigned for each in case this situation changes, or another program uses this library differently.

ID byte | Name  | Payload length in bytes | Is Compound / Notes
---|------------|-------------------------|-------------|
00 | bitfield              | 1     | Contains up to 8 booleans. Knowing which bits are used is left to implementation, but start with the LSB
01 | String                | TLV   | UTF16; but length is still in bytes |
02 | UByteNum              | 1        |   |
03 | UShort                | 2        |   |
04 | UInteger              | 4        |   |
05 | ULong                 | 8        |   |
06 | ByteNum               | 1        |   |
07 | Short                 | 2        |   |
08 | Integer               | 4        |   |
09 | Long                  | 8        |   |
0A | Address               | Compound | [Yes, see entry below](#Address) |
0B | ByteArray             | TLV      |   |
0F | FileInfo              | Compound | [Yes, see entry below](#FileInfo) |
0E | List                  | TLNV     | |

Sendable (Communicable) Message Objects
---------------------------------------

These objects can be sent directly to another Peer.

ID byte | Name  | Payload length in bytes | Is Compound / Notes
---|------------|-------------------------|-------------|
0C | PeerInfo              | Compound | [Yes, see entry below](#PeerInfo) Can be sent in reply to a PeerInfo Request |
0D | Archive Status        | Compound | Same type as update announcment, but with a different IDByte: [ArchiveInfo](#ArchiveInfo). A reply to Archive Status Request
10 | Request For Peers     | 0/TLV    | Can have no payload (length 0), or List of UUIDs of peers already known 
11 | Request All Files     | 0        | Asks for latest known version of all files. Likely to be broadcast by a new subscriber, to all known peers.
12 | File Data Chunk       | Compound | [Yes, see entry below](#FileDataChunk)
13 | File Request          | TLV      | Contains FileInfo. FileInfo's RevNum can be for a specific version, or -1 for latest version
14 | Greeting              | 16       | Contains UUID(long msb,long lsb). If UUID is unknown to receiver, it may request the sender's PeerInfo
15 | Exit Announcement     | 0        | Usually sent to all known peers
16 | Archive Status Request| 0        | Queries overall archive status, not any 1 peer's mirror
17 | Update Announcement   | Compound | Same type as Archive Status, but with a different IDByte: [ArchiveInfo](#ArchiveInfo). Sendable by Publisher only.|
18 | "no haz" FileReq Reply| Compound | Used as a reply to a File Request when a peer doesn't have a (version of a?) file requested of it. Contains a list of FileInfos that the requesting peer asked for, which the replying peer doesn't have. 
19 | PeerInfo Request      | 0        | A request for the connected Peer's PeerInfo
1A | "haz nao" announcement| Compound | announces to network upon completion that this peer now has this FileID, so others can request it. Contains a list (usually of length 1) of FileInfos of file this peer now has
1B | More Peers            | Compound | Contains a List:PeerInfo. Is a reply to a request for more Peers.

* Personal PeerInfo Request
* Peer(s) Info List
* Personal PeerInfo
* File Tree Status ---- provided by DirectoryInfo

Compound Object Constituents
----------------------------

Empty directories in a tree are also specified by this type.
If isDirectory is true, the last 3 fields can be left out.

NOTE: It's not necessary to specify all parent directories this way; any needed
parent directories will be created when files in them are created.

<a name="FileInfo" />FileInfo 
-----------------------------------
Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | Integer
1. Name             | String
2. Path             | String
2. isDirectory      | bitfield<0>
3. file size        | Long
4. revision number  | Long
5. checksum         | SHA1

<a name="ArchiveInfo" />Archive Status
-----------

A "joint type": this structure represent two Communicables: Archive Status and Update Announcement.

The information within is used differently depending on the label. 
Only the Publisher can send an Update Announcement.
As an Archive Status, it's a reply to file tree status req. 
It gives the known status of the global archive, not the requested peer's local mirror.

Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
1. (Length)         | Integer
2. Global RevNum    | Long
3. Files            | List:FileInfo

List
-----
Lists of static-length elements will just be a series of payloads without delimiters;
variable-length elements in a list will each have their own length field

Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
1. (Length)            | Long
2. ID byte of elements | a byte
3. number of elements  | int
4. &lt;elements&gt;    | ?


<a name="PeerInfo" />PeerInfo
--------

Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
0. (Length)            | Integer
1. UUID1:msb           | Long
2. UUID2:lsb           | Long
3. GlobalRevisionNumber| Long
4. isPublisher         | bitfield<0>
5. Addresses           | List:Address


<a name="Address" />Address
-------
Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
1. (Length)            | Integer
2. isUp                | bitfield<0>
3. usingHostname(NotIP)| bitfield<1>
4. USing IPv6          | bitfield<2>
5. IP/hostname         | ByteArray.4/String
6. listenPort          | UShort
7. lastKnownTimeOnline | Long (ms since epoch)


<a name="FileDataChunk" />FileDataChunk
--------
Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
1. (Length)            | Integer
2. FileInfo            | FileInfo
3. isWholeFile         | bitfield<0>
4. offset              | Long
5. payload             | ByteArray


Each peer has to request files it wants
Other peers will help by sending requested files (or pieces of it), but can also reply with "I don't have it"
all peers will announce when they have finished downloading a file, so other peers now know to request that file from it

This means that a File Tree Status Requset isn't for asking another peer about its personal state, which is pointless 
(Each peer is responsible for managing its own archive copy, and this type of request would only 
benefit a system where peers try to be helpful by pre-emptively pushing to each other).

Instead, it's asking what a peer knows about the about the global archive tree
(ie what the latest version out there are, whether the asked peer has the files or not).

TO DO?
======

* Change List from TLNV to just TNV, to avoid cases where the total number of bytes is > Integer.MAX_VALUE
* Add the following message types:
    
    - 'I now have this file version' announcement
        - announces to network that this peer now has this FileID upon completion, so others can request it
* Merge Update announcement with Archive Status, as they are functioanlly the same, without allowing peers other than the Publisher to push updates
* add a bitfield hasBeenDeleted (or something) to FileInfos, so they can describe the deletion of a file in an Update Announcement
Number of downloaders, to allow load balancing between Publisher and finished-updating Subscribers

Subscriber announcement of the latest revision number when it becomes up to date

need a way of closing/removing closed ConnectionOperators

DONE
=====

* Global Revision Number
    - a simple counter for the whole file tree, incremented on every revision
* Request for all files: used by a new peer to ask for all the files in the archive, 
without having to specifically request each one (also avoid querying for them)
* File Request Reply: I don't have that version/file
* Differentiate between supporting data types (like int, bitfield, String) and 
objects that can actually be sent down the Socket. 
The latter are more likely to have a custom java class, and implement Communicable.
