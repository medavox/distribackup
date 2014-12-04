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
10      | Request for Peers     | TLV   | Can have no payload (length 0), or UUIDs of peers already known 
11      | PeerInfo List         | TLNV  | Simple Array
12      | FileDataChunk         | TLV   | Yes, see list
13      | WholeFileData         | TLV   | Contains FileID, followed by ByteArray

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
0. (ID Byte)
0. (Length)         :UInteger
1. Name             :String
2. Path             :String
3. file size        :ULong
4. revision number  :ULong
5. checksum         :SHA1

PeerInfo Order of Constituent Objects
-------------------------------------
0. (ID Byte)
0. (Length)            :UInteger
1. UUID1               :Long
2. UUID2               :Long
3. GlobalRevisionNumber:ULong
4. isPublisherOrPeer   :bitfield<0>
5. Addresses           :List:Address


Address: Order of Constituent Objects
------------------------------------
0. (ID Byte)
0. (Length)            :UInteger
1. isUp                :bitfield<0>
2. usingHostname(NotIP):bitfield<1>
3. USing IPv6          :bitfield<2>
4. IP/hostname         :ByteArray.4/String
5. listenPort          :UShort
6. lastKnownTimeOnline :Long (ms since epoch)


List
-----
0. (ID byte)
0. (Length)            :UInteger
1. ID byte of elements
2. number of elements   :int
4. &lt;elements&gt;

FileDataChunk Order of Constituent Objects
----------------------------------------


