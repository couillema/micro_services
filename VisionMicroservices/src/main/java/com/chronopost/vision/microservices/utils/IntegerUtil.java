package com.chronopost.vision.microservices.utils;

public class IntegerUtil {
    private final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z' ,
        'A' , 'B' , 'C' , 'D' , 'E' , 'F' ,
        'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,
        'M' , 'N' , 'O' , 'P' , 'Q' , 'R' ,
        'S' , 'T' , 'U' , 'V' , 'W' , 'X' ,
    };
    
    private final static int MAX_RADIX = digits.length;

    /**
     * Renvoie un String du premier argument dans la base spécifié par le second
     * argument.<br>
     * {@link Integer#toString(int i, int radix)} ne sait pas convertir vers une
     * base supérieur à {@link Character#MAX_RADIX} (36).<br>
     * Cette méthode reprend le fonctionnement de
     * {@link Integer#toString(int i, int radix)} en ajoutant de nouveau
     * caractères utilisables ([A-X]) pour étendre la base maximale utilisable à
     * 60.<br>
     * <strong>!!! ATTENTION !!! Ne jamais utiliser {@link String#toUpperCase()}
     * ou {@link String#toLowerCase()} sur le résultat de cette méthode avec une
     * base supérieure à 36 ! Il deviendrait impossible de distinguer les
     * valeurs de 10 à 33 (a-x) des valeurs supérieures (A-X).</strong>
     * 
     * @param i
     *            an integer to be converted to a string.
     * @param radix
     *            the radix to use in the string representation.
     * 
     * @return a string representation of the argument in the specified radix.
     */
    public static String convertToBase(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > MAX_RADIX) {
            radix = 10;
        }

        /* Use the faster version */
        if (radix == 10) {
            return Integer.toString(i);
        }

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }
}
