package batman.controller;

import batman.model.MonteCarlo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.math.BigInteger;

import static java.lang.Long.MAX_VALUE;

public class Controller {
    private double max = 8;
    private double min = -8;
    private Timeline updateProgressPeriodically;
    private static int refreshRate;
    private static int processesCount;
    private static int maxBatchSize;
    @FXML
    TextField maxBatchSizeTextField;
    @FXML
    TextArea outputTextArea;
    @FXML
    TextField numberOfPoints;
    @FXML
    private Canvas canvas;
    private TaskManager taskManager;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button stopButton;
    @FXML
    private Button startButton;
    @FXML
    private TextField refreshRateTextField;
    @FXML
    private TextField processesCountTextField;
    @FXML
    private void handleStart(){
        refreshRate = Integer.parseInt(refreshRateTextField.getText());
        if (taskManager == null || !taskManager.isRunning()) {
            maxBatchSize = Integer.parseInt(maxBatchSizeTextField.getText());
            taskManager = new TaskManager();
            processesCount = Integer.parseInt(processesCountTextField.getText());
            long pointsCount = Math.min(Integer.parseInt(numberOfPoints.getText()), Long.MAX_VALUE);
            taskManager.init(pointsCount, canvas.getGraphicsContext2D(), min, max);
            new Thread(taskManager).start();
            progressBar.progressProperty().bind(taskManager.progressProperty());

             if (updateProgressPeriodically == null){
                 updateProgressPeriodically = new Timeline(new KeyFrame(Duration.seconds(1.0/getRefreshRate()), new EventHandler<ActionEvent>() {

                     @Override
                     public void handle(ActionEvent event) {
                         updateOutput(taskManager.getMessage());
                     }
                 }));
             }
            updateProgressPeriodically.setCycleCount(Timeline.INDEFINITE);
            updateProgressPeriodically.play();
        }

    }
    @FXML
    private void handleStop(){
        taskManager.cancel();
    }
    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(gc.getCanvas().getLayoutX(),
                gc.getCanvas().getLayoutY(),
                gc.getCanvas().getWidth(),
                gc.getCanvas().getHeight());
    }
    @FXML
    public void initialize(){
        drawShapes(canvas.getGraphicsContext2D());
        numberOfPoints.setTooltip(new Tooltip("number of points to be checked"));
        startButton.setTooltip(new Tooltip("Starts new calculation. Just updates refresh rate if pressed during execution"));
        stopButton.setTooltip(new Tooltip("Stops the calculation and throws away the result"));
        refreshRateTextField.setTooltip(new Tooltip("sets how many times per second you want to refresh"));
        processesCountTextField.setTooltip(new Tooltip("how many calculating processes are allowed"));
    }
    public static int getRefreshRate(){
        return refreshRate > 0 ? refreshRate : 1;
    }
    public void updateOutput(String s){
        outputTextArea.setText(s);
    }
    public void addToOutput(String s){
        outputTextArea.setText(outputTextArea.getText() + s);
    }
    static int getProcessesCount(){
        return processesCount > 0? processesCount : 1;
    }
    public static int getMaxBatchSize() {
        return maxBatchSize;
    }
}
