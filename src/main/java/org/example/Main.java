package org.example;

import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.example.ImageTool.calcPositionObject;


public class Main {
    public static void main(String[] args) throws Exception {

        // 识别工具
        ITesseract instance = new Tesseract();
        ClassLoader classLoader = Main.class.getClassLoader();
        File tessDataFolder = new File(classLoader.getResource("tessdata").getFile());
        instance.setDatapath(tessDataFolder.getPath());
        instance.setLanguage("chi_sim");

        // 左上角图像
        BufferedImage a = ImageIO.read(ImageTool.class.getResource("/target.png"));
        // 模拟截屏，最终需要替换为截屏语句   BufferedImage screen = getScreen(robot);
        BufferedImage screen = ImageIO.read(ImageTool.class.getResource("/05.png"));
        // 左上角（0，0）
        PositionObject p = calcPositionObject(screen, a);
        if (p == null) {
            throw new Exception("未识别到游戏");
        }

        Robot robot = new Robot();
        //词条区(450,230) (285,410)
        Rectangle captureArea = new Rectangle(p.getX() + 450, p.getY() + 230, p.getX() + 285, p.getX() + 410);
        BufferedImage targetField = robot.createScreenCapture(captureArea);
        ImageIO.write(targetField, "png", new File("./screen.png"));
        try {
            // 进行OCR识别
            String result = instance.doOCR(targetField);
            System.out.println(result);
            // 获取每个词的坐标
            List<Word> words = instance.getWords(targetField, ITessAPI.TessPageIteratorLevel.RIL_WORD);
            for (Word word : words) {
                System.out.println(word.getText() + " - Rect: " + word.getBoundingBox());
            }
        } catch (TesseractException e) {
            throw new Exception(e.getMessage());
        }
        // 暂存截屏 无用
//        ImageIO.write(image, "png", new File("./screen.png"));

        //     BufferedImage image = ImageIO.read(ImageTool.class.getResource("/demo.jpg"));


//        if (p != null) {
//            robot.setAutoDelay(500);
//            robot.mouseMove(p.getX(), p.getY());
//        }

        //        ImageIO.write(image, "png", new File("./screen.png"));
    }
}