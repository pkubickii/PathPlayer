package pl.pkubicki.util;

import java.util.Objects;

public class Straight {
    private double factorA;
    private double factorB;

    public Straight(double factorA, double factorB) {
        this.factorA = factorA;
        this.factorB = factorB;
    }

    public double getFactorA() {
        return factorA;
    }

    public void setFactorA(double factorA) {
        this.factorA = factorA;
    }

    public double getFactorB() {
        return factorB;
    }

    public void setFactorB(double factorB) {
        this.factorB = factorB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Straight that = (Straight) o;
        return Double.compare(that.getFactorA(), getFactorA()) == 0 && Double.compare(that.getFactorB(), getFactorB()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFactorA(), getFactorB());
    }

    @Override
    public String toString() {
        if (factorB > 0.0)
            return "y=" + factorA + "x" + " + " + factorB;
        else
            return "y=" + factorA + "x " + factorB;
    }
}
