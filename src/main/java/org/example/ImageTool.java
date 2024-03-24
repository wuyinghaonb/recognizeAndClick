package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageTool {
    public static double getLoss(int x, int y, BufferedImage image, BufferedImage t) {
        double loss = 0;
        for (int m = 0; m < t.getWidth(); m++) {
            for (int n = 0; n < t.getHeight(); n++) {
                if (x + m >= image.getWidth() || y + n >= image.getHeight()) {
                    continue;
                }
                Color ic = new Color(image.getRGB(x + m, y + n));
                Color tc = new Color(t.getRGB(m, n));
                loss += Math.pow(ic.getRed() - tc.getRed(), 2) + Math.pow(ic.getBlue() - tc.getBlue(), 2) + Math.pow(ic.getGreen() - tc.getGreen(), 2);
            }
        }
        return loss;
    }

    public static BufferedImage getScreen(Robot robot) {
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    public static PositionObject calcPositionObject(BufferedImage image, BufferedImage t) {
        double min = Double.MAX_VALUE;
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                double loss = getLoss(x, y, image, t);
                min = Math.min(min, loss);
                System.out.println(min);
                if (loss < 30) {
                    return new PositionObject(loss, x, y);
                }
            }
        }
        return null;
    }

    public static String getSystemClipboard() {
        Clipboard sysClb;
        sysClb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Transferable t = sysClb.getContents(null);
            if (null != t && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void setSysClipboardText(String msg) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(msg);
        clip.setContents(tText, null);
    }

    public static void main(String[] args) throws Exception {
        BufferedImage image = getScreen(new Robot());
        ImageIO.write(image, "png", new File("./screen.png"));
    }
}
