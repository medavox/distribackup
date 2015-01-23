Protocol Binary Spec 
====================

Current Issues
--------------

###Length Fields In Lists And Their Containing Objects

Using int as the length field type could allow a string of Messages, when still strung together as a byte[] (ie a compound Message!), to overflow an integer's max value.

PeerInfo length field is an int; however, one of its containing types is a List, whose length field is a long!
this means we could have an enclosed List whose length was longer than the enclosing object, whose length is supposed to be the sum of all of its elements.

Basically there is a tension here between number of bytes from a stream (which is potentially infinite) and number of elements in an array.

Any situation in which a potentially infinite (or at least very large) array-like object like List, String or HList is enclosed by another object (with the same or smaller max length) whose length field should total everything inside it, could cause this to happen.

Any situation where an object encloses another object, and their length fields are the same size, could cause this to happen.


###Possible Solutions:

1. Just disallow Lists inside PeerInfo objects from being so big that they overflow PeerInfo's length field?
    - Clunky, as the length measures total bytes, not number of elements, and this would be annoying to count all the time to make sure we're not going over, especially in HLists

2. Make it so that PeerInfo (and any other Message types which contain a List or HList) only count their length up to the start of the List, which must be the last element. The List's length field then denotes the rest of the message

    - downside is, it makes it so that you hav to check 2 places to find the total Message length (including List)
    
3. Lists and HLists could become TNV, dropping the length field. Variable-length elements would need their own length fields (as before), and fixed-length elements' length would be known. Downside is, it would be slow to iterate through Lists of variable-length messages. The total byte-length would not be known - but would also not NEED to be known by Java, which is an advantage for supporting large arrays.

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



ID byte | Name  | Payload length in bytes | Is Compound / Notes
---|------------|-------------------------|-------------|
00 | bitfield              | 1     | Contains up to 8 booleans. Knowing which bits are used is left to implementation, though start with the LSB
01 | String                | TLV   | UTF16; but length is still in bytes |
02 | UByteNum              | 1        |   |
03 | UShort                | 2        |   |
04 | UInteger              | 4        |   |
05 | ULong                 | 8        |   |
06 | ByteNum               | 1        |   |
07 | Short                 | 2        |   |
08 | Integer               | 4        |   |
09 | Long                  | 8        |   |
0A | Address               | Compound | [Yes, see entry below](#Address)
0B | ByteArray             | TLV      |   |
0C | PeerInfo              | Compound | [Yes, see entry below](#PeerInfo)
0D | DirectoryInfo         | Compound | Similar to FileInfo, but can contain other FileID or DirectoryIDs [Yes, see entry below](#DirectoryInfo)
0E | List                  | see expl |
0F | FileInfo              | Compound | [Yes, see entry below](#FileInfo)
10 | Request For Peers     | 0/TLV    | Can have no payload (length 0), or List of UUIDs of peers already known 
11 | Not Used Currently    | -        | -
12 | File Data Chunk       | Compound | [Yes, see entry below](#FileDataChunk)
13 | File Request          | TLV      | Contains FileInfo. FileInfo's RevNum can be for a specific version, or -1 for latest version
14 | Greeting              | 16       | Contains UUID(long msb,long lsb). If UUID is unknown to receiver, it may request the sender's PeerInfo
15 | Exit Announcement     | 0        |   | Usually sent to all known peers
16 | File Tree Status Req  | 0        |   | Sent to 1 peer at a time
17 | Update Announcement   | Compound | New GRN, plus a FileID List of affected files   |
18 | Heterogeneous List    | TLV      | each element has its own ID Byte

TODO
----

* File Request Reply: I don't have that version/file
* Global Revision Number
    - a simple counter for the whole file tree, incremented on every revision
    - is now contained as a ULong inside a PeerInfo
* 'I now have this file version' announcement
    - announces to network that this peer now has this FileID upon completion, so others can request it

Compound Object Constituents
----------------------------

<a name="FileInfo" />FileInfo 
-----------------------------------
Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | Integer
1. Name             | String
2. Path             | String
3. file size        | Long
4. revision number  | Long
5. checksum         | SHA1

<a name="DirectoryInfo" />DirectoryInfo
-----------
A reply to file tree status req, with this as root dir

Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | Integer
2. Name             | String
3. Path             | String
4. Contents         | HList:FileInfo,DirectoryID

List
-----
Lists of static-length elements will just be a series of payloads without delimiters;
variable-length elements in a list will each have their own length field

Element                 | Type
------------------------|--------
0. (ID Byte)            | a byte
1. (Length)             | Long
2. ID byte of elements  | a byte
3. number of elements   | int
4. &lt;elements&gt;     | ?


HList
-----
Element               | Type
----------------------|--------
0. (ID Byte)          | a byte
0. (Length)           | Long
1. Number of Elements | int
1. elements | each has its own ID Byte


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
Element                 | Type
------------------------|--------
0. (ID Byte)            | a byte
1. (Length)             | Integer
2. FileInfo             | FileInfo
3. isWholeFile          | bitfield<0>
4. offset               | Long
5. payload              | ByteArray

Wild Speculation
================

* change List and Hlist from TLNV to just TNV, to prevent cases where the total number of bytes is > Integer.MAX_VALUE
* Add the following message types:
    - Request for all files: used by a new peer to ask for all the files in the archive, without having to specifically request each one (also avoid querying for them)
* Differentiate bewteen supporting data types (like int, bitfield, String) and objects that can actually be sent down the Socket. The latter are more likely to have a custom java class, and implement Communicable.

Number of downloaders, to allow load balancing between Publisher and finished-updating Subscribers

Subscriber announcement of the latest revision number when it becomes up to date

