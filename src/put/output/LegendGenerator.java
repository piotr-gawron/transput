package put.output;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LegendGenerator {
	public static final void generate(double minVal, double maxVal, Color from, Color to, String fileName, int width, int height) throws IOException {
		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D ig2 = bi.createGraphics();

		ig2.fillRect(0, 0, width, height);


		int left = 20;
		width -= 2*left;

		for (int y = left; y < width + left; y++) {
			double scale = ((double) (y - left)) / ((double) width);
			int r = (int) (to.getRed() * scale + (1 - scale) * from.getRed());
			int g = (int) (to.getGreen() * scale + (1 - scale) * from.getGreen());
			int b = (int) (to.getBlue() * scale + (1 - scale) * from.getBlue());
			ig2.setPaint(new Color(r, g, b));
			ig2.drawLine(y, 30, y, 50);
		}
		ig2.setPaint(Color.BLACK);
		for (int i = 0; i <= 10; i++) {
			int x = left + i * width / 10;
			ig2.drawLine(x, 25, x, 50);

			int val = (int)((maxVal - minVal) * i / 10 + minVal);
			ig2.drawString(val+"", x-10, 10);
		}

		ImageIO.write(bi, "PNG", new File(fileName));
	}
}
