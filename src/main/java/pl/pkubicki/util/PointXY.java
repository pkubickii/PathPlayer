package pl.pkubicki.util;

import java.util.Objects;

public class PointXY {
    private double x;
    private double y;

    public PointXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointXY pointXY = (PointXY) o;
        return Double.compare(pointXY.getX(), getX()) == 0 && Double.compare(pointXY.getY(), getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "PointXY{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
