package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitorEx;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OwlUtils {

    public static LatLng stringToLatLng(String stringGps) {
        String[] cords = stringGps.split(", ");
        return new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
    }

    public static boolean checkProximity(LatLng point1, LatLng point2, double proximity, LengthUnit unit) {
        if (LatLngTool.distance(point1, point2, unit) < proximity) return true;
        return false;
    }
    // method to gather all individuals from ontology which have GPS coordinates,
    // TODO: change it into one which will narrow this list picking points from some area of vicinity in correlation to starting point
    public static NodeSet<OWLNamedIndividual> createNodeSetWithGpsProperty(OWLReasoner reasoner, OWLDataFactory dataFactory) {
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        return reasoner.getInstances(dataFactory.getOWLClass(owlGpsClassIRI));
    }

    public static Set<OWLNamedIndividual> createProximityNodeSet(LatLng poi, Double proximity, LengthUnit unit, OWLReasoner reasoner, OWLDataFactory dataFactory) {
        NodeSet<OWLNamedIndividual> nodes = createNodeSetWithGpsProperty(reasoner, dataFactory);
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates"));
        Set<OWLNamedIndividual> proximitySet = new HashSet<>();
        nodes.forEach(node -> {
            System.out.println(node);
            System.out.println(reasoner.getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty));
            (reasoner.getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty)).forEach(owlLiteral -> {
                LatLng temp = OwlUtils.stringToLatLng(owlLiteral.getLiteral());
                if (OwlUtils.checkProximity(poi, temp, proximity, unit)) {
                    proximitySet.add(node.getRepresentativeElement());
                }
            });
        });
        return proximitySet;
    }
    public static String proximitySetToString(Set<OWLNamedIndividual> set) {
        StringBuilder sB = new StringBuilder();
        String result = "";
        set.forEach(item -> {
            sB.append(item.toString());
            sB.append("\n");
        });
        result = sB.toString();
        return result;
    }
}
