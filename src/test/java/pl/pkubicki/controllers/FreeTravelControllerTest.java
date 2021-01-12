package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import org.junit.jupiter.api.Test;
import pl.pkubicki.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FreeTravelControllerTest {
    public static PointXY point1 = new PointXY(-2, 3);
    public static PointXY point2 = new PointXY(1, 3);
    public static PointXY point3 = new PointXY(2, 1);

    @Test
    public void main() {
        Straight straight = getStraightFromTwoPoints(point1, point2);
        if(straight instanceof StraightPerpendicularToOX) {
            System.out.println("Perpendicular to OX");
        }
        else if (straight instanceof StraightPerpendicularToOY){
            System.out.println("Perpendicular to OY: ");
        } else {
            System.out.println("Straight: ");
        }
        System.out.println(straight);
    }


    public static Straight getStraightFromTwoPoints(PointXY point1, PointXY point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        if (x1-x2 == 0) return new StraightPerpendicularToOX(x1);
        else if (y1-y2 == 0) return new StraightPerpendicularToOY(y1);
        double a = (y1 - y2)/(x1 - x2);
        double b = y1 - ((y1 - y2)/(x1 - x2))*x1;

        return new Straight(a, b);
    }

    public static Straight getStraightPerpendicular(Straight straight, PointXY point) {
        double x = point.getX();
        double y = point.getY();

        double a = -1/straight.getFactorA();
        double b = y - a*x;

        return new Straight(a, b);
    }
}