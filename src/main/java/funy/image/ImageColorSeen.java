package funy.image;

import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ColorProcessor;

public class ImageColorSeen {

	private static ImageColorSeen colorSeen = new ImageColorSeen();
	private Opener opener;

	private ImageColorSeen() {
		opener = new Opener();
	}

	public static ImageColorSeen getInstance() {
		return colorSeen;
	}

	public boolean isLooksBlackWhite(String fileAbsolutePath) {
		try {
			ImagePlus imp = opener.openImage(fileAbsolutePath);
			Rectangle rect = new Rectangle(0, 0, imp.getWidth(), imp.getHeight());
			ColorProcessor cp = (ColorProcessor) imp.getProcessor();
			double[] histMean = calculateRgbMean(cp.getWidth(), cp.getPixels(), rect);
			return (IJ.d2s(histMean[0], 2).equals(IJ.d2s(histMean[1], 2))
					? IJ.d2s(histMean[0], 2).equals(IJ.d2s(histMean[2], 2)) : false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private double[] calculateRgbMean(int width, Object pixels, Rectangle roi) {
		double[] hmean = new double[3];
		int histcount = roi.width * roi.height;
		int[][] histogram = getHistogram(width, (int[]) pixels, roi);
		for (int col = 0; col < 3; col++) {
			double sum = 0;
			for (int i = 0; i < 256; i++) {
				sum += i * histogram[col][i];
			}
			hmean[col] = sum / histcount;
		}
		return hmean;
	}

	private int[][] getHistogram(int width, int[] pixels, Rectangle roi) {
		int c, r, g, b;
		int roiY = roi.y;
		int roiX = roi.x;
		int roiWidth = roi.width;
		int roiHeight = roi.height;
		int[][] histogram = new int[3][256];
		for (int y = roiY; y < (roiY + roiHeight); y++) {
			int i = y * width + roiX;
			for (int x = 0; x < roiWidth; x++) {
				c = pixels[i];
				r = (c & 0xff0000) >> 16;
				g = (c & 0xff00) >> 8;
				b = c & 0xff;
				histogram[0][r]++;
				histogram[1][g]++;
				histogram[2][b]++;
				i++;
			}
		}
		return histogram;
	}
}
