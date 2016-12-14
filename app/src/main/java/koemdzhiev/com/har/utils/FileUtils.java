package koemdzhiev.com.har.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Created by Georgi on 9/21/2016.
 */

public class FileUtils {
    private static final String DIRECTORY_HAR = "HAR";

    public static void saveCurrentDataToArffFile(Context context, Instances instances,
                                                 String activityLabel, String userName) {
        String formattedUserName = userName.replace(" ", "_");
        File path = Environment.getExternalStoragePublicDirectory(DIRECTORY_HAR);
        // have the object build the directory structure, if needed.
        path.mkdirs();
        File file = new File(path, "/" + "HAR_" + activityLabel + "_" + formattedUserName + "_" +
                System.currentTimeMillis() + ".arff");

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(instances.toString());
            writer.flush();
            writer.close();
            Toast.makeText(context, "File Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
        }

        Log.d(FileUtils.class.getSimpleName(), "DATA SAVED TO A ARFF file");
    }

    @Nullable
    public static Instances readARFFFileSchema(Context context) {
        BufferedReader reader = null;
        Instances instances = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("schema_file.arff")));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            instances = arff.getData();
            instances.setClassIndex(instances.numAttributes() - 1);

//            Log.i(FileUtils.class.getSimpleName(), "Schema read successfully ->" + instances.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return instances;
    }
}
