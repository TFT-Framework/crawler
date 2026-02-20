package es.ulpgc.eii.spool.domain;

public record SchemaVersion(String value) {

    public SchemaVersion(String value) {
        if (!isValid(value))
            throw new IllegalArgumentException("SchemaVersion must follow semver (e.g. '1.0.0'): " + value);
        this.value = value;
    }

    private static boolean isValid(String value) {
        return value != null && value.matches("\\d+\\.\\d+\\.\\d+");
    }

    public static SchemaVersion of(String value) {
        return new SchemaVersion(value);
    }
}
