package pl.pkubicki.util.geometry;

import java.util.Objects;

public class StraightPerpendicularToOY extends Straight {

    private double y;

    public StraightPerpendicularToOY(double y) {
        this.y = y;
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
        if (!super.equals(o)) return false;
        StraightPerpendicularToOY that = (StraightPerpendicularToOY) o;
        return Double.compare(that.getY(), getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getY());
    }

    @Override
    public String toString() {
        return "y= " + y;
    }
}
