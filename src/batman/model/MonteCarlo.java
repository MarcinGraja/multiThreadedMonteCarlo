package batman.model;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class MonteCarlo extends Task{
    private int totalProcessed=0;
    private int totalHits=0;
    private static double min;
    private static double max;
    private static BlockingQueue <ArrayList <Tuple<Double, Double>>> blockingQueue;
    private static long pointsToCheckCount;
    private static int maxBatchSize;
    public static void init(double min, double max, BlockingQueue <ArrayList <Tuple<Double, Double>>> blockingQueue,
                            long pointsToCheckCount, int maxBatchSize){
        MonteCarlo.min = min;
        MonteCarlo.max = max;
        MonteCarlo.blockingQueue = blockingQueue;
        MonteCarlo.pointsToCheckCount = pointsToCheckCount;
        MonteCarlo.maxBatchSize = maxBatchSize;
    }
    private boolean priority = false;
    private ArrayList <Tuple<Double, Double>> points;
    @Override
    protected Object call() throws Exception {
        Random random = new Random();
        points = new ArrayList<>();
        while (!isCancelled() && totalProcessed < pointsToCheckCount){
            totalProcessed++;
            double x = min + (max - min) * random.nextDouble(); // range of <min,max>
            double y = min + (max - min) * random.nextDouble();
            if (Equation.calc(x,y)){
                points.add(new Tuple<>(x,y));
                totalHits++;
            }
            if (priority || points.size() > maxBatchSize){
                blockingQueue.put(points);
                points = new ArrayList<>();
                priority = false;
            }
        }
        System.out.println("processed:" + totalProcessed + "\t hits:" + totalHits);
        return null;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }
    public int getPointsSize(){
        return points.size();
    }
    public int getTotalProcessed() {
        return totalProcessed;
    }
    public int getTotalHits() {
        return totalHits;
    }
}
