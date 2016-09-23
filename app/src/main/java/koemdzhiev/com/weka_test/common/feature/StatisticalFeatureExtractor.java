package koemdzhiev.com.weka_test.common.feature;

import java.util.Collections;

import koemdzhiev.com.weka_test.common.data.Point;
import koemdzhiev.com.weka_test.common.data.TimeSeries;

/**
 * Created by Georgi on 9/19/2016.
 */
public class StatisticalFeatureExtractor extends FeatureExtractor {
    private double mean;
    private double sumSquared;
    private double sumMad;
    private double variance;
    private double sumVar;
    private double mad;

    public StatisticalFeatureExtractor(TimeSeries series, String activityLabel) {
        super(series, activityLabel);
        prepareFeatures();
    }

    public FeatureSet computeFeatures() {
        featureSet.put("MEAN_" + series.getId(), mean);
        featureSet.put("STDV_" + series.getId(), computeSTDV());
        featureSet.put("RMS_" + series.getId(), computeRMS());
        featureSet.put("MAD_" + series.getId(), computeMAD());
        featureSet.put("VAR_" + series.getId(), computeVariance());
        return featureSet;
    }

    private double getMean() {
        return mean;
    }

    public void recomputeFeatures(TimeSeries series) {
        this.series = series;
        computeFeatures();
    }

    private void prepareFeatures() {
        mean = 0;
        sumSquared = 0;
        sumMad = 0;
        variance = 0;
        sumVar = 0;

        for (Point p : series) {
            mean += p.getValue();
            sumSquared += Math.pow(p.getValue(), 2);
        }
        mean = mean / series.size();
        for (Point p : series) {
            sumMad += Math.abs(p.getValue() - mean);
            sumVar += Math.pow(p.getValue() - mean, 2);
        }
        variance = sumVar / (series.size() - 1);
        mad = sumMad / series.size();
    }

    public double computeSTDV() {
        return Math.sqrt(variance);
    }

    public double computeRMS() {
        return Math.sqrt(sumSquared / series.size());
    }

    public double computeMedian(TimeSeries series) {
        Collections.sort(series);

        if (series.size() % 2 == 1) {
            return series.get((series.size() - 1) / 2).getValue();
        } else {
            double lower = series.get(series.size() / 2 - 1).getValue();
            double upper = series.get(series.size() / 2).getValue();
            return (lower + upper) / 2;
        }
    }

    public double computeMAD() {
        return mad;
    }

    public double computeVariance() {
        return variance;
    }

    public double computeMean() {
        return mean;
    }
}
