package de.poweruser.powerserver.gamespy;

import de.poweruser.powerserver.games.GameBase;

/*
 * This is a Java port of the algorithm, that the Gamespy master server and the games use, 
 * to generate the challenge-response for Gamespy's "secure" protocol
 * The original C code (GSMSALG) was written by Luigi Auriemma: http://aluigi.altervista.org/
 * Previous ports from C to PHP by Lithium and from PHP to C# by FordGT90Concept were most helpful in order to get this done
 */

public class GamespyValidation {

    private static final short[] enctype1_data = new short[] { 0x01, 0xba, 0xfa, 0xb2, 0x51, 0x00, 0x54, 0x80, 0x75, 0x16, 0x8e, 0x8e, 0x02, 0x08, 0x36, 0xa5, 0x2d, 0x05, 0x0d, 0x16, 0x52, 0x07, 0xb4, 0x22, 0x8c, 0xe9, 0x09, 0xd6, 0xb9, 0x26, 0x00, 0x04, 0x06, 0x05, 0x00, 0x13, 0x18, 0xc4, 0x1e, 0x5b, 0x1d, 0x76, 0x74, 0xfc, 0x50, 0x51, 0x06, 0x16, 0x00, 0x51, 0x28, 0x00, 0x04, 0x0a, 0x29, 0x78, 0x51, 0x00, 0x01, 0x11, 0x52, 0x16, 0x06, 0x4a, 0x20, 0x84, 0x01, 0xa2, 0x1e, 0x16, 0x47, 0x16, 0x32, 0x51, 0x9a, 0xc4, 0x03, 0x2a, 0x73, 0xe1, 0x2d, 0x4f, 0x18, 0x4b, 0x93, 0x4c, 0x0f, 0x39, 0x0a, 0x00, 0x04, 0xc0, 0x12, 0x0c, 0x9a, 0x5e, 0x02, 0xb3, 0x18, 0xb8, 0x07, 0x0c, 0xcd, 0x21, 0x05, 0xc0, 0xa9, 0x41, 0x43, 0x04, 0x3c, 0x52, 0x75, 0xec, 0x98, 0x80, 0x1d, 0x08, 0x02, 0x1d, 0x58, 0x84, 0x01, 0x4e, 0x3b, 0x6a, 0x53, 0x7a, 0x55, 0x56, 0x57, 0x1e, 0x7f, 0xec, 0xb8, 0xad, 0x00, 0x70, 0x1f, 0x82, 0xd8, 0xfc, 0x97, 0x8b, 0xf0, 0x83, 0xfe, 0x0e, 0x76, 0x03, 0xbe, 0x39, 0x29, 0x77, 0x30, 0xe0, 0x2b, 0xff, 0xb7, 0x9e, 0x01, 0x04, 0xf8, 0x01, 0x0e, 0xe8, 0x53, 0xff, 0x94, 0x0c, 0xb2, 0x45, 0x9e, 0x0a, 0xc7, 0x06, 0x18, 0x01, 0x64, 0xb0, 0x03, 0x98, 0x01, 0xeb, 0x02, 0xb0, 0x01, 0xb4, 0x12, 0x49, 0x07, 0x1f, 0x5f, 0x5e, 0x5d, 0xa0, 0x4f, 0x5b, 0xa0, 0x5a, 0x59, 0x58, 0xcf, 0x52, 0x54, 0xd0, 0xb8, 0x34, 0x02, 0xfc, 0x0e, 0x42, 0x29, 0xb8, 0xda, 0x00, 0xba, 0xb1, 0xf0, 0x12, 0xfd, 0x23, 0xae, 0xb6, 0x45, 0xa9, 0xbb, 0x06, 0xb8, 0x88, 0x14, 0x24, 0xa9, 0x00, 0x14, 0xcb, 0x24, 0x12, 0xae, 0xcc, 0x57, 0x56, 0xee, 0xfd, 0x08, 0x30, 0xd9, 0xfd, 0x8b, 0x3e, 0x0a, 0x84, 0x46, 0xfa, 0x77, 0xb8 };

    private final byte[] securekey;
    private final byte[] gamekey;
    private final EncType type;
    private String validateString;

    public GamespyValidation(String securekey, String gamekey, EncType type) {
        if(securekey == null || gamekey == null || type == null) { throw new IllegalArgumentException("GamespyValidation: null parameters are not permitted."); }
        if(securekey.length() != 6) { throw new IllegalArgumentException("GamespyValidation: The challenge string must have a length of 6 characters. <" + securekey + "> has " + securekey.length()); }
        if(gamekey.length() != 6) { throw new IllegalArgumentException("GamespyValidation: The game key must have a length of 6 characters. <" + gamekey + "> has " + gamekey.length()); }
        this.gamekey = gamekey.getBytes();
        this.securekey = securekey.getBytes();
        this.type = type;
        this.validateString = null;
    }

    public GamespyValidation(String securekey, GameBase game, EncType type) {
        this(securekey, game.getGamespyKey(), type);
    }

    public String getValidationString() {
        if(this.validateString != null) { return this.validateString; }

        short[] table = new short[256];
        int[] temp = new int[4]; // Some Temporary variables

        for(short i = 0; i < 256; i++) {
            table[i] = i;
        }

        for(short i = 0; i < 256; i++) {
            // Scramble the Table with our Handoff
            temp[0] = (temp[0] + table[i] + this.gamekey[i % this.gamekey.length]) & 255;
            temp[2] = table[temp[0]];

            // Update the buffer
            table[temp[0]] = table[i];
            table[i] = (short) temp[2];
        }

        temp[0] = 0;
        short[] key = new short[6];
        for(int i = 0; i < this.securekey.length; i++) {
            // Add the next char to the array
            key[i] = (byte) this.securekey[i];

            temp[0] = (temp[0] + key[i] + 1) & 255;
            temp[2] = table[temp[0]];

            temp[1] = (temp[1] + temp[2]) & 255;
            temp[3] = table[temp[1]];

            table[temp[1]] = (short) temp[2];
            table[temp[0]] = (short) temp[3];

            // XOR the Buffer
            key[i] ^= table[(temp[2] + temp[3]) & 255];
        }

        switch(this.type) {
            case ADVANCED1:
                for(short i = 0; i < this.securekey.length; i++) {
                    key[i] = enctype1_data[key[i]];
                }
                break;
            case ADVANCED2:
                for(short i = 0; i < this.securekey.length; i++) {
                    key[i] ^= this.gamekey[i % this.gamekey.length];
                }
                break;
            default:
                break;
        }

        int length = this.securekey.length / 3;
        StringBuilder sb = new StringBuilder();

        int j = 0;
        while(length >= 1) {
            length--;

            temp[2] = key[j];
            temp[3] = key[j + 1];

            sb.append(addChar(temp[2] >> 2));
            sb.append(addChar((temp[2] & 3) << 4 | (temp[3] >> 4)));

            temp[2] = key[j + 2];

            sb.append(addChar((temp[3] & 15) << 2 | (temp[2] >> 6)));
            sb.append(addChar(temp[2] & 63));

            j += 3;
        }

        this.validateString = sb.toString();
        return this.validateString;
    }

    private char addChar(int value) {
        char out = 0;
        if(value < 26) {
            out = (char) (value + 65);
        } else if(value < 52) {
            out = (char) (value + 71);
        } else if(value < 62) {
            out = (char) (value - 4);
        } else if(value == 62) {
            out = '+';
        } else if(value == 63) {
            out = '/';
        }
        return out;
    }
}
