package koemdzhiev.com.weka_test.common.data;

import java.util.ArrayList;

/**
 * Created by Georgi on 9/19/2016.
 */
public class TimeSeries extends ArrayList<Point> {
    /**
     * The time series description
     */
    private String label;

    /**
     * The time series unique ID
     */
    private String id;

    public TimeSeries(String label, String id) {
        this.label = label;
        this.id = id;
    }

    /**
     * Adds a point to the series if there is no other point at the same time instant
     */
    public boolean addPoint(Point p) {
        if ((p != null) && (!this.contains(p))) {
            return super.add(p);
        } else {
            return false;
        }
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public boolean add(Point p) {
        throw new UnsupportedOperationException("This method is not supported. Use addPoints instead.");
    }

    public void add(int x, Point p) {
        throw new UnsupportedOperationException("This method is not supported. Use addPoints instead.");
    }
}
