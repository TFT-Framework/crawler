package software.spool.crawler.internal.utils;

public interface ProcessorFormat<R, P, T> {
    Transformer<R, P, T> pipeline();
}
