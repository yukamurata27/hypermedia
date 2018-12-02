package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller {
	private final int WIDTH = 352;
	private final int HEIGHT = 288;
	
	private byte[][] red = new byte[HEIGHT][WIDTH];
	private byte[][] green = new byte[HEIGHT][WIDTH];
	private byte[][] blue = new byte[HEIGHT][WIDTH];
	
	private File primaryFolder;
	private String primaryRootPath;
	private String primaryPath;
	private File secondaryFolder;
	private String secondaryRootPath;
	private String secondaryPath;
	private double boxX;
	private double boxY;
	private double boxW;
	private double boxH;

	@FXML private Slider sliderL;
	@FXML private Slider sliderR;
	@FXML private Text textL;
	@FXML private Text textR;
	@FXML private Pane paneL;
	@FXML private Pane paneR;
	@FXML private Button importPButton;
	@FXML private Button importSButton;
	@FXML private Button makeLinkBtn;
	@FXML private AnchorPane linkPane;

	public void initialize() throws Exception {
		sliderL.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	int frameNum = (int) sliderL.getValue();
            	textL.setText("Frame " + String.valueOf(frameNum));
            	try {
            		primaryPath = primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + String.format("%04d", frameNum) + ".rgb");
            		changeFrame(paneL, createBufferedImg(new File(primaryPath)));
            	} catch (Exception e) {}
            }
        });

		sliderR.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
            	int frameNum = (int) sliderR.getValue();
            	textR.setText("Frame " + String.valueOf(frameNum));
            	try {
            		secondaryPath = secondaryRootPath + "/" + (secondaryRootPath.substring(secondaryRootPath.lastIndexOf("/") + 1) + String.format("%04d", frameNum) + ".rgb");
            		changeFrame(paneR, createBufferedImg(new File(secondaryPath)));
            	} catch (Exception e) {}
            }
        });
    }

	private void setLeftImg (File path) throws Exception {
		primaryRootPath = path.getAbsolutePath();
		primaryPath = primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + "0001.rgb");
		changeFrame(paneL, createBufferedImg(new File(primaryPath)));
	}

	private void setRightImg (File path) throws Exception {
		secondaryRootPath = path.getAbsolutePath();
		secondaryPath = secondaryRootPath + "/" + (secondaryRootPath.substring(secondaryRootPath.lastIndexOf("/") + 1) + "0001.rgb");
		changeFrame(paneR, createBufferedImg(new File(secondaryPath)));
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
						primaryFolder = directoryChooser.showDialog(stage);
						try {
							setLeftImg(primaryFolder);
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
						secondaryFolder = directoryChooser.showDialog(stage);
						try {
							setRightImg(secondaryFolder);
						} catch (Exception error){}
					}
				}
		);
	}

	@FXML
	private void makeNewBox (ActionEvent event) {
		boxX = 150;
		boxY = 130;
		boxW = 32;
		boxH = 28;
		Rectangle rect = createDraggableRectangle(boxX, boxY, boxW, boxH);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.RED);
        paneL.getChildren().add(rect);
	}

	private Rectangle createDraggableRectangle(double x, double y, double width, double height) {
        final double handleRadius = 10;

        Rectangle rect = new Rectangle(x, y, width, height);

        // top left resize handle:
        Circle resizeHandleNW = new Circle(handleRadius, Color.GOLD);
        // bind to top left corner of Rectangle:
        resizeHandleNW.centerXProperty().bind(rect.xProperty());
        resizeHandleNW.centerYProperty().bind(rect.yProperty());

        // bottom right resize handle:
        Circle resizeHandleSE = new Circle(handleRadius, Color.GOLD);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.centerXProperty().bind(rect.xProperty().add(rect.widthProperty()));
        resizeHandleSE.centerYProperty().bind(rect.yProperty().add(rect.heightProperty()));

        // move handle:
        Circle moveHandle = new Circle(handleRadius, Color.GOLD);
        // bind to bottom center of Rectangle:
        moveHandle.centerXProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)));
        moveHandle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty()));

        // force circles to live in same parent as rectangle:
        rect.parentProperty().addListener((obs, oldParent, newParent) -> {
            for (Circle c : Arrays.asList(resizeHandleNW, resizeHandleSE, moveHandle)) {
                Pane currentParent = (Pane)c.getParent();
                if (currentParent != null) {
                    currentParent.getChildren().remove(c);
                }
                ((Pane)newParent).getChildren().add(c);
            }
        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        setUpDragging(resizeHandleNW, mouseLocation);
        setUpDragging(resizeHandleSE, mouseLocation);
        setUpDragging(moveHandle, mouseLocation);

        resizeHandleNW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                if (newX >= handleRadius 
                        && newX <= rect.getX() + rect.getWidth() - handleRadius) {
                	boxX = newX;
                	boxW = rect.getWidth() - deltaX;
                	rect.setX(boxX);
                    rect.setWidth(boxW);
                }
                double newY = rect.getY() + deltaY;
                if (newY >= handleRadius 
                        && newY <= rect.getY() + rect.getHeight() - handleRadius) {
                	boxY = newY;
                	boxH = rect.getHeight() - deltaY;
                    rect.setY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxX = rect.getX() + rect.getWidth() + deltaX;
                if (newMaxX >= rect.getX() 
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    boxW = rect.getWidth() + deltaX;
                	rect.setWidth(boxW);
                }
                double newMaxY = rect.getY() + rect.getHeight() + deltaY;
                if (newMaxY >= rect.getY() 
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    boxH = rect.getHeight() + deltaY;
                	rect.setHeight(boxH);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        moveHandle.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                double newMaxX = newX + rect.getWidth();
                if (newX >= handleRadius 
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    boxX = newX;
                	rect.setX(boxX);
                }
                double newY = rect.getY() + deltaY ;
                double newMaxY = newY + rect.getHeight();
                if (newY >= handleRadius 
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    boxY = newY;
                	rect.setY(boxY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        return rect;
    }

    private void setUpDragging(Circle circle, Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            circle.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            circle.getParent().setCursor(Cursor.DEFAULT);
            mouseLocation.value = null;
        });
    }

    static class Wrapper<T> { T value; }

    @FXML
    private void connectVideo (ActionEvent event) throws Exception {
    	//System.out.println("x: " + boxX + ", y: " + boxY + ", Width: " + boxW + ", Heigh: " + boxH);
    	//System.out.println("PRIMARY   -> frame: " + ", path: " + primaryPath);
    	//System.out.println("SECONDARY -> frame: " + ", path: " + secondaryPath);
    	//System.out.println("SECONDARY -> frame: " + ", path: " + secondaryPath);

    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);
        //dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("This is a Dialog"));
        TextArea textArea = new TextArea(exceptionText);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    	
    	JSONObject obj1 = new JSONObject();
    	JSONObject obj2 = new JSONObject();
    	JSONArray ja = new JSONArray();

    	obj1.put("primary", primaryPath);
        obj2.put("name", "temp 1");
        obj2.put("SECONDARY", secondaryPath);
        obj2.put("x", boxX);
        obj2.put("y", boxY);
        obj2.put("width", boxW);
        obj2.put("height", boxH);
        obj1.put("frameNo", obj2);
        ja.add(obj1);

        //StringWriter out = new StringWriter();
        //obj.writeJSONString(out);

        //String jsonText = out.toString();
        //System.out.print(jsonText);

        try (FileWriter file = new FileWriter("meta.json")) {
            file.write(ja.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
