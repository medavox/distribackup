package com.medavox.distribackup.connections;

public enum Message
{
	BITFIELD		((byte)0x00,  2),
	STRING			((byte)0x01, -1),
	UBYTENUM		((byte)0x02,  2),
	USHORT			((byte)0x03,  3),
	UINTEGER		((byte)0x04,  5),
	ULONG			((byte)0x05,  9),
	BYTENUM			((byte)0x06,  2),
	SHORT			((byte)0x07,  3),
	INTEGER			((byte)0x08,  5),
	LONG			((byte)0x09,  9),
	ADDRESS			((byte)0x0A, -2),
	BYTE_ARRAY		((byte)0x0B, -1),
	PEER_INFO		((byte)0x0C, -2),
	ARCHIVE_STATUS	((byte)0x0D, -2),
	LIST			((byte)0x0E, -1),
	FILE_INFO		((byte)0x0F, -2),
	REQ_FOR_PEERS	((byte)0x10,  0),
	REQ_ALL_FILES	((byte)0x11,  0),
	FILE_DATA_CHUNK ((byte)0x12, -2),
	FILE_REQUEST	((byte)0x13, -2),
	GREETING		((byte)0x14, 16),
	EXIT_ANNOUNCE	((byte)0x15,  0),
	TREE_STATUS_REQ	((byte)0x16,  0),
	UPDATE_ANNOUNCE	((byte)0x17, -2),
	NO_HAZ			((byte)0x18, -2),
	PEER_INFO_REQ   ((byte)0x19,  0),
	HAZ_NAO         ((byte)0x1A, -2),
	MORE_PEERS      ((byte)0x1B, -2);
	
	public final byte IDByte;
	public final int length;
	
	/**A length value of -1 or -2 means Message length must be calculated during
	construction. Length of -1 indicates it's of variable-length using TLV,
	-2 indicates it's a compound message.
	Length in this context means the total Message length, including IDByte and 
	any other headers.*/
	Message(byte IDByte, int length)
	{
		this.IDByte = IDByte;
		this.length = length;
	}
	
	public static Message getMessageTypeFromID(byte id)
	{
		for(Message m : Message.values())//SUCH a useful, yet undocumented method!
		{
			if(m.IDByte == id)
			{
				return m;
			}
		}
		return null;
	}
}
