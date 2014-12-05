Protocol Binary Spec 

ID byte | Name       | Payload length in bytes | Is Compound / Notes
--------|------------|-------------------------|-------------|-------
00      | bitfield              | 1     | Contains up to 8 booleans. Knowing which bits are used is left to implementation, though start with the LSB
01      | String                | TLV   | UTF16; but length is still in bytes |
02      | UByteNum              | 1     |   |
03      | UShort                | 2     |   |
04      | UInteger              | 4     |   |
05      | ULong                 | 8     |   |
06      | ByteNum               | 1     |   |
07      | Short                 | 2     |   |
08      | Integer               | 4     |   |
09      | Long                  | 8     |   |
0A      | ByteArray             | TLV   |   |
0B      | FileID                | TLV; total | Yes, see list    
0C      | PeerInfo              | TLV; total | Yes, see list    
0D      | DirectoryInfo         | TLV   |   |
0E      | List                  | see expl  |
0F      | Address               | TLV   | Yes, See list    
10      | Request For Peers     | TLV   | Can have no payload (length 0), or UUIDs of peers already known 
11      | PeerInfo List         | TLNV  | Simple Array
12      | FileData              | TLV   | See list
13      | File Request          | TLV   | Contains FileID; can be
14      | Greeting              | 18    | Contains UUID + Listen Port
15      | Exit Announcement     | 0     |   |
16      | File Tree Status Req  | 0     |   |
17      | Update Announcement   | 
18      | DirectoryID           | TLV   |   |
19      | Heterogeneous List    | TLV   | each element has its own ID Byte

TLV
:   Type-Length Value, a simple way of defining variable-length types such as Strings. Length is an unsigned Int: 32-bits, 0 to 2^32 -1

TLNV
: An extra field for array types, specifying how many elements there are. Useful for progress estimation, or simple iteration

Each type will have an ID byte, follow either by its payload (for static-length types)
or a length int (without the int type header attached, because we know what should 
follow for a certain message type), followed by its payload.

Objects inside a compound object don't need an ID byte; their type is inferred from the object's structure definition.
Variable-length types (such as strings) still need their length field though.

FileID Order of Constituent Objects
-----------------------------------
Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | UInteger
1. Name             | String
2. Path             | String
3. file size        | ULong
4. revision number  | ULong
5. checksum         | SHA1

DirectoryID
-----------

when directory is the root, this is a reply to file tree status req

Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | UInteger
2. Name             | String
3. Path             | String
4. Contents         | HList:FileID,DirectoryID

HList
-----
Element             | Type
--------------------|--------
0. (ID Byte)        | a byte
0. (Length)         | UInteger
1. elements | each with their own ID Byte

PeerInfo
--------
Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
0. (Length)            | UInteger
1. UUID1               | Long
2. UUID2               | Long
3. GlobalRevisionNumber| ULong
4. isPublisherOrPeer   | bitfield<0>
5. Addresses           | List:Address


Address
-------
Element                | Type
-----------------------|--------
0. (ID Byte)           | a byte
1. (Length)            | UInteger
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
1. (Length)             | UInteger
2. ID byte of elements  | a byte
3. number of elements   | int
4. &lt;elements&gt;     | ?

FileData
--------

Element                 | Type
------------------------|--------
0. (ID Byte)            | a byte
1. (Length)             | UInteger
2. FileID               | FileID
3. isWholeFile          | bitfield<0>
4. offset               | ULong
5. payload              | ByteArray
