package org.example;

import com.apple.eawt.Application;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
        BufferedImage image = getScreen(new Robot());
        ImageIO.write(image, "png", new File(" ./screen.png"));
    }
}