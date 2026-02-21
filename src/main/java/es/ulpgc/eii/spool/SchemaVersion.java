package es.ulpgc.eii.spool;

/**
 * A validated value object representing the semantic version of an event's payload schema.
 *
 * <p>{@code SchemaVersion} enforces the semver format {@code MAJOR.MINOR.PATCH}
 * (e.g. {@code "1.0.0"}, {@code "2.3.1"}) at construction time, preventing
 * malformed versions from propagating through the system.</p>
 *
 * <p>Use the static factory method {@link #of(String)} as the preferred way
 * to create instances:</p>
 * <pre>{@code
 * SchemaVersion version = SchemaVersion.of("1.0.0");
 * }</pre>
 *
 * <p>As a Java {@code record}, instances are immutable and compare by value.</p>
 *
 * @param value the raw semver string (e.g. {@code "1.0.0"}); must not be {@code null}
 *              and must match the pattern {@code \d+\.\d+\.\d+}
 * @see Event#schemaVersion()
 * @since 1.0.0
 */
public record SchemaVersion(String value) {

    /**
     * Compact canonical constructor that validates the semver format.
     *
     * @param value the raw version string to validate
     * @throws IllegalArgumentException if {@code value} is {@code null} or does not
     *                                  match the {@code MAJOR.MINOR.PATCH} pattern
     */
    public SchemaVersion(String value) {
        if (!isValid(value))
            throw new IllegalArgumentException("SchemaVersion must follow semver (e.g. '1.0.0'): " + value);
        this.value = value;
    }

    /**
     * Returns {@code true} if the given string is a valid semver version.
     *
     * <p>A valid version must be non-null and match the regex {@code \d+\.\d+\.\d+},
     * i.e. three dot-separated non-negative integers.</p>
     *
     * @param value the string to validate
     * @return {@code true} if {@code value} is a valid semver string, {@code false} otherwise
     */
    private static boolean isValid(String value) {
        return value != null && value.matches("\\d+\\.\\d+\\.\\d+");
    }

    /**
     * Static factory method for creating a {@code SchemaVersion} from a string.
     *
     * <p>Preferred over the constructor for more expressive, readable code:</p>
     * <pre>{@code
     * SchemaVersion v = SchemaVersion.of("1.0.0");
     * }</pre>
     *
     * @param value the semver string (e.g. {@code "1.0.0"})
     * @return a new {@code SchemaVersion} instance
     * @throws IllegalArgumentException if {@code value} is {@code null} or not a valid semver
     */
    public static SchemaVersion of(String value) {
        return new SchemaVersion(value);
    }
}
