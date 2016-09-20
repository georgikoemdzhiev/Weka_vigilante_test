package koemdzhiev.com.weka_test.sensor;

import java.util.Observable;
import java.util.Observer;

import koemdzhiev.com.weka_test.common.data.Point;
import koemdzhiev.com.weka_test.common.data.TimeSeries;
import koemdzhiev.com.weka_test.common.data.TimeWindow;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Created by Georgi on 9/19/2016.
 * CURRENTLY not used
 */
public class SensorManager implements Observer {
    // 5 Seconds
    private static final long WINDOW_LENGTH = 5000;
    private long windowBegTime = -1;
    private String activityLabel;
    /**
     * A classification model trained in the server
     */
    private Classifier classifier;

    /**
     * The data schema read from an empty ARFF file
     */
    private Instances instanceHeader;

    private TimeSeries accXSeries, accYSeries, accZSeries;
    private TimeWindow window;
    //...//

    public SensorManager(String activityLabel) {
        this.activityLabel = activityLabel;
        this.accXSeries = new TimeSeries(activityLabel, "accX_");
        this.accYSeries = new TimeSeries(activityLabel, "accY_");
        this.accZSeries = new TimeSeries(activityLabel, "accZ_");
        this.window = new TimeWindow(activityLabel);
    }

    @Override
    public void update(Observable sensor, Object packet) {
        if (packet instanceof AccPacket) {
            AccPacket pc = (AccPacket) packet;
            accXSeries.addPoint(new Point(pc.getTimeStamp(), pc.getX()));
            accYSeries.addPoint(new Point(pc.getTimeStamp(), pc.getY()));
            accZSeries.addPoint(new Point(pc.getTimeStamp(), pc.getZ()));
        }

        if (System.currentTimeMillis() - windowBegTime > WINDOW_LENGTH) {
            if (windowBegTime > 0) {
                window.addTimeSeries(accXSeries);
                window.addTimeSeries(accYSeries);
                window.addTimeSeries(accZSeries);

                issueTimeWindow(window);
                resetTimeSeries();
            }

            windowBegTime = System.currentTimeMillis();
        }
    }

    private void resetTimeSeries() {
        this.accXSeries.clear();
        this.accYSeries.clear();
        this.accZSeries.clear();
    }

    public void issueTimeWindow(TimeWindow window) {
        // TODO: 9/19/2016 extract features
        //Create a FeatureSet instance and use its toInstance method to create weka instance
        // use the classifier to classify the instance
    }

    public void initializeClassifier() {
        instanceHeader = getInstanceHeader();
        classifier = getClassifier();
    }

    private Classifier getClassifier() {
        // TODO: 9/19/2016 Add logic to make a network call to download the trained offline model/classifier
        return null;
    }

    public Instances getInstanceHeader() {
        // TODO: 9/19/2016 Add logic to read an empty arff file to set the shceme of the arff file
        return null;
    }
}
