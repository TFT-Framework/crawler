package software.spool.crawler.api;

import software.spool.crawler.internal.utils.factory.Transformer;

/**
 * Describes a named data-processing pipeline that converts raw source data into
 * serialized string records suitable for inbox storage.
 *
 * <p>
 * A {@code ProcessorFormat} combines three processing stages:
 * <ol>
 * <li><b>Deserialization</b> – converts the raw type {@code R} into an
 * intermediate parsed type {@code P}.</li>
 * <li><b>Splitting</b> – expands the parsed value {@code P} into zero or more
 * typed records of type {@code T} (e.g. individual elements of a JSON
 * array).</li>
 * <li><b>Serialization</b> – converts each record {@code T} into a
 * {@code String} payload ready to be stored in the inbox.</li>
 * </ol>
 *
 * <p>
 * Pre-built formats are available in
 * {@link software.spool.crawler.api.Formats}.
 * Custom formats can be created by implementing this interface:
 * </p>
 * 
 * <pre>{@code
 * ProcessorFormat<String, MyType, MyRecord> MY_FORMAT = () -> Transformer.of(MyDeserializer::new, MySplitter::new,
 *         MySerializer::new);
 * }</pre>
 *
 * @param <R> the raw source type produced by the
 *            {@link software.spool.crawler.api.source.PollSource}
 * @param <P> the intermediate parsed type after deserialization
 * @param <T> the individual record type produced by the splitter
 */
public interface ProcessorFormat<R, P, T> {

    /**
     * Returns a fully configured {@link Transformer} that can be applied to raw
     * data read from a source.
     *
     * @return a {@link Transformer} wiring together the deserializer, splitter, and
     *         serializer for this format
     */
    Transformer<R, P, T> pipeline();
}
