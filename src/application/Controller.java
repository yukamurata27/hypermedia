package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.*;

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
import javafx.scene.control.Label;
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
	private int btnNum = 0;
	private ArrayList<String> tmpData = new ArrayList<String>();
	
	private JSONObject obj1;
	private JSONObject obj2;
	private JSONObject obj3;

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

    private void changeLblName () throws Exception {
    }

    @FXML
    private void connectVideo (ActionEvent event) throws Exception {
    	// POPUP DIALOG ///////////////////////////////////////////////////////////////////////
    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);
        dialog.initModality(Modality.NONE);
        //VBox dialogVbox = new VBox(20);
        //dialogVbox.getChildren().add(new Text("This is a Dialog"));
        
        Label linkNameLbl = new Label("Link name");

        StringWriter sw = new StringWriter();
        //PrintWriter pw = new PrintWriter(sw);
        //ex.printStackTrace(pw);
        String exceptionText = sw.toString();
        TextArea textArea = new TextArea(exceptionText);
        textArea.setPrefSize(350, 7);
        
        Label startlbl = new Label("Starting frame number");

        StringWriter sw2 = new StringWriter();
        //PrintWriter pw = new PrintWriter(sw);
        //ex.printStackTrace(pw);
        String exceptionText2 = sw2.toString();
        TextArea startTxtArea = new TextArea(exceptionText2);
        startTxtArea.setPrefSize(50, 7);
        
        Label endlbl = new Label("Ending frame number");

        StringWriter sw3 = new StringWriter();
        //PrintWriter pw = new PrintWriter(sw);
        //ex.printStackTrace(pw);
        String exceptionText3 = sw3.toString();
        TextArea endTxtArea = new TextArea(exceptionText3);
        endTxtArea.setPrefSize(50, 7);
        
        //Scene dialogScene = new Scene(dialogVbox, 300, 200);
        //dialog.setScene(dialogScene);
        
        Button cancel = new Button();
        cancel.setText("Cancel");
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	dialog.close();
            }
        });

        Button save = new Button();
        save.setText("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	
            	//  JSON  /////////////////////////////////////////////////////////////
            	JSONParser jsonParser = new JSONParser();
            	JSONArray jsonArray = new JSONArray();
            	Object obj = new Object();
            	try {
	            	obj = jsonParser.parse(new FileReader(primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json"));
	            	jsonArray = (JSONArray)obj;
            	} catch (Exception exc){}

            	if (jsonArray != null) {
                    //List<URL> urlList = new ArrayList<URL>();
                    Iterator it = jsonArray.iterator();
                    while (it.hasNext()) {
                        //String primary = it.next().toString();
                    	JSONObject jsonObject = (JSONObject) it.next();

                    	for(Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
                    	    String key = (String) iterator.next();
                    	    if (key.equals(primaryPath)) {
                    	    	//append
                    	    	/*try {
									obj = jsonParser.parse(new FileReader(primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json"));
								} catch (Exception e1) {}
                    	    	jsonArray = (JSONArray) obj;
                                JSONArray jsonPrimaryPath = (JSONArray) jsonArray.get(primaryPath);
                                */

                                obj1 = new JSONObject();
                	            obj2 = new JSONObject();

                	            obj1.put("SECONDARY", secondaryPath);
                	            obj1.put("x", boxX);
                	            obj1.put("y", boxY);
                	            obj1.put("width", boxW);
                	            obj1.put("height", boxH);

                	            obj2.put(textArea.getText(), obj1);

                                //jsonPrimaryPath.add(obj2);
                	            //jsonObject = (JSONObject)jsonObject.get(key);
                	            //jsonObject.put(primaryRootPath, obj2);
                	            jsonArray.add(jsonObject.get(key));
                	            jsonArray.add(obj2);

                                try {
                                	FileWriter fileToWrite = new FileWriter(primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json", false);
                                    fileToWrite.write(jsonArray.toJSONString());
                                    fileToWrite.flush();
                                    fileToWrite.close();
                                } catch (IOException exc) {}
                    	    }
                    	    //System.out.println("key->" + key);
                    	    //->/Users/yukamurata/Documents/USC/Fall 18/CSCI 576/FinalProject/Source/USCOne/USCOne0001.rgb
                    	    //System.out.println(jsonObject.get(key));
                    	    //->{"sign":{"SECONDARY":"\/Users\/yukamurata\/Documents\/USC\/Fall 18\/CSCI 576\/FinalProject\/Source\/USCTwo\/USCTwo0001.rgb","x":74.0,"width":204.0,"y":68.0,"height":124.0}}
                    	}
                    }
                }
            	
            	// For a frame that does not have a bounding box yet
	            obj1 = new JSONObject();
	            obj2 = new JSONObject();
	            obj3 = new JSONObject();

	            obj1.put("SECONDARY", secondaryPath);
	            obj1.put("x", boxX);
	            obj1.put("y", boxY);
	            obj1.put("width", boxW);
	            obj1.put("height", boxH);

	            obj2.put(textArea.getText(), obj1);
	            obj3.put(primaryPath, obj2);
	            jsonArray.add(obj3);

	            try (FileWriter file = new FileWriter(primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json")) {
	                file.write(jsonArray.toJSONString());
	                file.flush();
	            } catch (IOException exc) {}
	            
            	
            	
                //  END of JSON  /////////////////////////////////////////////////////////////

            	Button btn = new Button();
            	btn.setLayoutX(0);
                btn.setLayoutY(27*btnNum);
            	linkPane.getChildren().add(btn);
            	btn.setText(textArea.getText());
            	btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                    	try {
                    		final Stage editStage = new Stage();
                    		editStage.initModality(Modality.APPLICATION_MODAL);
                    		editStage.initModality(Modality.NONE);

                            StringWriter sw4 = new StringWriter();
                            String exceptionText4 = sw4.toString();
                            TextArea editTxtArea = new TextArea(exceptionText4);
                            editTxtArea.setText(textArea.getText());
                            editTxtArea.setPrefSize(350, 7);

                            StringWriter sw2 = new StringWriter();
                            String exceptionText2 = sw2.toString();
                            TextArea startTxtArea = new TextArea(exceptionText2);
                            startTxtArea.setText("Get starting frame number");
                            startTxtArea.setPrefSize(50, 7);

                            StringWriter sw3 = new StringWriter();
                            String exceptionText3 = sw3.toString();
                            TextArea endTxtArea = new TextArea(exceptionText3);
                            endTxtArea.setText("Get starting frame number");
                            endTxtArea.setPrefSize(50, 7);
                            
                            Button cancel = new Button();
                            cancel.setText("Cancel");
                            cancel.setOnAction(new EventHandler<ActionEvent>() {
                                @Override public void handle(ActionEvent e) {
                                	editStage.close();
                                }
                            });
                    		
                            Button save = new Button();
                            save.setText("Save");
                            save.setOnAction(new EventHandler<ActionEvent>() {
                                @Override public void handle(ActionEvent e) {
                                	btn.setText(editTxtArea.getText());
                                	editStage.close();
                                }
                            });
                            
                            VBox vbox2 = new VBox();
                            vbox2.getChildren().addAll(linkNameLbl, editTxtArea, startlbl, startTxtArea, endlbl, endTxtArea, cancel, save);
                            Scene scene2 = new Scene(vbox2);

                            editStage.setScene(scene2);
                            editStage.show();
                    	} catch(Exception exc){}
                    }
                });
            	btnNum++;
            	dialog.close();
            }
        });
        
        VBox vbox = new VBox();
        vbox.getChildren().addAll(linkNameLbl, textArea, startlbl, startTxtArea, endlbl, endTxtArea, cancel, save);
        Scene scene = new Scene(vbox);

        dialog.setScene(scene);
        dialog.show();
    }
    
    @FXML
    private void saveFile (ActionEvent event) throws Exception {
    	//StringWriter out = new StringWriter();
        //obj.writeJSONString(out);

        //String jsonText = out.toString();
        //System.out.print(jsonText);
    	
    	try (FileWriter file = new FileWriter("meta.json")) {
            //file.write(ja.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
