/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: marcus $
 * $Date: 2000/12/20 21:48:30 $
 * $Name:  $
 *****************************************************************************/
package de.mud.ssh;

public abstract class Cipher {

    public static Cipher getInstance(String algorithm) {
        Class c;
        try {
            c = Class.forName("de.mud.ssh."+algorithm);
            return (Cipher)c.newInstance();
        }
        catch(Throwable t) {
            return null;
        }
    }

    public byte[] encrypt(byte[] src) {
        byte[] dest = new byte[src.length];
        encrypt(src, 0, dest, 0, src.length);
        return dest;
    }

    public abstract void encrypt(byte[] src, int srcOff, byte[] dest, int destOff, int len);

    public byte[] decrypt(byte[] src) {
        byte[] dest = new byte[src.length];
        decrypt(src, 0, dest, 0, src.length);
        return dest;
    }

    public abstract void decrypt(byte[] src, int srcOff, byte[] dest, int destOff, int len);

    public abstract void setKey(byte[] key);

    public void setKey(String key) {
        setKey(key.getBytes());
    }

    public static final byte SSH_CIPHER_NONE = 0;	// No encryption
    public static final byte SSH_CIPHER_IDEA = 1;	// IDEA in CFB mode (patented)
    public static final byte SSH_CIPHER_DES  = 2;	// DES in CBC mode
    public static final byte SSH_CIPHER_3DES = 3;	// Triple-DES in CBC mode
    public static final byte SSH_CIPHER_TSS  = 4;	// An experimental stream cipher
    public static final byte SSH_CIPHER_RC4  = 5;	// RC4	(patented)
    public static final byte SSH_CIPHER_BLOWFISH  = 6;	// Bruce Schneier's Blowfish (public domain)
}
