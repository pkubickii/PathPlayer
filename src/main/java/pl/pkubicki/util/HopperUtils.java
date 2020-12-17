package pl.pkubicki.util;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.javadocmd.simplelatlng.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HopperUtils {
    private static final String ghLoc = "D:\\mazowieckie.osm.pbf";

    public static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopperOSM().forServer();
        hopper.setDataReaderFile(ghLoc);
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache");
        hopper.setEncodingManager(EncodingManager.create("foot"));

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("foot").setVehicle("foot").setWeighting("fastest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));
        // explicitly allow that the calling code can disable this speed mode
        hopper.getRouterConfig().setCHDisablingAllowed(true);

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
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
        pointList.forEach( p -> {
            latLngs.add(new LatLng(p.getLat(), p.getLon()));
        });
        latLngs.add(endPoint);
        return latLngs;
    }

}
