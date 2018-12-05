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
	private int btnNum;
	private ArrayList<String> tmpData = new ArrayList<String>();
	boolean dupe;
	
	private JSONObject obj1;
	private JSONObject obj2;
	private JSONObject obj3;
	
	private ArrayList<String> dataALst;
	private ArrayList<String> linkNameALst;

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
							linkPane.getChildren().clear();
							setLeftImg(primaryFolder);
							loadJSON(primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json"));
							btnNum = 0;
							loadLinks();
							sliderL.setValue(0.0);
							textL.setText("Frame 1");
						} catch (Exception error){}
					}
				}
		);
	}
	
	private void loadLinks () {
		Label linkNameLbl = new Label("Link name");
				
		for (String it : linkNameALst) {
			Button btn = new Button();
	    	btn.setLayoutX(0);
	        btn.setLayoutY(27*btnNum);
	    	linkPane.getChildren().add(btn);
	    	btn.setText(it);
	    	btn.setOnAction(new EventHandler<ActionEvent>() {
	            @Override public void handle(ActionEvent e) {
	            	try {
	            		
	            		// Change a frame to the starting point
	            		
	            		int idx = linkNameALst.indexOf(it);
	            		String startPath = dataALst.get(idx).split(",")[0];
	            		String frameNumber = startPath.substring(startPath.length()-8, startPath.length()-4);
	            		primaryPath = primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + frameNumber + ".rgb");
	            		changeFrame(paneL, createBufferedImg(new File(primaryPath)));
	            		
	            		// Change slider
	            		
	            		sliderL.setValue(Double.parseDouble(frameNumber));
	            		textL.setText("Frame " + frameNumber);
	            		
	            		final Stage editStage = new Stage();
	            		editStage.initModality(Modality.APPLICATION_MODAL);
	            		editStage.initModality(Modality.NONE);

	                    StringWriter sw4 = new StringWriter();
	                    String exceptionText4 = sw4.toString();
	                    TextArea editTxtArea = new TextArea(exceptionText4);
	                    editTxtArea.setText(it);
	                    editTxtArea.setPrefSize(350, 7);
	                    
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
	                        	linkNameALst.set(linkNameALst.indexOf(it), editTxtArea.getText());
	                        	editStage.close();
	                        }
	                    });
	                    
	                    VBox vbox2 = new VBox();
	                    vbox2.getChildren().addAll(linkNameLbl, editTxtArea, cancel, save);
	                    Scene scene2 = new Scene(vbox2);

	                    editStage.setScene(scene2);
	                    editStage.show();
	            	} catch(Exception exc){}
	            }
	        });
	    	btnNum++;
		}
	}
	
	/*
	 * Check whether a json file exist for the primary video
	 * If so, load the data into listNameALst and dataAList
	 */
	private void loadJSON (String pathToJSON) {
		linkNameALst = new ArrayList<String>();
		dataALst = new ArrayList<String>();
		JSONParser jsonParser = new JSONParser();
    	/////JSONArray jsonArray = new JSONArray();
    	Object obj = new Object();
    	
    	// Open a json file if any
    	try {
        	obj = jsonParser.parse(new FileReader(primaryRootPath + "/" + primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json"));
        	/////jsonArray = (JSONArray)obj;
    	} catch (Exception exc){ return; }
    	
    	JSONArray jsonArray = new JSONArray();
    	jsonArray = (JSONArray)obj;
    	
    	// Get each link data
    	if (jsonArray != null) {
            Iterator it = jsonArray.iterator();
            while (it.hasNext()) {
            	JSONObject jsonObject = (JSONObject) it.next();
            	for(Iterator dataIter = jsonObject.entrySet().iterator(); dataIter.hasNext();) {
            		Object jsonobj = dataIter.next();
            		String key = jsonobj.toString().split("=")[0];
            		String data = jsonobj.toString().split("[\\{\\}]")[1];
            		
            		linkNameALst.add(key);
            		String[] pairs = data.split(",");
            		String dataLine = "";
            		
            		/*
            		System.out.println("0: " + pairs[0].split(":")[1]);
            		System.out.println("1: " + pairs[1].split(":")[1]);
            		System.out.println("2: " + pairs[2].split(":")[1]);
            		System.out.println("3: " + pairs[3].split(":")[1]);
            		System.out.println("4: " + pairs[4].split(":")[1]);
            		System.out.println("5: " + pairs[5].split(":")[1]);
            		System.out.println("6:" + pairs[6].split(":")[1]);
            		*/
            		
            		dataLine += pairs[5].split(":")[1].substring(2, pairs[5].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[4].split(":")[1].substring(2, pairs[4].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[0].split(":")[1].substring(2, pairs[0].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[1].split(":")[1].substring(1, pairs[1].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[3].split(":")[1].substring(1, pairs[3].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[2].split(":")[1].substring(1, pairs[2].split(":")[1].length()-1).replace("\\","");
            		dataLine += "," + pairs[6].split(":")[1].substring(1, pairs[6].split(":")[1].length()-1).replace("\\","");
            		//System.out.println("data 2 (dataline) is " + dataLine);
            		dataALst.add(dataLine);
            	}
            }
    	}
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

        dupe = false;
        Button save = new Button();
        save.setText("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	// Validation of frame numbers
            	
            	int sFrame = Integer.parseInt(startTxtArea.getText().replaceAll("\\D+",""));
            	int eFrame = Integer.parseInt(endTxtArea.getText().replaceAll("\\D+",""));
            	
            	if (sFrame < 1 || 9000 < sFrame) {
            		return;
            	} else if (eFrame < 1 || 9000 < eFrame) {
            		return;
            	}
            	
            	// Check uniqueness of link name
            	if (linkNameALst.contains(textArea.getText())) {
            		return;
            	}
            		
            	linkNameALst.add(textArea.getText());
            	
            	String data = "";
            	data += primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + String.format("%04d", sFrame) + ".rgb");
            	data += "," + primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + String.format("%04d", eFrame) + ".rgb");
            	data += "," + secondaryPath;
            	data += "," + boxX;
            	data += "," + boxY;
            	data += "," + boxW;
            	data += "," + boxH;
            	//System.out.println("data 1 looks like " + data);
            	dataALst.add(data);

            	Button btn = new Button();
            	btn.setLayoutX(0);
                btn.setLayoutY(27*btnNum);
            	linkPane.getChildren().add(btn);
            	btn.setText(textArea.getText());
            	btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                    	try {
                    		// Change the frame to the starting point
                    		
                    		int idx = linkNameALst.indexOf(textArea.getText());
                    		String startPath = dataALst.get(idx).split(",")[0];
                    		String frameNumber = startPath.substring(startPath.length()-8, startPath.length()-4);
                    		primaryPath = primaryRootPath + "/" + (primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + frameNumber + ".rgb");
                    		changeFrame(paneL, createBufferedImg(new File(primaryPath)));
                    		
                    		// Change slider
    	            		
    	            		sliderL.setValue(Double.parseDouble(frameNumber));
    	            		textL.setText("Frame " + frameNumber);
                    		
                    		final Stage editStage = new Stage();
                    		editStage.initModality(Modality.APPLICATION_MODAL);
                    		editStage.initModality(Modality.NONE);

                            StringWriter sw4 = new StringWriter();
                            String exceptionText4 = sw4.toString();
                            TextArea editTxtArea = new TextArea(exceptionText4);
                            editTxtArea.setText(textArea.getText());
                            editTxtArea.setPrefSize(350, 7);
                            
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
                                	linkNameALst.set(linkNameALst.indexOf(textArea.getText()), editTxtArea.getText());
                                	editStage.close();
                                }
                            });
                            
                            VBox vbox2 = new VBox();
                            vbox2.getChildren().addAll(linkNameLbl, editTxtArea, cancel, save);
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
    	JSONArray jsonArray = new JSONArray();
    	int idx = 0;
    	for (String it : dataALst) {
    		obj1 = new JSONObject();
            obj2 = new JSONObject();
            
            String[] data = it.split(",");
            
            obj1.put("primaryStart", data[0]);
            obj1.put("primaryEnd", data[1]);
            obj1.put("secondary", data[2]);
            obj1.put("x", data[3]);
            obj1.put("y", data[4]);
            obj1.put("width", data[5]);
            obj1.put("height", data[6]);
            obj2.put(linkNameALst.get(idx), obj1);
            jsonArray.add(obj2);

            idx++;
    	}
    	
    	try (FileWriter file = new FileWriter(primaryRootPath + "/" + primaryRootPath.substring(primaryRootPath.lastIndexOf("/") + 1) + ".json")) {
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (IOException exc) {}
    }
}
