package org.example;

import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Random;

import static org.example.ImageTool.calcPositionObject;
import static org.example.ImageTool.convertToGrayscale;


public class Main {

    private final static int TRY_COUNT = 10;
    private final static int WAIT_PAGE_LOAD_INTERVAL = 2000;
    private final static int WAIT_ROBOT_INTERVAL = 200;
    private final static ITesseract instance = new Tesseract();

    public static void main(String[] args) throws Exception {

        // 识别工具
        // 获取JAR文件所在目录的路径
        String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParent();
        // 构造tessdata目录的完整路径
        String tessDataPath = jarDir + File.separator + "tessdata";
        // 设置Tesseract的数据路径
        instance.setDatapath(tessDataPath);
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

        BufferedImage targetField1 = screen.getSubimage(p.getX() + 45, p.getY() + 230, 240, 40);
        BufferedImage targetField2 = screen.getSubimage(p.getX() + 45, p.getY() + 270, 240, 44);
        BufferedImage targetField3 = screen.getSubimage(p.getX() + 45, p.getY() + 314, 240, 43);
        BufferedImage targetField4 = screen.getSubimage(p.getX() + 45, p.getY() + 357, 240, 53);
        //     ImageIO.write(targetField, "png", new File("./screen.png"));
        while (true) {
            //词条区(45,230) (285,410)
//            Rectangle captureArea = new Rectangle(p.getX() + 45, p.getY() + 230, p.getX() + 285, p.getY() + 410);
//            BufferedImage targetField = robot.createScreenCapture(captureArea);
            try {
                // 获取每个词的坐标
                int i = 0;
                i += count(targetField1);
                i += count(targetField2);
                i += count(targetField3);
                i += count(targetField4);
                if(i >=2) {
                    break;
                }
                // 随机延迟
                Random r = new Random();
                int delay = r.nextInt(501);
                robot.delay(delay);

                int random = (int)(Math.random()*51)-25;
                robot.mouseMove(p.getX() + 770 + random, p.getY() + 700 + random);
                robot.delay(WAIT_ROBOT_INTERVAL);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(WAIT_ROBOT_INTERVAL);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            } catch (TesseractException e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    public static int count (BufferedImage targetField) throws Exception{
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
        if(res > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}