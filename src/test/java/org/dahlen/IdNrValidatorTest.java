package org.dahlen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IdNrValidatorTest {

    public static final String[] VALID_IDNR = {
            "86095742719",
            "47036892816",
            "65929970489",
            "57549285017",
            "25768131411"
    };

    public static final String[] INVALID_IDNR = {
            (String) null,
            "",
            "           ",
            "02476291358",
            "01234567890",
            "abcdefghijk",
            "25768131412",
            "22558131412",
            "22234567892"
    };

    @Test
    void testIsValidIdNr() {

        for (String valid : VALID_IDNR) {
            assertTrue(IdNrValidator.isValidIdNr(valid),
                    String.format("Valid IdNr '%s' is falsely reported invalid!", valid));
        }

        for (String invalid : INVALID_IDNR) {
            assertFalse(IdNrValidator.isValidIdNr(invalid),
                    String.format("Invalid IdNr '%s' is falsely reported valid!", invalid));
        }

    }
}
