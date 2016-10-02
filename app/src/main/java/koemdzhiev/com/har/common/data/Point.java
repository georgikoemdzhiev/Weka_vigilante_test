package koemdzhiev.com.har.common.data;

/**
 * Created by Georgi on 9/19/2016.
 */
public class Point implements Comparable<Point> {
    /**
     * The time instant
     */
    private long time;

    /**
     * The value of the variable of interest at  the given time
     */
    private double value;

    public Point(long time, double value) {
        this.time = time;
        this.value = value;
    }

    public double getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Compares two points according to their time instant value
     */
    @Override
    public int compareTo(Point p) {
        if (p == null) {
            throw new IllegalArgumentException("Cannot compare to null value");
        } else if (this.time < p.time) {
            return -1;
        } else if (this.time > p.time) {
            return 1;
        } else {
            return 0;
        }
    }
}
