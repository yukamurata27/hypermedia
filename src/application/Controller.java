package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class Controller {
	private final int WIDTH = 352;
	private final int HEIGHT = 288;
	
	private byte[][] red = new byte[HEIGHT][WIDTH];
	private byte[][] green = new byte[HEIGHT][WIDTH];
	private byte[][] blue = new byte[HEIGHT][WIDTH];

	@FXML private Slider sliderL;
	@FXML private Slider sliderR;
	@FXML private Text textL;
	@FXML private Text textR;
	@FXML private Pane paneL;
	@FXML private Pane paneR;
	
	public void initialize() {
        // do initialization and configuration work...

		sliderL.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	textL.setText("Frame " + String.valueOf((int) sliderL.getValue()));
            }
        });

		sliderR.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	textR.setText("Frame " + String.valueOf((int) sliderR.getValue()));
            }
        });
		
		//////////////////////////////////////////////////////////////////////////////////
		File file = new File("../Source/USCOne/USCOne0001.rgb");
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		img = createBufferedImg(file);
		
	    Image i = SwingFXUtils.toFXImage(img, null);
	    ImageView v = new ImageView(i);
	    paneL.getChildren().add(v);
    }
	
	private BufferedImage createBufferedImg (File file) {
		FileInputStream fileInputStream = null;
		byte[] stream = new byte[(int) file.length()];
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		int baseIdx;
		
		try {
			//convert file into byte array
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(stream);
			fileInputStream.close();
		} catch (Exception e) {}

		// Save each R, G, and B values of image in byte
		for(int y = 0; y < HEIGHT; y++) {
			for(int x = 0; x < WIDTH; x++) {
				baseIdx = x + WIDTH * y;
				red[y][x] = stream[baseIdx];
				green[y][x] = stream[baseIdx + (HEIGHT * WIDTH)];
				blue[y][x] = stream[baseIdx + 2 * (HEIGHT * WIDTH)];

				int pix = 0xff000000 | ((red[y][x] & 0xff) << 16) | ((green[y][x] & 0xff) << 8) | (blue[y][x] & 0xff);
				img.setRGB(x, y, pix);
			}
		}
		
		return img;
	}
}
