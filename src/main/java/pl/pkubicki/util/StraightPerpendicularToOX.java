package pl.pkubicki.util;

import java.util.Objects;

public class StraightPerpendicularToOX extends Straight{
    private double x;

    public StraightPerpendicularToOX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StraightPerpendicularToOX that = (StraightPerpendicularToOX) o;
        return Double.compare(that.getX(), getX()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getX());
    }

    @Override
    public String toString() {
        return "x= " + x;
    }
}
