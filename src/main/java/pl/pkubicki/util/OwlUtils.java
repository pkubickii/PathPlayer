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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public static NodeSet<OWLNamedIndividual> createNodeSetWithGpsClass() throws OWLOntologyCreationException {
        File file= new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        OWLClassExpression owlClassExpression = dataFactory.getOWLClass(owlGpsClassIRI);
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(owlClassExpression);
        return instances;
    }

    public static NodeSet<OWLNamedIndividual> createProximityNodeSet(LatLng poi, NodeSet<OWLNamedIndividual> nodes) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        NodeSet<OWLNamedIndividual> proxSet = null;
        Set<OWLNamedIndividual> set = nodes.getFlattened();
        System.out.println("SetItem: ");
        set.forEach(item -> {
            System.out.println(item);
        });
        System.out.println("NODE: ");
        nodes.forEach(node -> {
            System.out.println(node);
        });

        return proxSet;
    }
}
