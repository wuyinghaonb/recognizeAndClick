package org.example;

import net.sourceforge.tess4j.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class OCR {

    public static void doOCR(ITesseract instance, BufferedImage image) throws Exception {
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
            throw new Exception(e.getMessage());
        }
    }

}
