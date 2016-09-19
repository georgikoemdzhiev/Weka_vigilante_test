package koemdzhiev.com.weka_test.sensor;

/**
 * Created by Georgi on 9/19/2016.
 */
public abstract class Packet {

    public int getSize() {
        return 0;
    }

    public double[] getRawData() {
        return new double[0];
    }

    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

}
