package batman.model;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;

import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Draw extends Task {
    private static GraphicsContext graphicsContext;
    private static BufferedImage bufferedImage;
    private static double range;
    private boolean finished;
    public void setFinished(){
        finished = true;
    }
    public static void init(GraphicsContext graphicsContext, BufferedImage bufferedImage, double range){
        Draw.graphicsContext = graphicsContext;
        Draw.bufferedImage = bufferedImage;
        Draw.range = range;
    }
    private BlockingQueue <ArrayList<Tuple<Double,Double>>>blockingQueue;
    public Draw(BlockingQueue<ArrayList<Tuple<Double,Double>>> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    protected Object call() throws Exception {
        int i=0;
        while (!isCancelled() && !finished) {
            List<Tuple<Double,Double>> points = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
            if (points == null) continue;
            for (Tuple<Double, Double> point : points) {
                transformPoint(point, (int) graphicsContext.getCanvas().getWidth(), (int) graphicsContext.getCanvas().getHeight(), range);
                bufferedImage.setRGB(point.x.intValue(), point.y.intValue(), Color.yellow.getRGB());
                if (isCancelled()) break;
            }

            graphicsContext.drawImage(SwingFXUtils.toFXImage(bufferedImage, null), 0, 0);
        }
            return null;
    }


    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        return (Integer) super.get();
    }
    private void transformPoint(Tuple <Double, Double> point, int windowWidth, int windowHeight, double range){
        point.x = point.x/range *windowWidth;
        point.x += windowWidth/2.;

        point.y = -point.y/range *windowHeight;
        point.y += windowHeight/2.;
    }
}
