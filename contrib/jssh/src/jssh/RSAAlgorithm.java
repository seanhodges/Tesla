/* RSAAlgorithm.java
 *
 * Copyright (C) 2002  R M Pitman <http://www.pitman.co.za>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jssh;

import java.security.*;
import java.math.BigInteger;	//used to implement RSA.

/** This class provides static methods that implement RSA encryption.
 */
public class RSAAlgorithm
{
    public static byte[] publicKeyEncrypt(
	byte[] clearData_,
        byte[] exponent_,
        byte[] modulus_,
	MessageDigest md_) 
    {
        int offset = 0;
        byte[] encryptionBlock = new byte[modulus_.length];

	/* Pad the data according to the PKCS#1 standard. The format is:
	 * Byte 0 is 0
	 * Byte 1 is 2 (indicates public-key encrypted data)
	 * Then there are nonzero random bytes to fill unused space
	 * Then a zero byte
	 * Then the data to be encrypted in the least significant bytes.
	 */
        encryptionBlock[0] = 0;
        encryptionBlock[1] = 2;
        offset = 2;
        for (int i = 2; i < (encryptionBlock.length - clearData_.length - 1); i++)
            encryptionBlock[offset++] = SSHMisc.getNonZeroRandomByte(md_);
        encryptionBlock[offset++] = 0;
        for (int i = 0; i < clearData_.length; i++)
            encryptionBlock[offset++] = clearData_[i];

        BigInteger m = new BigInteger (1, modulus_);
        BigInteger e = new BigInteger (1, exponent_);
        BigInteger message = new BigInteger (1, encryptionBlock);

        message = message.modPow(e, m);	    //RSA Encryption !!

        byte[] messageBytes = message.toByteArray();

/* THIS DOES NOT APPEAR TO CAUSE A PROBLEM.... 
        //there should be no zeroes at the begining but we have to fix it (JDK bug !!)
        int tempOffset = 0;
        while (messageBytes[tempOffset] == 0)
            tempOffset++;
        for (int i = encryptionBlock.length - messageBytes.length + tempOffset;
		i < encryptionBlock.length; i++) {
	    encryptionBlock[i] = messageBytes[tempOffset++];
	}
	return encryptionBlock; */

	return messageBytes;
    }

    public static byte[] encrypt(
	byte[] clearData_,
        byte[] exponent_,
        byte[] modulus_) 
    {
        BigInteger m = new BigInteger (1, modulus_);
        BigInteger e = new BigInteger (1, exponent_);
        BigInteger message = new BigInteger (1, clearData_);

        message = message.modPow(e, m);	    //RSA Encryption !!

        byte[] messageBytes = message.toByteArray();
	return messageBytes;
    }

    public static byte[] stripPKCSPadding(byte[] padded) 
	throws SSHProtocolException
    {
	if (padded[0] != 2) {
	    throw new SSHProtocolException("Invalid PKCS format");
	}

	int i;
	for (i=1; i<padded.length; i++) {
	    if (padded[i] == 0)
		break;
	}
	if (i == padded.length)
	    throw new SSHProtocolException("Invalid PKCS format");

	byte[] remaining = new byte[padded.length - i - 1];
	System.arraycopy(padded, i+1,
	    remaining, 0, remaining.length);

	return remaining;
    }
}
