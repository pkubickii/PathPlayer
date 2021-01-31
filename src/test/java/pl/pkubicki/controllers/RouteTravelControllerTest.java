package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.junit.jupiter.api.Test;
import pl.pkubicki.util.OwlUtils;
import pl.pkubicki.util.geometry.PointXY;
import pl.pkubicki.util.geometry.Straight;

import java.util.*;

class RouteTravelControllerTest {

    private static LatLng ds1 = OwlUtils.stringToLatLng("52.163546, 22.272052");
    private static LatLng pkp = OwlUtils.stringToLatLng("52.162407, 22.271910");
    private static LatLng wns = OwlUtils.stringToLatLng("52.164171, 22.272156");
    private static LatLng bdr = OwlUtils.stringToLatLng("52.163097, 22.272134");
    private static LatLng rss = OwlUtils.stringToLatLng("52.165107, 22.271771");

    private static List<LatLng> owlPoints = new ArrayList<LatLng>() {
        {
            add(ds1);
            add(rss);
            add(pkp);
            add(wns);
            add(bdr);
        }
    };

    private static List<LatLng> route = new ArrayList<LatLng>() {
        {
            add(OwlUtils.stringToLatLng("52.162200, 22.271957"));
            add(OwlUtils.stringToLatLng("52.16212638925049, 22.271858678199912"));
            add(OwlUtils.stringToLatLng("52.16241856282395, 22.271274714274885"));
            add(OwlUtils.stringToLatLng("52.162433836514516, 22.271294458313907"));
            add(OwlUtils.stringToLatLng("52.162507038470515, 22.271148240666424"));
            add(OwlUtils.stringToLatLng("52.162576328871616, 22.271233363551648"));
            add(OwlUtils.stringToLatLng("52.16259923940746, 22.271185493570243"));
            add(OwlUtils.stringToLatLng("52.1626152581561, 22.271206727725417"));
            add(OwlUtils.stringToLatLng("52.16259402400093, 22.271252362532593"));
            add(OwlUtils.stringToLatLng("52.16281567877864, 22.271553924788996"));
            add(OwlUtils.stringToLatLng("52.162922594612596, 22.271667732410158"));
            add(OwlUtils.stringToLatLng("52.163143131803196, 22.27165916424228"));
            add(OwlUtils.stringToLatLng("52.16315039611944, 22.27176254105037"));
            add(OwlUtils.stringToLatLng("52.16388278820849, 22.271739257985487"));
            add(OwlUtils.stringToLatLng("52.16390737512501, 22.2717647762246"));
            add(OwlUtils.stringToLatLng("52.16410649189591, 22.271777814740936"));
            add(OwlUtils.stringToLatLng("52.16405539675183, 22.273153973330295"));
            add(OwlUtils.stringToLatLng("52.164208, 22.273169"));
        }
    };

    @Test
    public void main() {
//        LinkedHashSet<LatLng> linkedList = getPointsOnRoute();
//        System.out.println("Points: " + linkedList);

        Set<String> testSet = new HashSet<String>() {
            {
                add("Trzy");
                add("Dwa");
                add("Jeden");
            }
        };
        System.out.println(testSet);
        Set<String> newSet = linkedToSet(testSet);
        System.out.println("After: ");
        System.out.println(newSet);
    }

    public Set<String> linkedToSet(Set<String> set) {
        return set;
    }



    private LinkedHashSet<LatLng> getPointsOnRoute() {
        LinkedHashSet<LatLng> proximityPoints = new LinkedHashSet<>();
        for (int i = 0; i < route.size()-1; i++) {
            Straight straight1 = getStraightFromTwoPoints(route.get(i), route.get(i + 1));
            for (LatLng point : owlPoints) {
                Straight perpStraight = getStraightPerpendicular(straight1, point);
                LatLng crossPoint = getCrossPoint(straight1, perpStraight);
                double dist = LatLngTool.distance(point, crossPoint, LengthUnit.METER);
                if (dist <= 200.0 && isOnSegment(route.get(i), route.get(i+1), crossPoint))
                    proximityPoints.add(point);
            }
        }
        return proximityPoints;
    }

    private Straight getStraightFromTwoPoints(LatLng point1, LatLng point2) {
        double x1 = convertToX(point1.getLongitude());
        double y1 = convertToY(point1.getLatitude());
        double x2 = convertToX(point2.getLongitude());
        double y2 = convertToY(point2.getLatitude());

        double a = (y1 - y2)/(x1 - x2);
        double b = y1 - ((y1 - y2)/(x1 - x2))*x1;

        return new Straight(a, b);
    }

    private Straight getStraightPerpendicular(Straight straight, LatLng point) {
        double x = convertToX(point.getLongitude());
        double y = convertToY(point.getLatitude());

        double a = -1/straight.getFactorA();
        double b = y - a*x;

        return new Straight(a, b);
    }

    private LatLng getCrossPoint(Straight straight1, Straight straight2) {
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

    private boolean isOnSegment(LatLng point1, LatLng point2, LatLng point3) {
        PointXY xy1 = new PointXY(convertToX(point1.getLongitude()), convertToY(point1.getLatitude()));
        PointXY xy2 = new PointXY(convertToX(point2.getLongitude()), convertToY(point2.getLatitude()));
        PointXY xy3 = new PointXY(convertToX(point3.getLongitude()), convertToY(point3.getLatitude()));

        return Math.min(xy1.getX(), xy2.getX()) <= xy3.getX() &&
                xy3.getX() <= Math.max(xy1.getX(), xy2.getX()) &&
                Math.min(xy1.getY(), xy2.getY()) <= xy3.getY() &&
                xy3.getY() <= Math.max(xy1.getY(), xy2.getY());
    }

    private double getDistance(PointXY point1, PointXY point2) {
        return Math.abs(Math.pow(point2.getX()-point1.getX(), 2) + Math.pow(point2.getY() - point1.getY(), 2));
    }

    private double convertToX(double longitude) {
        double R = 6371;
        double avgLat = (52.16226631921795 + 52.16176184357488)/2;
        return R*longitude*Math.cos(avgLat);
    }

    private double convertToY(double latitude) {
        double R = 6371;
        return R*latitude;
    }

    private double convertToLongitude(double x) {
        double R = 6371;
        double avgLat = (52.16226631921795 + 52.16176184357488)/2;
        return x/(R*Math.cos(avgLat));
    }

    private double convertToLatitude(double y) {
        double R = 6371;
        return y/R;
    }

    private Straight straightPerpendicularTest(Straight straight, double x, double y) {
        double a;
        double b;

        a = -1/straight.getFactorA();
        b = y - a*x;

        return new Straight(a, b);
    }

    private void crossPointTest(Straight straight1, Straight straight2) {
        double W;
        double Wx;
        double Wy;

        W = -straight1.getFactorA() - (-(straight2.getFactorA()));
        Wx = straight1.getFactorB() - straight2.getFactorB();
        Wy = -straight1.getFactorA()*straight2.getFactorB() - (-straight2.getFactorA()*straight1.getFactorB());
        double x = Wx/W;
        double y = Wy/W;
        System.out.println("x: " + x + " y: " + y);
    }

    private boolean isOnSegmentTest(double x1, double y1, double x2, double y2, double x3, double y3) {
        return Math.min(x1, x2) <= x3 &&
                x3 <= Math.max(x1, x2) &&
                Math.min(y1, y2) <= y3 &&
                y3 <= Math.max(y1, y2);
    }

    private Straight testFactors(double x1, double y1, double x2, double y2) {
        double a;
        double b;

        a = (y1 - y2)/(x1 - x2);
        b = y1 - ((y1 - y2)/(x1 - x2))*x1;

        return new Straight(a, b);
    }
}