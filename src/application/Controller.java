package application;

import java.awt.Label;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;

public class Controller {
	@FXML
    private Slider sliderR;
	@FXML
    private Slider sliderL;
	@FXML
    private Text textR;
	@FXML
    private Text textL;
	
	public void initialize() {
        // do initialization and configuration work...

        // trivial example, could also be done directly in the fxml:
		//System.out.println(sliderR.getValue());
		//System.out.println(sliderL.getValue());

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
    }
}
