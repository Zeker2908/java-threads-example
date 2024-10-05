package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooksExecutor {
    private final List<Path> files;
    private final AtomicLong countSymbol;

    public BooksExecutor(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (Stream<Path> fileStream = Files.walk(directory)) {
                files = fileStream.filter(Files::isRegularFile).collect(Collectors.toList());
            }
            countSymbol = new AtomicLong(0);
            countSymbolThread();
        } else {
            throw new IllegalArgumentException("Invalid directory path.");
        }
    }

    private void countSymbolThread() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Path file : files) {
            executorService.submit(() -> countSymbolsInFile(file));
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void countSymbolsInFile(Path file) {
        try (Stream<String> lines = Files.lines(file)) {
            countSymbol.addAndGet(lines.mapToInt(String::length).sum());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Path> getFiles() {
        return files;
    }

    public long getCountSymbol() {
        return countSymbol.get();
    }

    public static void main(String[] args) throws IOException {
        double timeToStart = System.currentTimeMillis();
        BooksExecutor booksExecutor = new BooksExecutor(
                Path.of("C:\\Users\\Leonid\\Downloads\\books"));
        double timeToEnd = System.currentTimeMillis();
        System.out.println("Количество символов: " + booksExecutor.getCountSymbol()+"\nВремя затраченное на выполнение "+(timeToEnd - timeToStart)/1000+" s");
    }
}
