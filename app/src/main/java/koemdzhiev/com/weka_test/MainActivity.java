package koemdzhiev.com.weka_test;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import koemdzhiev.com.weka_test.common.data.Point;
import koemdzhiev.com.weka_test.common.data.TimeSeries;
import koemdzhiev.com.weka_test.common.data.TimeWindow;
import koemdzhiev.com.weka_test.common.feature.FeatureSet;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // 5 Seconds
    private static final long WINDOW_LENGTH = 5000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private long windowBegTime = -1;
    private String activityLabel;
    private SensorManager sensorManager;
    private Sensor accSensor;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activityLabel = "walking";
        this.accXSeries = new TimeSeries(activityLabel, "accX_");
        this.accYSeries = new TimeSeries(activityLabel, "accY_");
        this.accZSeries = new TimeSeries(activityLabel, "accZ_");
        this.window = new TimeWindow(activityLabel);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        this.instanceHeader = getInstanceHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, accSensor);
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
        Log.d(TAG,"issueTimeWindow method called" + String.format("XarraySize: %d YarraySize: %d ZarraySize: %d",accXSeries.size(),accYSeries.size(),accZSeries.size()));
        FeatureSet featureSet = new FeatureSet(window);
        featureSet.setActivityLabel(activityLabel);
        Log.d(TAG,"FeatureSet.toInstance: " +  featureSet.toInstance(this.instanceHeader));
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
        BufferedReader reader = null;
        Instances instances = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("schema_file.arff")));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            instances = arff.getData();
            instances.setClassIndex(instances.numAttributes() - 1);

            Log.i(TAG, "Schema read successfully ->" + instances.toString());

        } catch (IOException e ) {
            e.printStackTrace();
        }

        return instances;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        final float alpha = 0.8f;
        final double[] gravity = new double[3];
        final double[] linear_acc = new double[3];
        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acc[0] = event.values[0] - gravity[0];
        linear_acc[1] = event.values[1] - gravity[1];
        linear_acc[2] = event.values[2] - gravity[2];
        accXSeries.addPoint(new Point(event.timestamp, linear_acc[0]));
        accYSeries.addPoint(new Point(event.timestamp, linear_acc[1]));
        accZSeries.addPoint(new Point(event.timestamp, linear_acc[2]));

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
