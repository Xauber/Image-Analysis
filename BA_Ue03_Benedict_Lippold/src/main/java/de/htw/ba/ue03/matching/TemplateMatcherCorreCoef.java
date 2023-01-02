package de.htw.ba.ue03.matching;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Erstellt nur eine Kopie von dem Template Bild
 *
 * @author Nico
 *
 */
public class TemplateMatcherCorreCoef extends TemplateMatcherBase {

    public TemplateMatcherCorreCoef(int[] templatePixel, int templateWidth, int templateHeight) {
        super(templatePixel, templateWidth, templateHeight);
    }

    @Override
    public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {

        double[][] distanceMap = new double[srcHeight - templateHeight + 1][srcWidth - templateWidth + 1];
        double meanTemp = 0;

        for (int i = 0; i < templatePixel.length; i++) {
            meanTemp += templatePixel[i];
        }
        meanTemp = meanTemp/templatePixel.length;


        for (int y = 0; y < srcHeight - templateHeight; y++) {
            for (int x = 0; x < srcWidth - templateWidth; x++) {

                double meanSource = 0;
                for (int yTemplate = 0; yTemplate < templateHeight; yTemplate++) {
                    for (int xTemplate = 0; xTemplate < templateWidth; xTemplate++) {
                        int posSource = (y + yTemplate) * srcWidth + x + xTemplate;
                        meanSource += srcPixels[posSource] & 0xff;
                    }
                }
                meanSource = meanSource/templatePixel.length;

                double sum1 = 0;
                double sum2 = 0;
                double sum3 = 0;

                for (int yTemplate = 0; yTemplate < templateHeight; yTemplate++) {
                    for (int xTemplate = 0; xTemplate < templateWidth; xTemplate++) {

                        int posTemplate = yTemplate * templateWidth + xTemplate;
                        int valueTemplate = templatePixel[posTemplate] & 0xff;
                        int posSource = (y + yTemplate) * srcWidth + x + xTemplate;
                        int srcValue = srcPixels[posSource] & 0xff;

                        sum1 += (srcValue - meanSource)*(valueTemplate - meanTemp);
                        sum2 += Math.pow(srcValue - meanSource,2);
                        sum3 += Math.pow(valueTemplate - meanTemp,2);
                    }
                }
                distanceMap[y][x] = sum1/Math.sqrt(sum2)*Math.sqrt(sum3);
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
                int value =  (int) (((distanceMap[y][x]/maximum))*255);

                //if(value > 255) value = 255;
                //if(value < 0) value = 0;

                dstPixels[pos] = 0xFF000000 | (value << 16) | (value) << 8 | value;
            }
        }
    }

    //@Override
    //public List<Point> findMaximas(double[][] distanceMap) {
      //  return null;
    //}


    @Override
    public List<Point> findMaximas(double[][] distanceMap) {

            List<Point> max = new ArrayList<Point>();
            int accumulatorMaxValue = 255;
            boolean biggerNeighbourExists = false;
            int numberUmgebungspositionen = 10;

            for (int y = 0; y < distanceMap.length; y++) {
                for (int x = 0; x < distanceMap[0].length; x++) {
                    double value = distanceMap[y][x];

                    if (value >  0.5 * accumulatorMaxValue)
                        {
                        for (int i = -numberUmgebungspositionen; i <= numberUmgebungspositionen && !biggerNeighbourExists; i++) {
                            for (int j = -numberUmgebungspositionen; j <= numberUmgebungspositionen && !biggerNeighbourExists; j++) {

                                // dealing with the borders to not run out of bounds in the accumulator array
                                if (y + i > distanceMap.length - 1 || y + i < 0 || x + j > distanceMap[0].length - 1 || x + j < 0) {
                                    continue;
                                }
                                if (distanceMap[y + i][x + j] > value) {
                                    biggerNeighbourExists = true;
                                }
                                else  {
                                    max.add(new Point(x,y));
                                }
                            }
                        }
                        biggerNeighbourExists = false;
                    }
                }
            }
            return max;
    }
}