Protocol Binary Spec 
====================

Using UInt as the length of Messages presents a problem in Java: we are allowing byte arrays longer than the maximum length of an array (Integer.MAX_VALUE). In order to prevent problems, this would require a ByteBuffer (or something) in place of nearly every use of byte[]. Euch.

However, even using int as the length field type would allow a string of Messages which are still strung together (ie a compound Message!) that could, combined, still overflow an integer's max value.

###ID Bytes

Each Object type will have an ID byte, followed either by its payload (for static-length types)
or a length int (without the int type header attached, because we know what should 
follow for a certain message type), followed by its payload.

Objects inside a compound object don't need an ID byte; their type is known from the object's structure definition.
Variable-length types (such as strings) still need their length field though.

###Message Lengths

Message types with a static payload length (eg bitfield, ULong) don't have (or need) a length attribute. Their length is built into the spec.

Variable-length homogenous types (String, ByteArray) are TLV; see below.

TLV
:   Type Length Value, a simple way of defining variable-length types such as Strings. Length is a Int: 32-bits, 0 to 2^31 -1. Changed from unisgned int due to Java array addressing limitations.

TLNV
:   Type Length Number Value. An extra field for array types, specifying how many elements there are. Useful for progress estimation, or simple iteration

ID byte | Name       | Payload length in bytes | Is Compound / Notes
--------|------------|-------------------------|-------------|
00      | bitfield              | 1     | Contains up to 8 booleans. Knowing which bits are used is left to implementation, though start with the LSB
01      | String                | TLV   | UTF16; but length is still in bytes |
02      | UByteNum              | 1        |   |
03      | UShort                | 2        |   |
04      | UInteger              | 4        |   |
05      | ULong                 | 8        |   |
06      | ByteNum               | 1        |   |
07      | Short                 | 2        |   |
08      | Integer               | 4        |   |
09      | Long                  | 8        |   |
0A      | Address               | Compound | [Yes, see entry below](#Address)
0B      | ByteArray             | TLV      |   |
0C      | PeerInfo              | Compound | [Yes, see entry below](#PeerInfo)
0D      | DirectoryInfo         | Compound | Similar to FileInfo, but can contain other FileID or DirectoryIDs [Yes, see entry below](#DirectoryInfo)
0E      | List                  | see expl |
0F      | FileInfo              | Compound | [Yes, see entry below](#FileInfo)
10      | Request For Peers     | 0/TLV    | Can have no payload (length 0), or List of UUIDs of peers already known 
11      | Not Used Currently    | -        | -
12      | FileData              | Compound | [Yes, see entry below](#FileData)
13      | File Request          | TLV      | Contains FileInfo <!--; can be -->
14      | Greeting              | 16       | Contains UUID. If UUID is unknown to receiver, it may request the sender's PeerInfo
15      | Exit Announcement     | 0        |   | Usually sent to all known peers
16      | File Tree Status Req  | 0        |   | Sent to 1 peer at a time
17      | Update Announcement   | Compound | New GRN, plus a FileID List of affected files   |
18      | Heterogeneous List    | TLV      | each element has its own ID Byte

TODO
----

* File Request Reply: I don't have that version/file
* Global Revision Number
    - a simple counter for the whole file tree, incremented on every revision
    - is now contained as a ULong inside a PeerInfo
* 'I now have this file version' announcement
    - announces to network that this peer now has this FileID upon completion, so others can request it

<a name="FileInfo" />FileInfo Order of Constituent Objects
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

HList
-----
Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | Integer
1. elements | each with their own ID Byte

<a name="PeerInfo" />PeerInfo
--------
Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
0. (Length)            | Integer
1. UUID1               | Long
2. UUID2               | Long
3. GlobalRevisionNumber| Long
4. isPublisherOrPeer   | bitfield<0>
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


List
-----
Lists of static-length elements will just be a series of payloads without delimiters;
variable-length elements in a list will each have their own length field

Element                 | Type
------------------------|--------
0. (ID Byte)            | a byte
1. (Length)             | Integer
2. ID byte of elements  | a byte
3. number of elements   | int
4. &lt;elements&gt;     | ?

<a name="FileData" />FileData
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

Number of downloaders, to allow load balancing between Publisher and finished-updating Subscribers

Subscriber announcement of the latest revision number when it becomes up to date

