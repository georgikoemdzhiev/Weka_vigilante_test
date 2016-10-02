package koemdzhiev.com.har.common.feature;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import koemdzhiev.com.har.common.data.TimeSeries;
import koemdzhiev.com.har.common.data.TimeWindow;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Georgi on 9/19/2016.
 */
public class FeatureSet extends Hashtable<String, Double> {

    private static final String TAG = FeatureSet.class.getSimpleName();
    private String activityLabel;

    public FeatureSet(String activityLabel) {
        this.activityLabel = activityLabel;
    }

    public FeatureSet(double[] values, String[] attributes) {
        if ((values == null) || (attributes == null) || (values.length) != (attributes.length)) {
            throw new IllegalArgumentException("Invalid arguments to create a feature set");
        }
        for (int i = 0; i < values.length; i++) {
            setAttribute(attributes[i], values[i]);
        }
    }

    /**
     * Creates a new feature set by extracting all features from the given time window
     */
    public FeatureSet(TimeWindow window) throws Exception {
        if (window == null) {
            throw new IllegalArgumentException("Cannot extract features from a null time window!");
        }

        Set<String> keys = window.keySet();
        for (String key : keys) {
            TimeSeries series = window.getTimeSeries(key);

            /** Compute some statistical features */
            StatisticalFeatureExtractor sta = new StatisticalFeatureExtractor(series, activityLabel);
            double mean = sta.computeMean();
            double stdv = sta.computeSTDV();
            double var = sta.computeVariance();
            double rms = sta.computeRMS();
            double mad = sta.computeMAD();

            /** Compute some structural features */
            StructuralFeatureExtractor str = new StructuralFeatureExtractor(series, activityLabel);
            int degree = 3;
            double[] coef = str.computeLeastSquares(degree);

            /** Add them to the feature set */
            String id = series.getId();
            this.put(id + "_mean", mean);
            this.put(id + "_stdv", stdv);
            this.put(id + "_rms", rms);
            this.put(id + "_mad", mad);
            this.put(id + "_var", var);
            this.put(id + "_coef1", coef[0]);
            this.put(id + "_coef2", coef[1]);
            this.put(id + "_coef3", coef[2]);

        }
    }

    public void addFeatures(FeatureSet featureSet) {
        for (String name : featureSet.keySet()) {
            Double value = featureSet.get(name);
            this.put(name, value);
        }
    }

    public Double getValue(String attName) {
        return super.get(attName);
    }

    public void setAttribute(String attName, Double value) {
        super.put(attName, value);
    }

    /**
     * Converts a FeatureSet to WEKA Instance
     * Parameter instanceHeader is an Instances object containing the attributes that the Instance should have.
     */

    public Instance toInstance(Instances instanceHeader) {
        DenseInstance instance = null;
        if (instanceHeader != null) {
            instance = new DenseInstance(instanceHeader.numAttributes());
            instance.setDataset(instanceHeader);
            instance.setClassValue(activityLabel);

            Enumeration e = instanceHeader.enumerateAttributes();
            while (e.hasMoreElements()) {
                Attribute attr = (Attribute) e.nextElement();
                if (this.containsKey(attr.name())) {
                    if (attr.isNominal()) {
                        instance.setValue(attr, "" + this.getValue(attr.name()));
                    } else {
                        instance.setValue(attr, this.getValue(attr.name()));
                    }
                } else {
                    /* Attributes not found in this featureSet are set to zero.
                     * Otherwise the classifier cannot evaluate the instance */
                    if (attr.isNominal()) {
                        instance.setValue(attr, "0.0");
                    } else {
                        instance.setValue(attr, 0);
                    }
                }

            }
        }
        return instance;
    }

    public String getActivityLabel() {
        return activityLabel;
    }

    public void setActivityLabel(String activityLabel) {
        this.activityLabel = activityLabel;
    }

}
