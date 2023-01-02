/**
 * @author Nico Hezel
 * modified by K. Jung, 28.10.2016
 */
package de.htw.ba.ue02.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class HoughTransformationController extends HoughTransformationBase {

	public int[][] accumulator;
	public int[][] accumulatorMax;

	public int numberUmgebungspositionen = 8;
	public double accumulatorThreshold = 0.6;

	protected static enum Methods {
		Empty, Accumulator, Maximum, Line
	};

	@Override
	public void runMethod(Methods currentMethod, int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, int dstWidth, int dstHeight, float sliderValue) throws Exception {
		switch (currentMethod) {
			case Accumulator:
				showAcc(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight);
				break;
			case Maximum:
				showMax(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, sliderValue);
				break;
			case Line:
				showLines(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, sliderValue);
				break;
			case Empty:
			default:
				empty(dstPixels, dstWidth, dstHeight);
				break;
		}
	}

	private void empty(int[] dstPixels, int dstWidth, int dstHeight) {
		// all pixels black
		Arrays.fill(dstPixels, 0xff000000);
	}
	
	private void showAcc(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, int dstWidth, int dstHeight) {
		empty(dstPixels, dstWidth, dstHeight);
		// TODO: calculate accumulator array
		// TODO: show accumulator array in dstPixel

		accumulator = new int[dstHeight][dstWidth];
		int accumulatorMaxValue = 0;

		// dstWidth:  für 360 Winkelschritte
		// dstHeight: für -r bis + r in 500er Schritte
		dstWidth = accumulator[1].length;
		dstHeight = accumulator[0].length;

		double radMax = Math.sqrt(srcWidth*srcWidth + srcHeight*srcHeight) / 2;
		double radMin = -radMax;
		

		for (int y = 0; y < srcHeight; y++) {
			for (int x = 0; x < srcWidth; x++) {

				int lumVal = (srcPixels[y * srcWidth + x] >> 16) & 0xFF;

				// translation of coordinate origin & calculation of accumulator values
				if (lumVal > 0) {
					int yTranslated = -(y - (srcHeight / 2));
					int xTranslated = x - (srcWidth / 2);
					for (int i = 0; i < accumulator[1].length; i++) {
						double phi_image = Math.toRadians((double) i / 2);
						double radius_image = (xTranslated * Math.cos(phi_image)) + (yTranslated * Math.sin(phi_image));
						int radius_Norm_image = (int) (((radius_image - radMin) / (radMax- radMin)) * dstHeight - 1);
						//int radius_Norm_image = (int) ((radius_image + 2) / 4  * dstHeight - 1);

						if (accumulator[radius_Norm_image][i] > accumulatorMaxValue) {
							accumulatorMaxValue++;
						}
						accumulator[radius_Norm_image][i]++;
						//System.out.println(accumulator[radius_Norm_image][i]);
					}
				}
			}
		}
		// create new accumulator image with precalculated accumulator values
		for (int i = 0; i < dstHeight; i++) {
			for (int j = 0; j < dstWidth; j++) {
				int pos = i * dstWidth + j;

				if (accumulator[i][j] > 0) {
					int lumVal = (int) ((accumulator[i][j] * accumulatorMaxValue) / 255);
					dstPixels[pos] = (0xFF << 24) | (lumVal << 16) | (lumVal << 8) | lumVal;
				}
			}
		}
	}
	
	private void showMax(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, int dstWidth, int dstHeight, float sliderValue) {
		empty(dstPixels, dstWidth, dstHeight);
		accumulatorMax = new int[dstHeight][dstWidth];
		int accumulatorMaxValue = 255;
		boolean biggerNeighbourExists = false;

		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				int valueAccuPos = accumulator[y][x];

				if (valueAccuPos < sliderValue * accumulatorMaxValue) {
					accumulatorMax[y][x] = 0;
				} else {
					for (int i = -numberUmgebungspositionen; i <= numberUmgebungspositionen && !biggerNeighbourExists; i++) {
						for (int j = -numberUmgebungspositionen; j <= numberUmgebungspositionen && !biggerNeighbourExists; j++) {

							// dealing with the borders to not run out of bounds in the accumulator array
							if (y + i > dstHeight - 1 || y + i < 0 || x + j > dstWidth - 1 || x + j < 0) {
								continue;
							}
							if (accumulator[y + i][x + j] > valueAccuPos) {
								accumulatorMax[y][x] = 0;
								biggerNeighbourExists = true;
							}
							else  {
								accumulatorMax[y][x] = 255;
							}
						}
					}
					biggerNeighbourExists = false;
				}
			}
		}
		// create new accumulator image with precalculated accumulator values
		for (int k = 0; k < dstHeight; k++) {
			for (int l = 0; l < dstWidth; l++) {
				int pos = k * dstWidth + l;
				if (accumulatorMax[k][l] > 0) {
					int lumVal = 255;
					dstPixels[pos] = (0xFF << 24) | (lumVal << 16) | (lumVal << 8) | lumVal;
				}
			}
		}
	}

	
	private void showLines(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, int dstWidth, int dstHeight, float sliderValue) {
		empty(dstPixels, dstWidth, dstHeight);
		// TODO: calculate accumulator array and maximum array
		// TODO: show maximum array in dstPixel
		// TODO: draw lines in srcImage


		BufferedImage bufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
		bufferedImage.setRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.RED);
		g2d.drawLine(0, 0, srcWidth, srcHeight);
		g2d.drawLine(srcWidth, 0, 0, srcHeight);
		g2d.dispose();
		bufferedImage.getRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
	}

}
