package put.output;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LegendGeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		try {
			String filename = "tmp.png";
			LegendGenerator.generate(-0, 1000, Color.GREEN, Color.RED, filename, 640, 100, "UNIT", "Some description");
			Desktop.getDesktop().open(new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
