package pl.pkubicki.util;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.javadocmd.simplelatlng.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HopperUtils {
    private static final String ghLoc = "osm/mazowieckie.osm.pbf";

    public static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopperOSM().forServer();
        hopper.setDataReaderFile(ghLoc);
        hopper.setGraphHopperLocation("routing-graph-cache");
        hopper.setEncodingManager(EncodingManager.create("foot"));
        hopper.setProfiles(new Profile("foot").setVehicle("foot").setWeighting("fastest").setTurnCosts(false));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));
        hopper.getRouterConfig().setCHDisablingAllowed(true);
        hopper.importOrLoad();
        return hopper;
    }

    public static List<LatLng> getRoute(double fromLat, double fromLon, double toLat, double toLon) {
        GraphHopper hopper = createGraphHopperInstance(ghLoc);
        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon)
                .setProfile("foot")
                .setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors())
            throw new RuntimeException(rsp.getErrors().toString());

        ResponsePath path = rsp.getBest();
        PointList pointList = path.getPoints();
        LatLng from = new LatLng(fromLat, fromLon);
        LatLng to = new LatLng(toLat, toLon);
        return pointListToLatLngList(pointList, from, to);
    }
    private static List<LatLng> pointListToLatLngList(PointList pointList, LatLng startPoint, LatLng endPoint) {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(startPoint);
        pointList.forEach( p -> latLngs.add(new LatLng(p.getLat(), p.getLon())));
        latLngs.add(endPoint);
        return latLngs;
    }

}
