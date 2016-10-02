package koemdzhiev.com.har;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import koemdzhiev.com.har.common.data.Point;
import koemdzhiev.com.har.common.data.TimeSeries;
import koemdzhiev.com.har.common.data.TimeWindow;
import koemdzhiev.com.har.common.feature.FeatureSet;
import koemdzhiev.com.har.utils.FileUtils;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    // 5 Seconds
    private static final long WINDOW_LENGTH = 5000;
    private static final String TAG = MainActivity.class.getSimpleName();
    // UI
    @BindView(R.id.saveBtn)
    Button mSaveButton;
    @BindView(R.id.clearBtn)
    Button mClearButton;
    @BindView(R.id.startRecBtn)
    Button mStartButton;
    @BindView(R.id.stopRecBtn)
    Button mStopButton;
    @BindView(R.id.user)
    Button mUserBtn;
    @BindView(R.id.currentActivity)
    Button mActivityTypeView;
    @BindView(R.id.numberOfInstances)
    TextView mNumberOfInstancesView;
    @BindView(R.id.classifedActivity)
    ListView mClassifiedActivity;
    private long windowBegTime = -1;
    private String activityLabel;
    private String userName = "DEFAULT";
    private SensorManager sensorManager;
    private Sensor accSensor;
    private PowerManager pm;
    private PowerManager.WakeLock mWakeLock;
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
    private TimeSeries accXSeries, accYSeries, accZSeries, accMSeries;
    private TimeWindow window;
    private ArrayList<String> classifiedActivities = new ArrayList<>();
    private ArrayAdapter<String> adapter;

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
        mActivityTypeView.setOnClickListener(this);
        mUserBtn.setOnClickListener(this);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, classifiedActivities);
        mClassifiedActivity.setAdapter(adapter);
        // Set the default activity to walking
        this.activityLabel = "walking";
        setUpTimeWindowAndTimeSeries();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Accelerometer_sensor_tag");
        mWakeLock.acquire();

        initializeClassifier();
        dataSet = instanceHeader;
    }

    private void setUpTimeWindowAndTimeSeries() {
        this.accXSeries = new TimeSeries(activityLabel, "accX_");
        this.accYSeries = new TimeSeries(activityLabel, "accY_");
        this.accZSeries = new TimeSeries(activityLabel, "accZ_");
        this.accMSeries = new TimeSeries(activityLabel, "accM_");
        this.window = new TimeWindow(activityLabel);
    }

    private void resetTimeSeries() {
        this.accXSeries.clear();
        this.accYSeries.clear();
        this.accZSeries.clear();
        this.accMSeries.clear();
        this.window.clear();
    }

    public void issueTimeWindow(TimeWindow window) {
        // extract features, convert the featureSet to weka instance object and add it to a list
        //Create a FeatureSet instance and use its toInstance method to create weka instance
        // use the classifier to classify the instance (TODO)
        Log.d(TAG, "issueTimeWindow method called" + String.format("XarraySize: %d YarraySize: %d ZarraySize: %d MarraySize: %d",
                accXSeries.size(), accYSeries.size(), accZSeries.size(), accMSeries.size()));
        FeatureSet featureSet = null;
        try {
            featureSet = new FeatureSet(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        featureSet.setActivityLabel(activityLabel);
        Log.d(TAG, "FeatureSet.toString: " + featureSet.toString());
        Log.d(TAG, "FeatureSet.toInstance: " + featureSet.toInstance(this.instanceHeader));
        double[] clasification;
        Instance instance = featureSet.toInstance(this.instanceHeader);
        try {
            clasification = classifier.distributionForInstance(instance);
            double classifiedClass = classifier.classifyInstance(instance);
            adapter.add("Classified:" + classifiedClass);
            Log.d(TAG, Arrays.toString(clasification));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSet.add(instance);

        //set the numberOfInstances view to the current dataSet size
        mNumberOfInstancesView.setText(dataSet.size() + "");
    }

    public void initializeClassifier() {
        instanceHeader = getInstanceHeader();
        classifier = getClassifier();
//        Log.d(TAG,"Classifier after deserialization: " + classifier.toString());
    }

    private Classifier getClassifier() {
        // Add logic to make a network call to download the trained offline model/classifier
        String filename = "J48.data";
        ObjectInputStream objectStream = null;
        Object obj = null;
        try {
            objectStream = new ObjectInputStream(getAssets().open(filename));
            obj = objectStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

//        Log.e(TAG, obj.getClass() + "" + obj.toString());

        if (obj instanceof Classifier) {
            Log.e(TAG, " obj is of class Classifier");
            return (Classifier) obj;
        }

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
        accMSeries.addPoint(new Point(event.timestamp, calculateMagnitude(linear_acc[0], linear_acc[1], linear_acc[2])));

        if (System.currentTimeMillis() - windowBegTime > WINDOW_LENGTH) {
            if (windowBegTime > 0) {
                window.addTimeSeries(accXSeries);
                window.addTimeSeries(accYSeries);
                window.addTimeSeries(accZSeries);
                window.addTimeSeries(accMSeries);

                issueTimeWindow(window);
                resetTimeSeries();
            }

            windowBegTime = System.currentTimeMillis();
        }
    }

    private double calculateMagnitude(double x, double y, double z) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn:
                FileUtils.saveCurrentDataToArffFile(this, dataSet, activityLabel, userName);
                dataSet.clear();
                break;
            case R.id.clearBtn:
                dataSet.clear();
                mNumberOfInstancesView.setText("0");
                Toast.makeText(this, "Data cleared!", Toast.LENGTH_SHORT).show();
                adapter.clear();
                break;
            case R.id.startRecBtn:
                // start recording logic
                sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME);
                mWakeLock.acquire();
                Toast.makeText(this, "Recording", Toast.LENGTH_SHORT).show();
                break;

            case R.id.stopRecBtn:
                // stop recording logic
                sensorManager.unregisterListener(this, accSensor);
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
                break;
            case R.id.currentActivity:
                showSetActivityDialog();
                break;
            case R.id.user:
                showSetUserNameDialog();
                break;
        }
    }

    private void showSetUserNameDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.user_name_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        userName = input.toString().trim();
                        mUserBtn.setText(userName);
                    }
                })
                .positiveText(R.string.dialog_positive_text)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.hide();
                    }
                })
                .show();
    }

    private void showSetActivityDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.activityType)
                .items(R.array.items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        MainActivity.this.activityLabel = text.toString();
                        mActivityTypeView.setText(text);
                    }
                })
                .show();
    }
}
