package hanu.a2_2001040004.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Constants {
    public static final ExecutorService executorService = Executors.newFixedThreadPool(4);
}
