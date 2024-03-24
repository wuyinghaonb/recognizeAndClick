package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.lang.Thread.sleep;

public class shoot {
    public static void main(String[] args) throws Exception {
        sleep(5000);
        BufferedImage image = getScreen(new Robot());
        // 获取当前用户的桌面路径
        String desktopPath = System.getProperty("user.home") + "\\Desktop";

        // 构建保存文件的路径，文件名为"DesktopCapture.png"
        File file = new File(desktopPath + "\\DesktopCapture.png");

        // 保存截图到文件
        ImageIO.write(image, "png", file);

    }

    public static BufferedImage getScreen(Robot robot) {
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }
}
