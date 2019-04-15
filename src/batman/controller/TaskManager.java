package batman.controller;

import batman.model.MonteCarlo;
import batman.model.Draw;
import batman.model.Tuple;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.pow;

public class TaskManager extends Task {
    private long pointsToBeProcessedCount;
    private double max;
    private double min;
    private GraphicsContext graphicsContext;
    private BufferedImage bufferedImage;
    void init(long pointsToBeProcessedCount, GraphicsContext graphicsContext,
              double min, double max){
        this.pointsToBeProcessedCount = pointsToBeProcessedCount;
        this.graphicsContext = graphicsContext;
        this.min=min;
        this.max=max;
        int width = (int)graphicsContext.getCanvas().getWidth();
        int height = (int)graphicsContext.getCanvas().getHeight();
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D temp = this.bufferedImage.createGraphics();
        temp.setColor(Color.blue);
        temp.fill(new Rectangle(width,height));
        temp.dispose();
    }
    private Timeline updateProgressPeriodically;
    public void updateQueue(MonteCarlo [] monteCarloArray, BlockingQueue blockingQueue) {
        MonteCarlo maxMonteCarlo = monteCarloArray[0];
        int max = 0;
        for (MonteCarlo monteCarlo : monteCarloArray) {
            if (monteCarlo.getPointsSize() > max) {
                max = monteCarlo.getPointsSize();
                maxMonteCarlo = monteCarlo;
            }
        }
        maxMonteCarlo.setPriority(true);
    }
    protected Object call(){
        System.out.println("started");
        int maxProcesses = Controller.getProcessesCount();
        System.out.println(maxProcesses);
        MonteCarlo []monteCarloArray = new MonteCarlo[maxProcesses];
        Thread [] monteCarloThreads = new Thread[maxProcesses];
        BlockingQueue <ArrayList<Tuple<Double,Double>>> blockingQueue = new ArrayBlockingQueue<>(10);
        MonteCarlo.init(min, max, blockingQueue, pointsToBeProcessedCount/maxProcesses, Controller.getMaxBatchSize());
        for (int i=0; i< maxProcesses; i++){
            monteCarloArray[i] = new MonteCarlo();
            monteCarloThreads[i] = new Thread(monteCarloArray[i]);
            monteCarloThreads[i].start();
    }
        Draw.init(graphicsContext,bufferedImage, max-min);
        Draw draw = new Draw(blockingQueue);
        Thread drawThread = new Thread(draw);
        drawThread.start();
        Timeline updateQueueTimeline;
        updateQueueTimeline = new Timeline(new KeyFrame(Duration.seconds(1.0/100), new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                updateQueue(monteCarloArray, blockingQueue);
            }
        }));
        updateProgressPeriodically = new Timeline(new KeyFrame(Duration.seconds(1.0/Controller.getRefreshRate()), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isCancelled()){
                    for (MonteCarlo monteCarlo : monteCarloArray){
                        monteCarlo.cancel();
                    }
                    draw.cancel();
                    updateProgressPeriodically.stop();
                }
                double shot = 0;
                double hit = 0;
                for (MonteCarlo monteCarlo : monteCarloArray){
                    shot+= monteCarlo.getTotalProcessed();
                    hit += monteCarlo.getTotalHits();
                }
                //System.out.println("hit: " + hit + "\tshot:" + shot);
                double percentage=0;
                double integral=0;

                if (shot!=0){
                    percentage = BigDecimal.valueOf(100 * hit / shot).
                            setScale(3, RoundingMode.HALF_UP).
                            doubleValue();
                    integral = BigDecimal.valueOf(percentage / 100 * pow(max - min, 2)).
                            setScale(3, RoundingMode.HALF_UP).
                            doubleValue();
                }
                updateMessage("Points processed: " + (int)shot
                        + "\nPoints hit: " + (int)hit
                        + "\nPercentage hit: " + percentage + "%"
                        + "\nArea(integral): " + integral);
                updateProgress(shot, pointsToBeProcessedCount);

            }
        }));
        updateProgressPeriodically.setCycleCount(Timeline.INDEFINITE);
        updateProgressPeriodically.play();
        for (Thread monteCarlo : monteCarloThreads){
            System.out.println("finito!");
            try {
                monteCarlo.join();
            }catch(Exception e){
                System.out.println("it's fine if you clicked cancel 1/2");
                e.printStackTrace();
                System.out.println("it's fine if you clicked cancel 2/2");
            }
        }
        draw.setFinished();
        try {
            drawThread.join();
        }catch(Exception e){
            System.out.println("it's fine if you clicked cancel 1/2");
            e.printStackTrace();
            System.out.println("it's fine if you clicked cancel 2/2");
        }
        updateMessage(getMessage() + "\n finished");
        return null;
    }

}
