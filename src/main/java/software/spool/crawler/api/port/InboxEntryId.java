package software.spool.crawler.api.port;

/**
 * Immutable value object representing the identifier of an entry stored in the
 * inbox.
 *
 * <p>
 * Use {@link #of(String)} as a named constructor to obtain instances.
 * </p>
 *
 * @param value the raw string identifier; must not be {@code null}
 */
public record InboxEntryId(String value) {
    /**
     * Creates a new {@code InboxEntryId} wrapping the given string value.
     *
     * @param id the raw identifier string; must not be {@code null}
     * @return a new {@code InboxEntryId} instance
     */
    public static InboxEntryId of(String id) {
        return new InboxEntryId(id);
    }
}
