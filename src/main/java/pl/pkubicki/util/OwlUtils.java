package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OwlUtils {
    public static LatLng stringToLatLng(String stringGps) {
        String[] cords = stringGps.split(", ");
        return new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
    }
    public static void owlToList() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file= new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        String base = "http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates";
        OWLDataProperty owlGpsProperty = dataFactory.getOWLDataProperty("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates");
        IRI owlGpsIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates");




    }
}
