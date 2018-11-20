package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {
	private final int WIDTH = 352;
	private final int HEIGHT = 288;
	
	private byte[][] red = new byte[HEIGHT][WIDTH];
	private byte[][] green = new byte[HEIGHT][WIDTH];
	private byte[][] blue = new byte[HEIGHT][WIDTH];
	
	private File primaryPath;
	private String primaryString;
	private File secondaryPath;
	private String secondaryString;

	@FXML private Slider sliderL;
	@FXML private Slider sliderR;
	@FXML private Text textL;
	@FXML private Text textR;
	@FXML private Pane paneL;
	@FXML private Pane paneR;
	@FXML private Button importPButton;
	@FXML private Button importSButton;

	public void initialize() throws Exception {
        // do initialization and configuration work...

		sliderL.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	int frameNum = (int) sliderL.getValue();
            	textL.setText("Frame " + String.valueOf(frameNum));
            	try {
            		changeFrame(paneL, createBufferedImg(new File(primaryString + "/" + (primaryString.substring(primaryString.lastIndexOf("/") + 1) + String.format("%04d", frameNum) + ".rgb"))));
            	} catch (Exception e) {}
            }
        });

		sliderR.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	int frameNum = (int) sliderR.getValue();
            	textR.setText("Frame " + String.valueOf(frameNum));
            	File file = new File("../Source/USCOne/USCOne" + String.format("%04d", frameNum) + ".rgb");
            	try {
            		changeFrame(paneR, createBufferedImg(new File(secondaryString + "/" + (secondaryString.substring(secondaryString.lastIndexOf("/") + 1) + String.format("%04d", frameNum) + ".rgb"))));
            	} catch (Exception e) {}
            }
        });
    }

	private void setLeftImg (File path) throws Exception {
		primaryString = path.getAbsolutePath();
		changeFrame(paneL, createBufferedImg(new File(primaryString + "/" + (primaryString.substring(primaryString.lastIndexOf("/") + 1) + "0001.rgb"))));
	}
	
	private void setRightImg (File path) throws Exception {
		secondaryString = path.getAbsolutePath();
		changeFrame(paneR, createBufferedImg(new File(secondaryString + "/" + (secondaryString.substring(secondaryString.lastIndexOf("/") + 1) + "0001.rgb"))));
	}
	
	private void changeFrame (Pane p, BufferedImage img) {
		Image i = SwingFXUtils.toFXImage(img, null);
	    ImageView v = new ImageView(i);
	    p.getChildren().clear();
	    p.getChildren().add(v);
	}
	
	private BufferedImage createBufferedImg (File file) throws Exception {
		FileInputStream fileInputStream = null;
		byte[] stream = new byte[(int) file.length()];
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		int baseIdx;
		
		//convert rgb file into byte array
		fileInputStream = new FileInputStream(file);
		fileInputStream.read(stream);
		fileInputStream.close();

		// Save RGB values of image individually in byte
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
	
	@FXML
	private void import_primary(ActionEvent event) {
		Stage stage = new Stage();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a primary video");

		importPButton.setOnAction(
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						primaryPath = directoryChooser.showDialog(stage);
						try {
							setLeftImg(primaryPath);
						} catch (Exception error){}
					}
				}
		);
	}

	@FXML
	private void import_secondary(ActionEvent event) {
		Stage stage = new Stage();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a secondary video");

		importSButton.setOnAction(
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						secondaryPath = directoryChooser.showDialog(stage);
						try {
							setRightImg(secondaryPath);
						} catch (Exception error){}
					}
				}
		);
	}
}
