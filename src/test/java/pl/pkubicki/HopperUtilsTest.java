package pl.pkubicki;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.*;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.util.HopperUtils;
import pl.pkubicki.util.OwlUtils;

import java.util.*;

public class HopperUtilsTest {
    private static final String ghLoc = "D:\\mazowieckie.osm.pbf";
    private GraphHopper hopper;

    @BeforeEach
    void setUp() {
        hopper = HopperUtils.createGraphHopperInstance(ghLoc);
    }

    @Test
    public void testGraphHopper() {
        GHRequest req = new GHRequest(52.162995, 22.271528, 52.16553841009015, 22.27621565578134)
                .setProfile("foot")
                .setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors())
            throw new RuntimeException(rsp.getErrors().toString());

        // use the best path, see the GHResponse class for more possibilities.
        ResponsePath path = rsp.getBest();

        PointList pointList = path.getPoints();
        double distance = path.getDistance();
        long timeInMs = path.getTime();
        Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
        InstructionList il = path.getInstructions();
        // iterate over all turn instructions
        for (Instruction instruction : il) {
             System.out.println("distance " + instruction.getDistance() + " for instruction: " + instruction.getTurnDescription(tr));
        }
        System.out.println(pointList);
    }
    @Test
    void testGetProximityListFromPointList () {
        GHRequest req = new GHRequest(52.162995, 22.271528, 52.16855105916234, 22.271195745978176)
                .setProfile("foot")
                .setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors())
            throw new RuntimeException(rsp.getErrors().toString());

        ResponsePath path = rsp.getBest();
        List<LatLng> latLngs = new ArrayList<>();
        PointList pointList = path.getPoints();
        pointList.forEach(p -> {
            latLngs.add(new LatLng(p.getLat(), p.getLon()));
        });
        System.out.println(latLngs);
        Set<OWLNamedIndividual> proximityPois = new HashSet<>();
        latLngs.forEach(latLng -> {
            Set<OWLNamedIndividual> individualsInProximity = OwlUtils.getIndividualsInPointProximity(latLng, 75.0, LengthUnit.METER);
            Collections.addAll(proximityPois, individualsInProximity.toArray(new OWLNamedIndividual[0]));
        });

        System.out.println(proximityPois);
    }
}
