package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;

public class ProximityUtils {

    public static Straight getStraightFromTwoPoints(LatLng point1, LatLng point2) {
        double x1 = convertToX(point1.getLongitude());
        double y1 = convertToY(point1.getLatitude());
        double x2 = convertToX(point2.getLongitude());
        double y2 = convertToY(point2.getLatitude());

        double a = (y1 - y2)/(x1 - x2);
        double b = y1 - ((y1 - y2)/(x1 - x2))*x1;

        return new Straight(a, b);
    }

    public static Straight getStraightPerpendicular(Straight straight, LatLng point) {
        double x = convertToX(point.getLongitude());
        double y = convertToY(point.getLatitude());

        double a = -1/straight.getFactorA();
        double b = y - a*x;

        return new Straight(a, b);
    }

    public static LatLng getCrossPoint(Straight straight1, Straight straight2) {
        double W;
        double Wx;
        double Wy;

        W = -straight1.getFactorA() - (-(straight2.getFactorA()));
        Wx = straight1.getFactorB() - straight2.getFactorB();
        Wy = -straight1.getFactorA()*straight2.getFactorB() - (-straight2.getFactorA()*straight1.getFactorB());
        double x = Wx/W;
        double y = Wy/W;
        return new LatLng(convertToLatitude(y), convertToLongitude(x));
    }

    public static boolean isOnSegment(LatLng point1, LatLng point2, LatLng point3) {
        PointXY xy1 = new PointXY(convertToX(point1.getLongitude()), convertToY(point1.getLatitude()));
        PointXY xy2 = new PointXY(convertToX(point2.getLongitude()), convertToY(point2.getLatitude()));
        PointXY xy3 = new PointXY(convertToX(point3.getLongitude()), convertToY(point3.getLatitude()));

        return Math.min(xy1.getX(), xy2.getX()) <= xy3.getX() &&
                xy3.getX() <= Math.max(xy1.getX(), xy2.getX()) &&
                Math.min(xy1.getY(), xy2.getY()) <= xy3.getY() &&
                xy3.getY() <= Math.max(xy1.getY(), xy2.getY());
    }

    public static double convertToX(double longitude) {
        double R = 6371;
        double avgLat = (52.16226631921795 + 52.16176184357488)/2;
        return R*longitude*Math.cos(avgLat);
    }

    public static double convertToY(double latitude) {
        double R = 6371;
        return R*latitude;
    }

    public static double convertToLongitude(double x) {
        double R = 6371;
        double avgLat = (52.16226631921795 + 52.16176184357488)/2;
        return x/(R*Math.cos(avgLat));
    }

    public static double convertToLatitude(double y) {
        double R = 6371;
        return y/R;
    }
}
