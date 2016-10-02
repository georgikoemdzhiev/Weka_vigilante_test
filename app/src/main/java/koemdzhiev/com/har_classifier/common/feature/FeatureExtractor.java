package koemdzhiev.com.har_classifier.common.feature;

import koemdzhiev.com.har_classifier.common.data.TimeSeries;

/**
 * Created by Georgi on 9/19/2016.
 */
public abstract class FeatureExtractor {
    //** The eries features will be extracted from */
    protected TimeSeries series;

    //** The set of features extracted from the given time series */
    protected FeatureSet featureSet;

    public FeatureExtractor(TimeSeries series, String activityLabel) {
        this.series = series;
        this.featureSet = new FeatureSet(activityLabel);
    }
}
