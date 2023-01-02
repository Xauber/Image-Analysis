package de.htw.ba.ue03.matching;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Erstellt nur eine Kopie von dem Template Bild
 *
 * @author Nico
 *
 */
public class TemplateMatcherSquare extends TemplateMatcherBase {

    public TemplateMatcherSquare(int[] templatePixel, int templateWidth, int templateHeight) {
        super(templatePixel, templateWidth, templateHeight);
    }

    @Override
    public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {

        double[][] distanceMap = new double[srcHeight - templateHeight + 1][srcWidth - templateWidth + 1];
        for (int y = 0; y < srcHeight - templateHeight; y++) {
            for (int x = 0; x < srcWidth - templateWidth; x++) {

                double squareError = 0;

                for (int yTemplate = 0; yTemplate < templateHeight; yTemplate++) {


                    for (int xTemplate = 0; xTemplate < templateWidth; xTemplate++) {

                        int posTemplate = yTemplate * templateWidth + xTemplate;
                        int valueTemplate = templatePixel[posTemplate] & 0xff;
                        int posSource = (y + yTemplate) * srcWidth + x + xTemplate;
                        int srcValue = srcPixels[posSource] & 0xff;

                        squareError += Math.pow(srcValue - valueTemplate, 2);
                    }
                }
                distanceMap[y][x] = Math.sqrt(squareError);
            }
        }
        return distanceMap;
    }

    @Override
    public void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight) {
        double maximum = Arrays.stream(Arrays.stream(distanceMap).flatMapToDouble(Arrays::stream).toArray()).max().getAsDouble();

        for (int y = 0; y < distanceMap.length; y++) {
            for (int x = 0; x < distanceMap[0].length; x++) {
                int pos = y * dstWidth + x;
                int value =  (int) ((1-(distanceMap[y][x]/maximum))*255);
                dstPixels[pos] = 0xFF000000 | (value << 16) | (value) << 8 | value;
            }
        }
    }

    @Override
    public List<Point> findMaximas(double[][] distanceMap) {
        return new ArrayList<>();
    }
}