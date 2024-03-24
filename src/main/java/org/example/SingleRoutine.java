package org.example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Objects;

import static org.example.ImageTool.calcPositionObject;
import static org.example.ImageTool.getScreen;

public class SingleRoutine {

    private final Robot robot;
    private final BufferedImage t;
    private final BufferedImage x;
    private final static int TRY_COUNT = 10;
    private final static int WAIT_PAGE_LOAD_INTERVAL = 2000;
    private final static int WAIT_ROBOT_INTERVAL = 200;

    public SingleRoutine(Robot robot, BufferedImage t, BufferedImage x) {
        this.robot = robot;
        this.t = t;
        this.x = x;
    }

    public void exec(String url) throws Exception {
        Desktop.getDesktop().browse(new URI(url));
        int c = 0;
        while (c < TRY_COUNT) {
            c++;
            Thread.sleep(WAIT_PAGE_LOAD_INTERVAL);
            BufferedImage image = getScreen(robot);
            PositionObject object = calcPositionObject(image, t);
            if (Objects.nonNull(calcPositionObject(image, x))) {
                execHackScript(robot);
                return;
            }
            if (Objects.nonNull(object)) {
                execHackScript(robot, object.getX(), object.getY());
                break;
            }
        }
//        System.out.println(HackTools.getSystemClipboard());
    }

    private void execHackScript(Robot robot) {
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_W);
        delay();
        robot.keyRelease(KeyEvent.VK_W);
        robot.keyRelease(KeyEvent.VK_META);
    }

    private void execHackScript(Robot robot, int x, int y) {
        robot.mouseMove(x + 14, y + 14);
        delay();
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        delay();
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_C);
        delay();
        robot.keyRelease(KeyEvent.VK_C);
        delay();
        robot.keyPress(KeyEvent.VK_W);
        delay();
        robot.keyRelease(KeyEvent.VK_W);
        delay();
        robot.keyRelease(KeyEvent.VK_META);
        robot.mouseMove(x - 20, y + 14);
    }

    private void delay() {
        robot.delay(WAIT_ROBOT_INTERVAL);
    }
}
