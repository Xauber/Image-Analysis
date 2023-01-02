/**
 * @author Nico Hezel
 */
package de.htw.ba.ue01.controller;

import java.text.ParseException;
import java.awt.Color;

public class EdgeDetectionController extends EdgeDetectionBase {

	private double[][] xGradientKernel = {
			{   0, 0,   0},
			{-0.5, 0, 0.5},
			{   0, 0,   0}
	};

	private double[][] yGradientKernel = {
			{0, -0.5, 0},
			{0,    0, 0},
			{0,  0.5, 0}
	};

	private double[][] xGradientSobelKernel = {
			{-0.125, 0, 0.125},
			{ -0.25, 0,  0.25},
			{-0.125, 0, 0.125}
	};

	private double[][] yGradientSobelKernel = {
			{-0.125, -0.25, -0.125},
			{     0,     0,      0},
			{ 0.125,  0.25,  0.125}
	};

	protected static enum Methods {
		Kopie, Graustufen, XGradient, YGradient, XGradientSobel, YGradientSobel,
		GradientenbetragSobel, GradientenwinkelSobel, GradientenwinkelSobelFarbe, KombiniertesBildSobelWinkelBetrag
	};

	@Override
	public void runMethod(Methods currentMethod, int[] srcPixels, int[] dstPixels, int width, int height) throws Exception {
		double parameter1 = getParameter();

		switch (currentMethod) {
			case Graustufen:
				doGray(srcPixels, dstPixels, width, height);

				break;
			case XGradient:
				doXGradient(srcPixels, dstPixels, width, height, parameter1);
				break;

			case YGradient:
				doYGradient(srcPixels, dstPixels, width, height, parameter1);
				break;

			case XGradientSobel:
				doXGradientSobel(srcPixels, dstPixels, width, height, parameter1);
				break;

			case YGradientSobel:
				doYGradientSobel(srcPixels, dstPixels, width, height, parameter1);
				break;

			case GradientenbetragSobel:
				doGradientenbetragSobel(srcPixels, dstPixels, width, height, parameter1);
				break;

			case GradientenwinkelSobel:
				doGradientenwinkelSobel(srcPixels, dstPixels, width, height, parameter1);
				break;

			case GradientenwinkelSobelFarbe:
				doGradientenwinkelSobelFarbe(srcPixels, dstPixels, width, height, parameter1);
				break;

			case KombiniertesBildSobelWinkelBetrag:
				doKombiniertesBildSobelWinkelBetrag(srcPixels, dstPixels, width, height, parameter1);
				break;

			case Kopie:
			default:
				doCopy(srcPixels, dstPixels, width, height, parameter1);
				break;
		}
	}



	private int[] doFilter(int srcPixels[], int width, int height, double [][] filterkernel) throws ParseException {

		int tempPixels[] = new int[srcPixels.length];
		// loop over all pixels of the destination image
		// länge des filter bestimmen
		int filtergröße = filterkernel[0].length;
		int kernelCenterDistance = (filterkernel.length-1)/2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				double pixelvalueSum = 0;

				for (int j = 0; j < filtergröße; j++) {
					for (int i = 0; i < filtergröße; i++) {
						int absXFilter = x + (i - kernelCenterDistance);
						int absYFilter= y + (j - kernelCenterDistance);
						int validx = Math.max(0, Math.min(absXFilter, width-1));
						int validy = Math.max(0, Math.min(absYFilter, height-1));

						int actualPos = validy * width + validx;

						pixelvalueSum = pixelvalueSum + (srcPixels[actualPos] & 0xFF) * filterkernel[j][i];
					}
				}
				tempPixels[pos] = (int) pixelvalueSum;
			}
		}
		return tempPixels;
	}



	private void doCopy(int srcPixels[], int dstPixels[], int width, int height, double parameter) {
		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				dstPixels[pos] = srcPixels[pos];
			}
		}
	}



	private void doGray(int srcPixels[], int dstPixels[], int width, int height) throws ParseException {
		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;

				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				dstPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
	}



	private void doXGradient(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixels = doFilter(tempPixels, width, height, xGradientKernel);
		System.out.println("hallo");
		for(int i = 0; i < filterPixels.length; i++){
			int value = filterPixels[i] + 128;
			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doYGradient(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixels = doFilter(tempPixels, width, height, yGradientKernel);
		System.out.println("hallo");
		for(int i = 0; i < filterPixels.length; i++){
			int value = filterPixels[i] + 128;
			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doXGradientSobel(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixels = doFilter(tempPixels, width, height, xGradientSobelKernel);
		System.out.println("hallo");
		for(int i = 0; i < filterPixels.length; i++){
			int value = filterPixels[i] + 128;
			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doYGradientSobel(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixels = doFilter(tempPixels, width, height, yGradientSobelKernel);
		System.out.println("hallo");
		for(int i = 0; i < filterPixels.length; i++){
			int value = filterPixels[i] + 128;
			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doGradientenbetragSobel(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixelsY = doFilter(tempPixels, width, height, yGradientSobelKernel);
		int [] filterPixelsX = doFilter(tempPixels, width, height, xGradientSobelKernel);

		for(int i = 0; i < filterPixelsX.length; i++){
			int value = (int) (Math.hypot(filterPixelsX[i], filterPixelsY[i])/127*255);
			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doGradientenwinkelSobel(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixelsY = doFilter(tempPixels, width, height, yGradientSobelKernel);
		int [] filterPixelsX = doFilter(tempPixels, width, height, xGradientSobelKernel);

		for(int i = 0; i < filterPixelsX.length; i++){
			int value = (int) (((Math.atan2(filterPixelsY[i], filterPixelsX[i])+ Math.PI) / (2 * Math.PI))*255);

			if (value > 255) {value = 255;}
			if (value < 0) {value = 0;}
			dstPixels[i]= 0xFF000000 | (value << 16) | (value << 8) | value;
		}
	}



	private void doGradientenwinkelSobelFarbe(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixelsY = doFilter(tempPixels, width, height, yGradientSobelKernel);
		int [] filterPixelsX = doFilter(tempPixels, width, height, xGradientSobelKernel);

		for(int i = 0; i < filterPixelsX.length; i++){
			double hue = (Math.atan2(filterPixelsY[i], filterPixelsX[i])+ Math.PI) / (2 * Math.PI);
			//System.out.println(hue);
			int colorValue = Color.HSBtoRGB((float) hue, 1, 1);
			dstPixels[i]= colorValue;
		}
	}



	private void doKombiniertesBildSobelWinkelBetrag(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int tempPixels[] = new int[srcPixels.length];

		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;



				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);

				// Overflow Begrenzung
				if (lum > 255) {lum = 255;}
				if (lum < 0) {lum = 0;}


				tempPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
		// offset 128
		int [] filterPixelsY = doFilter(tempPixels, width, height, yGradientSobelKernel);
		int [] filterPixelsX = doFilter(tempPixels, width, height, xGradientSobelKernel);

		for(int i = 0; i < filterPixelsX.length; i++){
			double hue = (Math.atan2(filterPixelsX[i], filterPixelsY[i])+ Math.PI) / (2 * Math.PI);
			double betrag =  Math.hypot(filterPixelsX[i], filterPixelsY[i])/127;
			//System.out.println(hue);
			int colorValue = Color.HSBtoRGB((float) hue, 1F, (float) betrag);
			dstPixels[i]= colorValue;
		}
	}
}
