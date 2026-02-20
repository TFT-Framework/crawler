package es.ulpgc.eii.spool.domain;

public record SchemaVersion(String value) {
    public SchemaVersion(String value) {
        this.value = validate(value) ? value : null;
    }

    private static boolean validate(String value) {
        return true;
    }

    public static SchemaVersion of(String value) {
        return new SchemaVersion(value);
    }
}
