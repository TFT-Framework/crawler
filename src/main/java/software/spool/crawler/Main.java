package software.spool.crawler;

import software.spool.crawler.dsl.SpoolNodeDSL;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        SpoolNodeDSL.fromDescriptor("/Crawler.yaml");
    }
}
 