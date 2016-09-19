package koemdzhiev.com.weka_test.common.data;

import java.util.Hashtable;

import koemdzhiev.com.weka_test.common.feature.FeatureSet;

/**
 * Created by Georgi on 9/19/2016.
 */
public class TimeWindow extends Hashtable<String, TimeSeries> {

    /**
     * The activity label associated with this time window
     */
    private String label;

    /**
     * The activity label associated with this time window
     */
    private FeatureSet featureSet;

    public TimeWindow(String label) {
        this.label = label;
    }

    public void addTimeSeries(TimeSeries series) {
        super.put(series.getId(), series);
    }

    public TimeSeries getTimeSeries(String seriesId) {
        return super.get(seriesId);
    }

    public TimeSeries put(String key, TimeSeries series) {
        throw new UnsupportedOperationException("Use method addTimeSeries rather than put!");
    }

    public TimeSeries get(String id) {
        throw new UnsupportedOperationException("Use method getTimeSeries rather than get!");
    }

    public FeatureSet getFeatureSet() {
        return this.featureSet;
    }

    public void setFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
