package koemdzhiev.com.weka_test.common.feature;

import koemdzhiev.com.weka_test.common.data.TimeSeries;

/**
 * Created by Georgi on 9/21/2016.
 */

public class StructuralFeatureExtractor extends FeatureExtractor {

    public StructuralFeatureExtractor(TimeSeries series, String activityLabel) {
        super(series, activityLabel);
    }

    public FeatureSet computeFeatures(int degree) throws Exception {
        double[]  features = computeLeastSquares(degree + 1);
        int i = 0;
        for (double f : features) {
            featureSet.put("COEF_" + degree + "^" + i + "_" + series.getId(), f);
            i++;
        }

        return featureSet;
    }

    public double[] computeLeastSquares(int coefCount) throws Exception {
        if (coefCount > series.size()) {
            double[] coef1 = new double[coefCount];
            double[] coef2 = computeLeastSquares(series.size());
            for (int i = 0; i < series.size(); i++) {
                coef1[i] = coef2[i];
            }
            for (int i = series.size(); i < coefCount; i++) {
                coef1[i] = 0;
            }
            return coef1;
        } else {
            Jama.Matrix M = new Jama.Matrix(series.size(), coefCount);
            Jama.Matrix B;

            for (int j = 0; j < coefCount; j++) {
                for (int i = 0; i < series.size(); i++) {
                    M.set(i, j, Math.pow((series.get(i).getTime() - series.get
                            (0).getTime()), j));
                }
            }

            B = new Jama.Matrix(series.size(), 1);
            for (int i = 0; i < series.size(); i++) {
                B.set(i, 0, (series.get(i).getValue()));
            }

            return M.solve(B).getRowPackedCopy();
        }
    }
}
