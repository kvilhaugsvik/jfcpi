package org.freeciv.types;

import org.freeciv.packet.fieldtype.IllegalNumberOfElementsException;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

/**
 * A vector of String values.
 *
 * Can be serialized and deserialized from a byte array.
 */
public class StringVector extends Vector<String> {
    /**
     * Decode a serialized StringVector
     * @param from the serialized StringVector
     * @param ender value that ends the StringVector
     * @param separator value that separates each String
     * @param charset the charset encoding
     */
    public StringVector(final byte[] from,
                        final byte ender, final byte separator,
                        final Charset charset) {
        super();

        int start_pos = 0;
        int c_pos = 0;

        while (true) {
            if (endOfSerialized(from, ender, c_pos)
                    || from[c_pos] == separator) {
                /* This ends a full string. */
                this.add(new java.lang.String(from, start_pos, c_pos - start_pos, charset));

                /* Don't include the separator in the next string. */
                start_pos = c_pos + 1;
            }

            if (endOfSerialized(from, ender, c_pos)) {
                /* Done decoding */
                break;
            }

            c_pos++;
        }
    }

    /**
     * Create a new StringVector with the specified content.
     * @param content the strings of the vector.
     */
    public StringVector(String... content) {
        super(Arrays.asList(content));
    }

    /**
     * Check if the end of the serialized string vector has been reached.
     * @param from the serialized string vector.
     * @param ender string vector ender.
     * @param c_pos current position in the serialized string vector.
     * @return true iff at the end of the serialized string vector
     */
    private static boolean endOfSerialized(byte[] from, byte ender, int c_pos) {
        return c_pos == from.length || from[c_pos] == ender;
    }

    /**
     * Get this StringVector as an array of bytes.
     * Useful when sending it over the network.
     * @param sizeLimit max size of the encoded strvec.
     * @param ender signals the end of the encoded strvec.
     * @param separator separates two strings in the strvec.
     * @param charset the charset to use when encoding the strvec.
     * @return this StringVector as an array of bytes.
     * @throws IllegalNumberOfElementsException when the encoded strvec is larger than allowed.
     */
    public byte[] getAsByteArray(final int sizeLimit,
                                 final byte ender, final byte separator,
                                 final Charset charset) throws IllegalNumberOfElementsException {
        byte[] out = new byte[sizeLimit];
        int out_pos = 0;

        for (String elem : this) {
            final byte[] bytes = elem.getBytes(charset);

            if (sizeLimit < out_pos + bytes.length) {
                /* Size limit violated. */
                throw new IllegalNumberOfElementsException(sizeLimit
                        + " bytes is not enough space.");
            }

            /* Copy the encoded string */
            System.arraycopy(bytes, 0, out, out_pos, bytes.length);
            out_pos += bytes.length;

            if (out_pos < sizeLimit) {
                /* End the current string with the separator. */
                out[out_pos] = separator;
                out_pos++;
            }
        }

        if (out_pos == sizeLimit) {
            /* Not enough room for strvec terminator. */

            return Arrays.copyOf(out, out_pos);
        } else {
            /* String vector terminator included. */

            /* Replace final string separator with strvec ender. */
            out[out_pos - 1] = ender;

            return Arrays.copyOf(out, out_pos);
        }
    }
}
