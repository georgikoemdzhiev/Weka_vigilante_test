package koemdzhiev.com.weka_test;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import koemdzhiev.com.weka_test.common.data.Point;
import koemdzhiev.com.weka_test.common.data.TimeSeries;
import koemdzhiev.com.weka_test.common.data.TimeWindow;
import koemdzhiev.com.weka_test.common.feature.FeatureSet;
import koemdzhiev.com.weka_test.utils.FileUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
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

    /**
     * Object to store the dataSet which will be stored to a file on the devices storage
     */
    private Instances dataSet;

    private TimeSeries accXSeries, accYSeries, accZSeries;
    private TimeWindow window;
    // UI
    @BindView(R.id.saveBtn)
    Button mSaveButton;
    @BindView(R.id.clearBtn)
    Button mClearButton;
    @BindView(R.id.startRecBtn)
    Button mStartButton;
    @BindView(R.id.stopRecBtn)
    Button mStopButton;

    //...//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mStopButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        // This will change in the feature - the user must specify the activity...
        this.activityLabel = "waling";
        this.accXSeries = new TimeSeries(activityLabel, "accX_");
        this.accYSeries = new TimeSeries(activityLabel, "accY_");
        this.accZSeries = new TimeSeries(activityLabel, "accZ_");
        this.window = new TimeWindow(activityLabel);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        this.instanceHeader = getInstanceHeader();
        dataSet = instanceHeader;
    }

    private void resetTimeSeries() {
        this.accXSeries.clear();
        this.accYSeries.clear();
        this.accZSeries.clear();
    }

    public void issueTimeWindow(TimeWindow window) {
        // extract features, convert the featureSet to weka instance object and add it to a list
        //Create a FeatureSet instance and use its toInstance method to create weka instance
        // use the classifier to classify the instance (TODO)
        Log.d(TAG, "issueTimeWindow method called" + String.format("XarraySize: %d YarraySize: %d ZarraySize: %d", accXSeries.size(), accYSeries.size(), accZSeries.size()));
        FeatureSet featureSet = null;
        try {
            featureSet = new FeatureSet(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        featureSet.setActivityLabel(activityLabel);
//        Log.d(TAG, "FeatureSet.toInstance: " + featureSet.toInstance(this.instanceHeader));
        Log.d(TAG, "FeatureSet.toInstance: " + featureSet.toString());

        dataSet.add(featureSet.toInstance(this.instanceHeader));
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
        // read an empty arff file to set the shceme of the arff file
        Instances instances = FileUtils.readARFFFileSchema(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn:
                FileUtils.saveCurrentDataToArffFile(this, dataSet, activityLabel);
                dataSet.clear();
                break;
            case R.id.clearBtn:
                dataSet.clear();
                Toast.makeText(this, "Data cleared!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.startRecBtn:
                // start recording logic
                sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(this, "Recording", Toast.LENGTH_SHORT).show();
                break;

            case R.id.stopRecBtn:
                // stop recording logic
                sensorManager.unregisterListener(this, accSensor);
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
