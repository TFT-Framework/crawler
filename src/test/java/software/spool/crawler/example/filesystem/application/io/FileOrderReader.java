package software.spool.crawler.example.filesystem.application.io;

import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.example.filesystem.domain.io.OrderReader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileOrderReader implements OrderReader, PollSource<String> {
    @Override
    public String poll() throws SpoolException {
        String resourcePath = "/part-000000.json";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            assert is != null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sourceId() {
        return "filesystem-products";
    }

    @Override
    public String read() {
        return poll();
    }
}
