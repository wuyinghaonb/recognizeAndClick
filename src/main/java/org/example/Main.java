package org.example;

import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {

    private final static int WAIT_ROBOT_INTERVAL = 10;
    private static final AtomicInteger i = new AtomicInteger(0);
    private static final AtomicBoolean screenChanged = new AtomicBoolean(false);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final ThreadLocal<BufferedImage> threadLocalLastImage = new ThreadLocal<>();
    private static final ThreadLocal<Robot> threadLocalRobot = ThreadLocal.withInitial(() -> {
        try {
            return new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    });
    private static final ThreadLocal<ITesseract> threadLocalTesseract = ThreadLocal.withInitial(() -> {
        ITesseract instance = new Tesseract();
        // 识别工具
        // 获取JAR文件所在目录的路径
        try {
            String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent();
            // 构造tessdata目录的完整路径
            String tessDataPath = jarDir + File.separator + "tessdata";
            // 设置Tesseract的数据路径
            instance.setDatapath(tessDataPath);
            instance.setLanguage("chi_sim");
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });

    public static void main(String[] args) throws Exception {

        // 左上角图像
        BufferedImage a = ImageIO.read(Objects.requireNonNull(ImageTool.class.getResource("/target.png")));

        // 左上角（0，0）
        Robot robot = new Robot();
//        BufferedImage screen = ImageTool.getScreen(robot);
        BufferedImage screen = ImageIO.read(Objects.requireNonNull(ImageTool.class.getResource("/01.png")));

        PositionObject p = ImageTool.calcPositionObject(screen, a);
        if (p == null) {
            throw new Exception("未识别到游戏");
        }

        //     ImageIO.write(targetField, "png", new File("./screen.png"));
        int totalCount = 0;
        while (true) {
            //词条区(45,230) (285,410)
//            BufferedImage b = ImageTool.getScreen(robot);
            //           BufferedImage b = ImageIO.read(Objects.requireNonNull(ImageTool.class.getResource("/01.png")));
            // todo 要么反复截屏计算哈希感知变化，要么设置延迟
//            BufferedImage t0 = b.getSubimage(p.getX() + 45, p.getY() + 230, 240, 53);

            try {
                // 异步任务
                CompletableFuture<Integer> future1 = countAsync(p.getX() + 45, p.getY() + 230, p.getX() + 285, p.getY() + 270);
                CompletableFuture<Integer> future2 = countAsync(p.getX() + 45, p.getY() + 270, p.getX() + 285, p.getY() + 314);
                CompletableFuture<Integer> future3 = countAsync(p.getX() + 45, p.getY() + 314, p.getX() + 285, p.getY() + 357);
                CompletableFuture<Integer> future4 = countAsync(p.getX() + 45, p.getY() + 357, p.getX() + 285, p.getY() + 410);

                // 任务都完成后，累加结果
                CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3, future4)
                        .thenRun(() -> {
                            i.addAndGet(future1.join());
                            i.addAndGet(future2.join());
                            i.addAndGet(future3.join());
                            i.addAndGet(future4.join());
                        });
                // 等待所有任务完成
                combinedFuture.join();

                System.out.println("识别到" + i + "条攻击");
                if (i.get() < 2) {
                    totalCount++;
                    System.out.println("祈愿第" + totalCount + "次");
                    robot.mouseMove(p.getX() + 770, p.getY() + 700);
                    robot.delay(WAIT_ROBOT_INTERVAL);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.delay(WAIT_ROBOT_INTERVAL);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                    // 等待屏幕变化
                    CompletableFuture<Boolean> f1 = screenCompare(p.getX() + 45, p.getY() + 230, p.getX() + 285, p.getY() + 270);
                    CompletableFuture<Boolean> f2 = screenCompare(p.getX() + 45, p.getY() + 270, p.getX() + 285, p.getY() + 314);
                    CompletableFuture<Boolean> f3 = screenCompare(p.getX() + 45, p.getY() + 314, p.getX() + 285, p.getY() + 357);
                    CompletableFuture<Boolean> f4 = screenCompare(p.getX() + 45, p.getY() + 357, p.getX() + 285, p.getY() + 410);

                    CompletableFuture<?> f = CompletableFuture.anyOf(f1, f2, f3, f4);
                    f.join();

                } else {
                    robot.mouseMove(0, 0);
                    robot.delay(500);
                    break;
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    public static int count(BufferedImage targetField) throws Exception {
        ITesseract instance = threadLocalTesseract.get();
        int res = 0;
        String ocrResult = instance.doOCR(targetField);
        System.out.println(ocrResult);
        List<Word> l = instance.getWords(targetField, ITessAPI.TessPageIteratorLevel.RIL_WORD);
        for (Word word : l) {
            if (word.getText().contains("攻")) {
                res++;
            }
        }
        // 保存图片
        threadLocalLastImage.set(targetField);
        // 黑白
        targetField = ImageTool.convertToGrayscale(targetField);
        ocrResult = instance.doOCR(targetField);
        System.out.println(ocrResult);
        l = instance.getWords(targetField, ITessAPI.TessPageIteratorLevel.RIL_WORD);
        for (Word word : l) {
            if (word.getText().contains("攻")) {
                res++;
            }
        }
        if (res > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    // 统计攻击
    public static CompletableFuture<Integer> countAsync(int a, int b, int c, int d) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Robot robot = threadLocalRobot.get();
                BufferedImage targetField = robot.createScreenCapture(new Rectangle(a, b, c, d));
                return count(targetField);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    // 检测变化
    public static CompletableFuture<Boolean> screenCompare(int a, int b, int c, int d) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Robot robot = threadLocalRobot.get();
                BufferedImage lastImage = threadLocalLastImage.get();

                while (!screenChanged.get() && !Thread.currentThread().isInterrupted()) {
                    BufferedImage currentImage = robot.createScreenCapture(new Rectangle(a, b, c, d));
                    if (lastImage != null && !ImageTool.areImagesEqual(lastImage, currentImage)) {
                        screenChanged.set(true);
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}