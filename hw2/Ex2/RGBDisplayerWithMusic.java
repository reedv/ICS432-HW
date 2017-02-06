import java.io.File;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets; 
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class RGBDisplayerWithMusic extends Application {

    private TextField textFieldR;
    private TextField textFieldG;
    private TextField textFieldB;
    private Circle circle;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

	HBox top_root = new HBox(5);

	textFieldR = new TextField(); textFieldR.setPrefWidth(50);
	textFieldG = new TextField(); textFieldG.setPrefWidth(50);
	textFieldB = new TextField(); textFieldB.setPrefWidth(50);

	// turn-custom button
	Button customButton = new Button("Turn custom");
	customButton.setOnAction(
                new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) { 
                            try {
                                circle.setFill(Color.rgb(
                                                         Integer.parseInt(textFieldR.getText()),
                                                         Integer.parseInt(textFieldG.getText()),
                                                         Integer.parseInt(textFieldB.getText())
                                                         ));
                            } catch (NumberFormatException e) {
                                System.out.println("Enter integers in the text fields!!");
                                circle.setFill(Color.BLACK);
                            } catch (java.lang.IllegalArgumentException e) {
                                System.out.println("Enter valid integers in the text fields!!");
                                circle.setFill(Color.BLACK);
                            }
                        }
                });

	// Red button
	Button redButton = new Button("Turn red");
	redButton.setOnAction(
                new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) { 
                            circle.setFill(Color.RED);
                        }
                });
        
    // play-music button
    Button musicButton = new Button("Play music");
        musicButton.setOnAction(
                new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent event) {
                        // open filechooser and get selected filepath
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Open Resource File");
                        File soundFile = fileChooser.showOpenDialog(primaryStage);
                        System.out.println("Selected file: " + soundFile.getAbsolutePath());
                        // spin off worker thread
                        Thread musicPlayerThread = new Thread(new ThreadEx2(soundFile));
                        musicPlayerThread.start();
                    }
                });

	// Quit button
	Button quitButton = new Button("Quit");
	quitButton.setOnAction(
                new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) { 
                            System.exit(0);
                        }
                });

	// Building the top row of widgets
	top_root.getChildren().add(new Label("R: "));	
	top_root.getChildren().add(textFieldR);
	top_root.getChildren().add(new Label("G: "));	
	top_root.getChildren().add(textFieldG);
	top_root.getChildren().add(new Label("B: "));	
	top_root.getChildren().add(textFieldB);
	top_root.getChildren().add(customButton);
	top_root.getChildren().add(redButton);
    top_root.getChildren().add(musicButton);
	top_root.getChildren().add(quitButton);
        
	// Bottom panel
	Pane bottom_root = new Pane();
	bottom_root.setPrefSize(600,200);
        circle = new Circle(300, 120, 80);
        circle.setFill(Color.RED);
        bottom_root.getChildren().addAll(circle);

	// Overall GUI
	VBox root = new VBox();
	root.getChildren().addAll(top_root, bottom_root);
        primaryStage.setScene(new Scene(root, 600, 250));
        primaryStage.show();
    }
}
