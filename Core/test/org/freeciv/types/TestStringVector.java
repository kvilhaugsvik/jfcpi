package org.freeciv.types;

import org.freeciv.packet.fieldtype.IllegalNumberOfElementsException;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test the StringVector data type.
 */
public class TestStringVector {
    /*---------------------------------------------------------------------
      Test data
    ---------------------------------------------------------------------*/

    /**
     * An encoded strvec containing "".
     */
    private final static byte[] STRVEC_EMPTY = {
            /* End of elements */
            0x00,
    };

    /**
     * An encoded strvec containing one string.
     */
    private final static byte[] STRVEC_ONE_ELEM = {
            /* First string */
            'o', 'n', 'e',
            /* End of elements (string vector) */
            0x00,
    };

    /**
     * An encoded strvec containing three strings.
     */
    private final static byte[] STRVEC_THREE_ELEMS = {
            /* First string */
            '1',
            /* End of element (string) */
            0x03,
            /* Second string */
            't', 'w', 'o',
            /* End of element (string) */
            0x03,
            /* Third string */
            'o', 'n', 'e',
            /* End of elements (string vector) */
            0x00,
    };

    /**
     * An encoded strvec containing one string.
     */
    private final static byte[] STRVEC_ONE_ELEM_UNTERMINATED = {
            /* First string */
            'u', 'n', 't', 'e', 'r'
    };

    /**
     * An encoded strvec containing one string.
     */
    private final static byte[] STRVEC_TWO_ELEMS_UNTERMINATED = {
            /* First string */
            'u', 'n', 't', 'e', 'r',

            /* End of element (string) */
            0x03,

            /* Second string */
            'm', 'i', 'n', 'a', 't', 'e', 'd'
    };

    /*---------------------------------------------------------------------
      Test creation
    ---------------------------------------------------------------------*/

    /**
     * Test creating a strvec from the string it contains.
     */
    @Test public void create_from_values_one_string() {
        StringVector vec = new StringVector("one");

        assertEquals("Bad number of elements", 1, vec.size());
        assertEquals("Bad element", "one", vec.firstElement());
    }

    /**
     * Test creating a strvec from the three strings it contains.
     */
    @Test public void create_from_values_three_strings() {
        StringVector vec = new StringVector("1", "two", "one");

        assertEquals("Bad number of elements", 3, vec.size());

        assertEquals("Bad element", "1", vec.elementAt(0));
        assertEquals("Bad element", "two", vec.elementAt(1));
        assertEquals("Bad element", "one", vec.elementAt(2));
    }

    /*---------------------------------------------------------------------
      Test decoding
    ---------------------------------------------------------------------*/

    /**
     * Test decoding a strvec that only contains "".
     */
    @Test public void decode_empty() {
        StringVector vec = new StringVector(STRVEC_EMPTY,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));

        assertEquals("Bad number of elements", 1, vec.size());
        assertEquals("Bad element", "", vec.firstElement());
    }

    /**
     * Test decoding a strvec with a single element.
     */
    @Test public void decode_one_elem() {
        StringVector vec = new StringVector(STRVEC_ONE_ELEM,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));

        assertEquals("Bad number of elements", 1, vec.size());
        assertEquals("Bad element", "one", vec.firstElement());
    }

    /**
     * Test decoding a strvec with three elements.
     */
    @Test public void decode_three_elems() {
        StringVector vec = new StringVector(STRVEC_THREE_ELEMS,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));

        assertEquals("Bad number of elements", 3, vec.size());

        assertEquals("Bad element", "1", vec.elementAt(0));
        assertEquals("Bad element", "two", vec.elementAt(1));
        assertEquals("Bad element", "one", vec.elementAt(2));
    }

    /**
     * Test decoding an unterminated strvec with a single element.
     */
    @Test public void decode_unterminated_one_elem() {
        StringVector vec = new StringVector(STRVEC_ONE_ELEM_UNTERMINATED,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));

        assertEquals("Bad number of elements", 1, vec.size());
        assertEquals("Bad element", "unter", vec.firstElement());
    }

    /**
     * Test decoding an unterminated strvec with two elements.
     */
    @Test public void decode_unterminated_two_elems() {
        StringVector vec = new StringVector(STRVEC_TWO_ELEMS_UNTERMINATED,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));

        assertEquals("Bad number of elements", 2, vec.size());

        assertEquals("Bad element", "unter", vec.elementAt(0));
        assertEquals("Bad element", "minated", vec.elementAt(1));
    }

    /*---------------------------------------------------------------------
      Test encoding
    ---------------------------------------------------------------------*/

    /**
     * Test encoding a strvec containing a single string.
     *
     * There is enough space to terminate the string vector.
     */
    @Test public void encode_space_plenty_one_string() {
        StringVector vec = new StringVector("one");

        assertArrayEquals("Encoding not as expected",
                STRVEC_ONE_ELEM,
                vec.getAsByteArray(5,
                        (byte)'\0', (byte)'\3', Charset.forName("UTF-8")));
    }

    /**
     * Test encoding a strvec containing three strings.
     *
     * There is enough space to terminate the string vector.
     */
    @Test public void encode_space_plenty_three_strings() {
        StringVector vec = new StringVector("1", "two", "one");

        assertArrayEquals("Encoding not as expected",
                STRVEC_THREE_ELEMS,
                vec.getAsByteArray(11,
                        (byte)'\0', (byte)'\3', Charset.forName("UTF-8")));
    }

    /**
     * Test encoding a strvec containing a single string.
     *
     * There isn't enough space to terminate the string vector.
     */
    @Test public void encode_space_exact_one_string() {
        StringVector vec = new StringVector("unter");

        assertArrayEquals("Encoding not as expected",
                STRVEC_ONE_ELEM_UNTERMINATED,
                vec.getAsByteArray(5,
                        (byte)'\0', (byte)'\3', Charset.forName("UTF-8")));
    }

    /**
     * Test encoding a strvec containing two strings.
     *
     * There isn't enough space to terminate the string vector.
     */
    @Test public void encode_space_exact_two_strings() {
        StringVector vec = new StringVector("unter", "minated");

        assertArrayEquals("Encoding not as expected",
                STRVEC_TWO_ELEMS_UNTERMINATED,
                vec.getAsByteArray(13,
                        (byte)'\0', (byte)'\3', Charset.forName("UTF-8")));
    }

    /**
     * Test encoding a strvec containing a single string.
     *
     * There is enough space to terminate the string vector.
     */
    @Test(expected = IllegalNumberOfElementsException.class)
    public void encode_space_not_enough_one_string() {
        StringVector vec = new StringVector("This is too long");

        vec.getAsByteArray(5,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));
    }

    /**
     * Test encoding a strvec where a string separator is aligned with the
     * max size limit.
     *
     * There is enough space to terminate the string vector.
     */
    @Test(expected = IllegalNumberOfElementsException.class)
    public void encode_space_not_enough_string_separator_aligned() {
        StringVector vec = new StringVector("Don't", "ignore", "this");

        vec.getAsByteArray(5,
                (byte)'\0', (byte)'\3', Charset.forName("UTF-8"));
    }
}
