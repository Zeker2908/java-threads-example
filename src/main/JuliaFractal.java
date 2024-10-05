package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.Random;

public class JuliaFractal extends JPanel {

    private final double ZOOM = 1;    // Масштаб
    private final double cx;                // Параметр Cx для множества Жюлиа
    private final double cy;                // Параметр Cy для множества Жюлиа
    private int currentMaxIter = 1;   // Текущее максимальное количество итераций
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int MAX_FINAL_ITER = 300; // Максимальное количество итераций
    private static final int WIDTH = (int) screenSize.getWidth();
    private static final int HEIGHT = (int) screenSize.getHeight();

    private final BufferedImage image; // Изображение для отрисовки

    public void incIter(){
        currentMaxIter++;
    }

    public JuliaFractal() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Random random = new Random();
        cx = -1 + 1 * random.nextDouble(); // Случайное значение в диапазоне [-2, 2]
        cy = -1 + 2 * random.nextDouble(); // Случайное значение в диапазоне [-2, 2]
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // Рисуем всё изображение сразу
    }

    private void drawJuliaSet() {
        int width = image.getWidth();
        int height = image.getHeight();
        int pullSize = Runtime.getRuntime().availableProcessors();
        try {
            ExecutorService executor = Executors.newFixedThreadPool(pullSize);

            for (int t = 0; t < 12; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    for (int y = threadId; y < height; y += pullSize) {
                        for (int x = 0; x < width; x++) {
                            int iter = currentMaxIter; // Используем текущее количество итераций
                            double zx = 1.5 * (x - (double) width / 2) / (0.5 * ZOOM * width);
                            double zy = (y - (double) height / 2) / (0.5 * ZOOM * height);

                            while (zx * zx + zy * zy < 4 && iter > 0) {
                                double tmp = zx * zx - zy * zy + cx;
                                zy = 2.0 * zx * zy + cy;
                                zx = tmp;
                                iter--;
                            }

                            // Вычисление градации серого
                            int gray = (int) (255 * (iter / (double) currentMaxIter));
                            int color = new Color(gray, gray, gray).getRGB();
                            image.setRGB(x, y, color); // Устанавливаем цвет пикселя
                        }
                    }
                });
            }

            // Ожидание завершения всех потоков
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Julia Fractal");
        JuliaFractal panel = new JuliaFractal();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(panel);
        frame.setVisible(true);

        // Таймер для постепенного увеличения итераций
        Timer timer = new Timer(1, e -> {
            if (panel.currentMaxIter < panel.MAX_FINAL_ITER) {
                panel.incIter(); // Увеличиваем количество итераций
                panel.drawJuliaSet(); // Перерисовываем с обновлённым количеством итераций
                panel.repaint(); // Перерисовываем панель с новым изображением
                frame.setTitle("Julia Fractal " + panel.currentMaxIter);
            } else {
                ((Timer) e.getSource()).stop(); // Остановить таймер, когда достигли максимума
            }
        });

        timer.start(); // Запуск анимации
    }
}
