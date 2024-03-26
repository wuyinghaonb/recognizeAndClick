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
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.ImageTool.calcPositionObject;
import static org.example.ImageTool.convertToGrayscale;


public class Main {

    private final static int WAIT_ROBOT_INTERVAL = 10;
    private static AtomicInteger i = new AtomicInteger(0);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4); // 假设我们需要两个线程
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
        BufferedImage screen = ImageIO.read(ImageTool.class.getResource("/01.png"));;

        PositionObject p = calcPositionObject(screen, a);
        if (p == null) {
            throw new Exception("未识别到游戏");
        }

        //     ImageIO.write(targetField, "png", new File("./screen.png"));
        int totalCount = 0;
        while (true) {
            //词条区(45,230) (285,410)
//            BufferedImage b = ImageTool.getScreen(robot);
            BufferedImage b = ImageIO.read(ImageTool.class.getResource("/01.png"));
            // todo 要么反复截屏计算哈希感知变化，要么设置延迟
//            BufferedImage t0 = b.getSubimage(p.getX() + 45, p.getY() + 230, 240, 53);
            BufferedImage targetField1 = b.getSubimage(p.getX() + 45, p.getY() + 230, 240, 40);
            BufferedImage targetField2 = b.getSubimage(p.getX() + 45, p.getY() + 270, 240, 44);
            BufferedImage targetField3 = b.getSubimage(p.getX() + 45, p.getY() + 314, 240, 43);
            BufferedImage targetField4 = b.getSubimage(p.getX() + 45, p.getY() + 357, 240, 53);
            try {
                // 异步任务
                CompletableFuture<Integer> future1 = countAsync(targetField1);
                CompletableFuture<Integer> future2 = countAsync(targetField2);
                CompletableFuture<Integer> future3 = countAsync(targetField3);
                CompletableFuture<Integer> future4 = countAsync(targetField4);

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
        // 黑白
        targetField = convertToGrayscale(targetField);
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

    public static CompletableFuture<Integer> countAsync(BufferedImage targetField) {
        // 异步执行OCR并返回CompletableFuture，使用类共享的线程池
        return CompletableFuture.supplyAsync(() -> {
            try {
                return count(targetField); // 你的count方法
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}