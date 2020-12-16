package pl.pkubicki;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.*;
import org.junit.Test;
import pl.pkubicki.util.HopperUtils;

import java.util.Locale;

public class HopperUtilsTest {
private static final String ghLoc = "D:\\mazowieckie.osm.pbf";
    @Test
    public void testGraphHopper() {
        GraphHopper hopper = HopperUtils.createGraphHopperInstance(ghLoc);
        GHRequest req = new GHRequest(52.162995, 22.271528, 52.16553841009015, 22.27621565578134)
                .setProfile("foot")
                .setLocale(Locale.JAPANESE);
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

    }
}
