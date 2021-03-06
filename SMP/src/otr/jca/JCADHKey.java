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

package otr.jca;

import java.math.BigInteger;

/**
 * Abstract DH key using the JCA provider. The g, l and p parameters as well as the 
 * actual key values are represented using BigInteger objects.
 * 
 * @author Andrew Chung (kachung@uwaterloo.ca)
 */
public abstract class JCADHKey extends otr.crypt.DHKey{
	// Key parameters
	private BigInteger g;
	private BigInteger p;
	
	public JCADHKey(BigInteger g, BigInteger p) {
		this.g = g;
		this.p = p;
	}
	
	public byte[] getG() {
		return JCAMPI.toBytes(g);
	}

	public byte[] getP() {
		return JCAMPI.toBytes(p);
	}
}
