package org.dancecoder.smailer.model;

import org.dancecoder.smailer.pdu.PDU;

public class MessageListItem {

	public enum Status {
	receivedUnread, // 0, received unread message (i.e. new message)
	receivedRead,   // 1, received read message
	storedUnsent,		// 2, stored unsent message (only applicable to SMs)
	storedSent,			// 3, stored sent message (only applicable to SMs)
	all,						// 4, all messages (only applicable to +CMGL command)		
	unknown
}
	
	private int index;	
	private Status status;
	private int length;
	private PDU pdu;

	
	public MessageListItem(int index, int stat, int length, PDU pdu) {
		this.index = index;
		this.length = length;
		this.pdu = pdu;
		switch(stat) {
			case 0: this.status = Status.receivedUnread; break;
			case 1: this.status = Status.receivedRead; break;
			case 2: this.status = Status.storedUnsent; break;
			case 3: this.status = Status.storedSent; break;
			default: this.status = Status.unknown; break;
		}
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return the message
	 */
	public PDU getPdu() {
		return pdu;
	}
}
