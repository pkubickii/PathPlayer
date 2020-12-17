package pl.pkubicki;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.junit.jupiter.api.Test;

public class AddPoiControllerTest {
    double lat1 = 52.162995;
    double lng1 = 22.271528;
    double lat2 = 52.162407;
    double lng2 = 22.271910;
    double lat3 = 52.164171;
    double lng3 = 22.272156;
    LatLng poi0 = new LatLng(lat1, lng1);
    LatLng poi1 = new LatLng(lat2, lng2);
    LatLng poi2 = new LatLng(lat3, lng3);

    @Test
    public void distanceCheck() {
        System.out.println(LatLngTool.distance(poi0, poi1, LengthUnit.METER));
    }
    @Test
    public void stringTest() {
        String poi = "52.164171, 22.272156";
        String[] pois = poi.split(", ");
        LatLng latLng = new LatLng(Double.parseDouble(pois[0]), Double.parseDouble(pois[1]));
        System.out.println("Punkt: " + latLng);
    }

}
