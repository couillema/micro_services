package com.chronopost.vision.microservices.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mlecointe
 * 
 * @see https://www.owasp.org/index.php/Hashing_Java
 * 
 */
public final class SecurityUtil {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(SecurityUtil.class);

	/**
	 * 
	 */
	private SecurityUtil() {

	}

	/**
	 * @param stringToEncode
	 *            chaine à coder
	 * @param salt
	 *            grain de sel
	 * @return retourne en tableau de byte la nouvelle chaine codée
	 */
	public static String getHash(final String stringToEncode, final String salt) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			final String msg = "l'agorithme SHA-256 n'est pas dispo:" + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		digest.reset();

		try {
			digest.update(salt.getBytes("UTF-8"));
			final byte[] bb = digest.digest(stringToEncode.getBytes("UTF-8"));

			return byteToHex(bb);

		} catch (UnsupportedEncodingException e) {
			final String msg = "il n'est pas possible d encoder en  UTF-8:" + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}

	}

	/**
	 * Convertit des octets en leur representation hexadecimale (base 16),
	 * chacun se retrouvant finalement 'non signe' et sur 2 caracteres.
	 * 
	 * @see http 
	 *      ://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html
	 * 
	 * @param bits
	 *            tableau de bytes � convertir
	 * @return les octects converti en chaine de caracteres
	 */
	private static String byteToHex(final byte[] bits) {
		if (bits == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(bits.length * 2);
		// encod(1_bit)
		// =>
		// 2 digits
		for (int i = 0; i < bits.length; i++) {
			if (((int) bits[i] & 0xff) < 0x10) { 
				hex.append('0');
			}
			hex.append(Integer.toString((int) bits[i] & 0xff, 16));
			// [(bit+256)%256]^16
		}
		return hex.toString();
	}
}
