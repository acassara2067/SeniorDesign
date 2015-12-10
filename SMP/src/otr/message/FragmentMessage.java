/*
 *  Java OTR library
 *  Copyright (C) 2008-2009  Ian Goldberg, Muhaimeen Ashraf, Andrew Chung,
 *                           Can Tang
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of version 2.1 of the GNU Lesser General
 *  Public License as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package otr.message;

import ca.uwaterloo.crysp.otr.OTRException;
import ca.uwaterloo.crysp.otr.OutBuf;

/**
 * OTR Fragment message.
 * @author Can Tang (c24tang@gmail.com)
 */
public class FragmentMessage extends OTRMessage {
	private String contents;
	
	/**
	 * Constructs a fragment message.
	 * @param input The fragment.
	 */
	public FragmentMessage(String input) {
		super(MSG_FRAGMENT);
		this.contents = input;
	}
	

	public void write(OutBuf stream) throws OTRException {
		stream.writeBytes(contents.getBytes());
	}

	public byte[] getContent(){
		return contents.getBytes();
	}
}
