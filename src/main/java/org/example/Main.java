package org.example;

import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.example.ImageTool.calcPositionObject;
import static org.example.ImageTool.getScreen;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
public class Main {
    public static void main(String[] args) throws Exception {
//        List<String> configs = Files.readAllLines(Paths.get(Application.class.getResource("/config.txt").getPath()));
//        Robot robot = new Robot();
//        SingleRoutine routine = new SingleRoutine(robot,
//                ImageIO.read(Application.class.getResource("/t.png")),
//                ImageIO.read(Application.class.getResource("/x.png")));
//        for (String conf : configs) {
//            routine.exec(conf);
//        }
        Robot robot = new Robot();
        BufferedImage image = getScreen(robot);
        // 暂存截屏 无用
        ImageIO.write(image, "png", new File("./screen.png"));

        BufferedImage a = ImageIO.read(ImageTool.class.getResource("/1111.png"));

        ITesseract instance = new Tesseract();
        String tessDataPath = Thread.currentThread().getContextClassLoader().getResource("tessdata").getPath();
        instance.setDatapath(tessDataPath);

        try {
            // 进行OCR识别
            String result = instance.doOCR(image);
            System.out.println(result);
            // 获取每个词的坐标
            List<Word> words = instance.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
            for (Word word : words) {
                System.out.println(word.getText() + " - Rect: " + word.getBoundingBox());
            }
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }


        PositionObject p = calcPositionObject(image, a);
        if (p != null) {
            robot.setAutoDelay(500);
            robot.mouseMove(p.getX(), p.getY());
        }
        System.out.println(p);
        //        ImageIO.write(image, "png", new File("./screen.png"));
    }
}