package software.spool.crawler.example.filesystem;

import software.spool.crawler.example.filesystem.application.Application;
import software.spool.crawler.example.filesystem.domain.model.OrderReceived;

public class Main {
    public static void main(String[] args) {
        new Application().run();
        // System.out.println(OrderReceived.class.toString());
    }
}