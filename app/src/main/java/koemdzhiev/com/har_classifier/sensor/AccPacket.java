package koemdzhiev.com.har_classifier.sensor;

/**
 * Created by Georgi on 9/19/2016.
 * CURRENTLY not used
 */
public class AccPacket extends Packet {
    private double[] accelerometerData;

    public AccPacket() {
        this.accelerometerData = new double[3];
    }

    public double getX() {
        return this.accelerometerData[0];
    }

    public double getY() {
        return this.accelerometerData[1];
    }

    public double getZ() {
        return this.accelerometerData[2];
    }
}
