package org.dahlen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks a German tax identification number 
 * (<q>steuerliche Identifikationsnummer</q>, <em>IdNr</em>) for formal
 * correctness. This concerns both the length (11 digits) and the content of the
 * actual ID number (digits 1-10) and the correct check digit (digit 11).
 * 
 * <p>
 * The check takes place along the documented, publicly-available rules (see
 * references).
 * </p>
 * 
 * <p>
 * The check does not indicate whether the ID number is currently assigned and
 * to whom it is assigned.
 * </p>
 * 
 * <p>
 * <em>IdNr</em><br>
 * The tax identification number is an eleven-digit sequence of digits, the
 * 11th digit being a check digit. The tax identification number must meet
 * the following criteria:
 * </p>
 * 
 * <ul>
 * <li>No leading zeros allowed.
 * <li>The 11th digit is a check digit and is calculated from digits 1 - 10
 * according to a documented algorithm (see below)
 * <li>In the first 10 digits of the identification number, exactly one digit
 * must
 * be duplicated or triplicated.
 * <li>If there are 3 identical digits in positions 1 to 10, these identical
 * digits must never appear in directly consecutive
 * positions.
 * </ul>
 * 
 * @see <a href="https://www.zfa.deutsche-rentenversicherung-bund.de/de/Inhalt/public/4_ID/47_Pruefziffernberechnung/001_Pruefziffernberechnung.pdf">Deutsche Rentenversicherung: Prüfziffernberechnung</a>
 * @see <a href=
 *      "https://download.elster.de/download/schnittstellen/Pruefung_der_Steuer_und_Steueridentifikatsnummer.pdf">Elster: Prüfung der Steuer- und Steueridentifikationsnummer</a>
 */
public class IdNrValidator {

    /** Absolute length of the entire IdNr. */
    public static final int IDNR_LENGTH = IdNrValidator.IDNR_NUMBER_LENGTH
            + IdNrValidator.IDNR_CHECKSUM_LENGTH;

    /** Length of the "number" part of the IdNr. (without check digit) */
    public static final int IDNR_NUMBER_LENGTH = 10;

    /** Length of the checksum / check digit */
    public static final int IDNR_CHECKSUM_LENGTH = 1;

    /**
     * Enumeration of possible validation errors, reported by
     * {@link IdNrValidator#validateIdNr(String)}.
     * 
     */
    public enum ValidationError {
        /** Given checksum does not match computed checksum */
        CHECKSUM_MISSMATCH,
        /** Given IdNr is <code>null</code> */
        IDNR_IS_NULL,
        /** Given IdNr is of unexpected length */
        IDNR_LENGTH_MISSMATCH,
        /** Given IdNr is of unexpected content (digits-only, no leading zero) */
        IDNR_FORMAT_MISSMATCH,
        /** Given IdNr-number contains triple digit sequence (like 111...) */
        NUMBER_INVALID_3_DIGITS_SEQUENCE,
        /** Given IdNr-number contains same digit more than 3 times (like 1111... */
        NUMBER_TO_MANY_OCCURENCES_OF_SAME_DIGIT,
        /**
         * Given IdNr-number contains more than one triple of same digit (like
         * 111222...)
         */
        NUMBER_TO_MANY_TRIPLE_OCCURENCES,
        /**
         * Given IdNr-number contains more than one double of same digit (like 1122...)
         */
        NUMBER_TO_MANY_DOUBLE_OCCURENCES, 
        /**
         * Given IdNr-number does not contain double or triple occurence of same digit
         */
        NUMBER_NO_OCCURENCES_OF_SAME_DIGIT;

    };

    // Singleton instance
    private static final IdNrValidator SINGLETON = new IdNrValidator();

    // RegEx to find tripple sequence of same number
    private static final Pattern PATTERN_3_DIGITS_SEQUENCE = Pattern.compile("(.)\\1\\1");

    // Log
    private static final Logger LOG = LoggerFactory.getLogger(IdNrValidator.class);

    /**
     * <p>Checks the given IdNr. for validity.</p>
     * <p>The method will call {@link #validateIdNr(String)} and check the outcome. If no
     * {@link IdNrValidator.ValidationError}s are reported, the given IdNr. is considered
     * <q>valid</q>.</p>
     * 
     * @param idnr IdNr. to check
     * @return <code>true</code>, if validation did not return any errors,
     *         <code>false</code> otherwise
     * @see #validateIdNr(String)
     */
    public static boolean isValidIdNr(final String idnr) {

        Set<ValidationError> errors = IdNrValidator.validateIdNr(idnr);

        if (errors.isEmpty()) {
            LOG.info("IdNr \"{}\" is valid.", idnr);
        } else {
            LOG.warn("IdNr {} is invalid, errors {}!",
                    String.format("\"%s\"", idnr),
                    errors);
        }

        return errors.isEmpty();
    }

    /**
     * Validates the given IdNr. and returns a Set of found validation
     * errors. 
     * 
     * <p>The given IdNr can be considered <q>valid</q>, if the
     * returned Set is empty.</p>
     * 
     * <p>The validation will immediately return if one of the following
     * {@link IdNrValidator.ValidationError}s were found:
     * </p>
     * 
     * <ul>
     * <li>{@link IdNrValidator.ValidationError#IDNR_IS_NULL}
     * <li>{@link IdNrValidator.ValidationError#IDNR_LENGTH_MISSMATCH}
     * <li>{@link IdNrValidator.ValidationError#IDNR_FORMAT_MISSMATCH}
     * </ul>
     * 
     * <p>
     * Otherwise, validation will continue and return all errors that
     * have occured.
     * </p>
     * 
     * @param idnr IdNr to check.
     * @return Set of found errors (if any).
     * @see IdNrValidator.ValidationError
     * @see #isValidIdNr(String)
     */
    public static Set<ValidationError> validateIdNr(final String idnr) {

        if (idnr == null) {
            return Set.of(ValidationError.IDNR_IS_NULL);
        }

        // 11-digits
        if (idnr.length() != IDNR_LENGTH) {
            return Set.of(ValidationError.IDNR_LENGTH_MISSMATCH);
        }

        // digits only, no leading zero
        if (!idnr.matches("^[1-9][0-9]{10}$")) {
            return Set.of(ValidationError.IDNR_FORMAT_MISSMATCH);
        }

        final String idnrNumber = idnr.substring(0, IDNR_NUMBER_LENGTH);
        final String idnrChecksum = idnr.substring(IDNR_NUMBER_LENGTH, 11);

        Set<ValidationError> errors = new HashSet<>();

        errors.addAll(SINGLETON.doValidateIdNr(idnrNumber));

        if (!SINGLETON.isValidIdNrChecksum(idnrNumber, Integer.valueOf(idnrChecksum).intValue())) {
            errors.add(ValidationError.CHECKSUM_MISSMATCH);
        }

        return errors;

    }

    /*
     * Private constructor, preventing outside instantiation.
     */
    private IdNrValidator() {

    }

    /*
     * Validates the "number" part of the IdNr (digits 1 to 10). Returns
     * all validation errors found.
     */
    private Set<ValidationError> doValidateIdNr(final String idnrNumber) {

        Objects.requireNonNull(idnrNumber);

        Set<ValidationError> errors = new HashSet<>();

        // 10-digits, no leading zero.
        if (!idnrNumber.matches("^[1-9][0-9]{9}$")) {
            errors.add(ValidationError.IDNR_FORMAT_MISSMATCH);
        }

        // No triple digit sequence.
        if (PATTERN_3_DIGITS_SEQUENCE.matcher(idnrNumber).find()) {
            errors.add(ValidationError.NUMBER_INVALID_3_DIGITS_SEQUENCE);
        }

        /*
         * Count occurences of each digit.
         */
        final Map<Integer, Integer> digits = new HashMap<>();

        idnrNumber
                .codePoints()
                .map(codePoint -> codePoint - '0')
                .forEach(digit -> digits.put(digit, Optional.ofNullable(digits.get(digit)).orElse(0) + 1));

        LOG.debug("IdNrNumber {}, digits distribution {}.", idnrNumber, digits);

        switch (digits.size()) {
            case 8:
                // only one triple allowed, causing lack of two other digits
                if (digits.values().stream().filter(d -> d == 3).count() != 1) {
                    errors.add(ValidationError.NUMBER_TO_MANY_TRIPLE_OCCURENCES);
                }
                break;
            case 9:
                // only one double allowed, causing lack of one other digits
                if (digits.values().stream().filter(d -> d == 2).count() != 1) {
                    errors.add(ValidationError.NUMBER_TO_MANY_DOUBLE_OCCURENCES);
                }
                break;
            case 10:
                // no double or triple, not okay
                errors.add(ValidationError.NUMBER_NO_OCCURENCES_OF_SAME_DIGIT);
                break;
            default:
                // too many occurences (3+) of same digit
                errors.add(ValidationError.NUMBER_TO_MANY_OCCURENCES_OF_SAME_DIGIT);
        }

        return errors;

    }

    /*
     * Computes the check digit of the given "number" part of the IdNr and
     * compares it to the given one.
     */
    private boolean isValidIdNrChecksum(final String idnrNumber, final int idnrCheckDigit) {

        Objects.requireNonNull(idnrNumber);

        int p = idnrNumber
                .codePoints()
                .map(codePoint -> codePoint - '0')
                .reduce(IDNR_NUMBER_LENGTH, (product, digit) -> {
                    int sum = (digit + product) % IDNR_NUMBER_LENGTH;
                    if (sum == 0) {
                        sum = IDNR_NUMBER_LENGTH;
                    }
                    product = (2 * sum) % IDNR_LENGTH;

                    return product;
                });

        int computedChecksum = IDNR_LENGTH - p;

        if (computedChecksum != idnrCheckDigit) {
            LOG.warn("IdNrNumber {}, check digit missmatch (given: {}, computed: {})!", idnrNumber, idnrCheckDigit,
                    computedChecksum);
        }
        return computedChecksum == idnrCheckDigit;
    }

}
