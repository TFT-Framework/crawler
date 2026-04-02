package software.spool.crawler;

import software.spool.core.model.spool.SpoolNode;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.dsl.CrawlerDSL;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Crawler> crawlers = CrawlerDSL.fromDescriptor("/Crawler.yaml").buildAll();
        SpoolNode node = SpoolNode.create();
        crawlers.forEach(node::register);
        node.start();
    }
}
 