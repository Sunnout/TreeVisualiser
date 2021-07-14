package plumtree.utils;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Random;

public class Coordinate {

    private static final Logger logger = LogManager.getLogger(Coordinate.class);

    public static final double Ce = 0.5;
    public static final double Cc = 0.25;

    private double x;
    private double y;

    private double height;

    private double error;

    private static final DecimalFormat df1 = new DecimalFormat("000.00");
    private static final DecimalFormat df2 = new DecimalFormat("#.##");
    private static final DecimalFormat df3 = new DecimalFormat("0.00");
    static {
        df1.setPositivePrefix("+");
    }

    public Coordinate(double x, double y, double height) {
        this(x, y, height, 1);
    }

    public Coordinate(double x, double y, double height, double error) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.error = error;
    }

    public double distance(Coordinate c) {
        return magnitude(difference(c));
    }

    public Coordinate difference(Coordinate c) {
        double x = c.x - this.x;
        double y = c.y - this.y;

        double height = this.height + c.height;

        return new Coordinate(x, y, height);
    }

    public Coordinate getDirection(Coordinate c) {
        Coordinate dist = difference(c);
        double length = magnitude(dist);
        if (length > 0)
            return new Coordinate(dist.x / length, dist.y / length, 0);
        else
            return new Coordinate(0, 0, 0);
    }

    public static Coordinate getRandomDirection(Random rnd) {
        Coordinate dist = new Coordinate(rnd.nextInt(), rnd.nextInt(), 0);
        double length = magnitude(dist);
        if (length > 0)
            return new Coordinate(dist.x / length, dist.y / length, 0);
        else
            return new Coordinate(0, 0, 0);
    }

    public static double magnitude(Coordinate c) {
        return Math.sqrt(Math.pow(c.x, 2) + Math.pow(c.y, 2)) + c.height;
    }

    public static Coordinate scale(Coordinate c, double a) {
        return new Coordinate(c.x * a, c.y * a, c.height * a);
    }

    public void add(Coordinate other) {
        this.x += other.x;
        this.y += other.y;
        this.height += other.height;
    }

    public void setError(double newError) {
        this.error = newError;
    }

    public static int closestToTarget(Coordinate target, Coordinate c1, Coordinate c2) {
        double d1 = target.distance(c1) * (c1.error + 1);
        double d2 = target.distance(c2) * (c2.error + 1);
        //System.out.println("dist("+target + ", " + c1 + ")=" + d1);
        //System.out.println("dist("+target + ", " + c2 + ")=" + d2);
        return Double.compare(d1, d2);
    }

    public void update(double rtt, Coordinate other, Random rnd) {
        rtt = Math.max(rtt, 1);
        double weight = this.getError() / (this.getError() + other.getError());

        double dist = this.distance(other);

        Coordinate u = dist == 0 ?
                Coordinate.getRandomDirection(rnd) :
                this.getDirection(other);

        //logger.warn("({},{}) -> ({},{}) = ({},{})", df2.format(this.x), df2.format(this.y),
        //        df2.format(other.x), df2.format(other.y), df2.format(u.x), df2.format(u.y));

        double sampleError = Math.abs(dist - rtt) / rtt;

        double newError = sampleError * Ce * weight + this.getError() * (1 - Ce * weight);

        double step = Cc * weight;

        this.add(Coordinate.scale(u, step * (dist - rtt)));
        this.setError(newError);
    }

    public double getError() {
        return error;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "{(" + df1.format(x) + ", " + df1.format(y) + ")"
                + ", h=" + df2.format(height) +
                ", e=" + df3.format(error) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x &&
                y == that.y &&
                height == that.height &&
                Double.compare(that.error, error) == 0;
    }

    public void serialize(ByteBuf out) {
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.height);
        out.writeDouble(this.error);
    }

    public static Coordinate deserialize(ByteBuf in) {
        return new Coordinate(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
    }

    public Coordinate copy(){
        return new Coordinate(this.x, this.y, this.height, this.error);
    }

}
