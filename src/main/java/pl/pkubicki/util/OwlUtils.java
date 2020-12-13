package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.*;

import java.io.File;
import java.util.*;

public class OwlUtils {
    private static final File owlFile = new File("src/main/java/pl/pkubicki/CityOnto.owl");
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private static final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    private static OWLOntology ontology;
    static {
        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
    private static final OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);;


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
    public static NodeSet<OWLNamedIndividual> getIndividualsWithGpsClass() {
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        return reasoner.getInstances(dataFactory.getOWLClass(owlGpsClassIRI));
    }

    public static Set<OWLNamedIndividual> getIndividualsInProximity(LatLng poi, Double proximity, LengthUnit unit) {
        NodeSet<OWLNamedIndividual> nodes = getIndividualsWithGpsClass();
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates"));
        Set<OWLNamedIndividual> proximitySet = new HashSet<>();
        nodes.forEach(node -> {
            (reasoner.getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty)).forEach(owlLiteral -> {
                LatLng temp = OwlUtils.stringToLatLng(owlLiteral.getLiteral());
                if (OwlUtils.checkProximity(poi, temp, proximity, unit)) {
                    proximitySet.add(node.getRepresentativeElement());
                }
            });
        });
        return proximitySet;
    }

    public static Map<OWLNamedIndividual, String> getIndividualsWithLabels(Set<OWLNamedIndividual> set) {
        Map<OWLNamedIndividual, String> individualsLabels = new HashMap<OWLNamedIndividual, String>();
        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
        OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
            public void visit(OWLAnnotationAssertionAxiom axiom) {
                boolean label = axiom.getProperty().isLabel();
                if (axiom.getProperty().isLabel()) {
                    set.forEach(individual -> {
                        if (axiom.getSubject().asIRI().get().getRemainder().hashCode() == individual.getIRI().getRemainder().hashCode()) {
                            individualsLabels.put(individual, axiom.getValue().asLiteral().get().getLiteral());
                        }
                    });
                }
            }
        };
        walker.walkStructure(visitor);
        return individualsLabels;
    }

    public static Map<OWLNamedIndividual, String> getAudioFileNames(Set<OWLNamedIndividual> namedIndividuals) {
        HashMap<OWLNamedIndividual, String> audioFileNames = new HashMap<>();
        OWLClassExpression classExpression = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#Voice"));
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#fileName"));
        OWLObjectPropertyExpression propertyExpression = dataFactory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#recordedInTheLocation"));
        NodeSet<OWLNamedIndividual> audioTrackIndividuals = reasoner.getInstances(classExpression);
        audioTrackIndividuals.forEach( ati -> {
            NodeSet<OWLNamedIndividual> recordedInLocationObject = reasoner.getObjectPropertyValues(ati.getRepresentativeElement(), propertyExpression);
            namedIndividuals.forEach( o -> {
                if (recordedInLocationObject.containsEntity(o)) {
                    Set<OWLLiteral> fileNameLiteral = reasoner.getDataPropertyValues(ati.getRepresentativeElement(), owlDataProperty);
                    fileNameLiteral.forEach( l -> {
                        audioFileNames.put(o, l.getLiteral());
                    });
                }
            });
        });
        return audioFileNames;
    }
}
