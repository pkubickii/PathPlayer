package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import pl.pkubicki.repositories.OwlRepo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OwlUtils {
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private static final IRI baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");

    public static Set<OWLNamedIndividual> getIndividualsInRouteProximity(List<LatLng> route, Double proximity, LengthUnit unit) {
        Set<OWLNamedIndividual> proximityIndividuals = new HashSet<>();
        route.forEach( point -> {
            Set<OWLNamedIndividual> individualsInProximity = getIndividualsInPointProximity(point, proximity, unit);
            Collections.addAll(proximityIndividuals, individualsInProximity.toArray(new OWLNamedIndividual[0]));
        });
        return proximityIndividuals;
    }

    public static Set<OWLNamedIndividual> getIndividualsInPointProximity(LatLng poi, Double proximity, LengthUnit unit) {
        NodeSet<OWLNamedIndividual> nodes = getIndividualsWithGpsClass();
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create(baseIRI + "#location_gps_coordinates"));
        Set<OWLNamedIndividual> proximitySet = new HashSet<>();
        nodes.forEach(node -> (OwlRepo.getReasoner().getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty)).forEach(owlLiteral -> {
            LatLng temp = OwlUtils.stringToLatLng(owlLiteral.getLiteral());
            if (OwlUtils.checkProximity(poi, temp, proximity, unit)) {
                proximitySet.add(node.getRepresentativeElement());
            }
        }));
        return proximitySet;
    }

    // method to gather all individuals from ontology which have GPS coordinates,
    // TODO: change it into one which will narrow this list picking points from some area of vicinity in correlation to starting point
    public static NodeSet<OWLNamedIndividual> getIndividualsWithGpsClass() {
        IRI owlGpsClassIRI = IRI.create(baseIRI + "g#GPSCoordinates");
        return OwlRepo.getReasoner().getInstances(dataFactory.getOWLClass(owlGpsClassIRI));
    }

    public static boolean checkProximity(LatLng point1, LatLng point2, double proximity, LengthUnit unit) {
        return LatLngTool.distance(point1, point2, unit) < proximity;
    }

    public static LatLng stringToLatLng(String stringGps) {
        String[] cords = stringGps.split(", ");
        return new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
    }

    public static Map<OWLNamedIndividual, String> getIndividualsWithLabels(Set<OWLNamedIndividual> set) {
        Map<OWLNamedIndividual, String> individualsLabels = new HashMap<>();
        OWLOntologyWalker walker = new OWLOntologyWalker(OwlRepo.getOntologyCollection());
        OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
            public void visit(OWLAnnotationAssertionAxiom axiom) {
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
        OWLClassExpression classExpression = dataFactory.getOWLClass(IRI.create(baseIRI + "#Voice"));
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create(baseIRI + "#fileName"));
        OWLObjectPropertyExpression propertyExpression = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "#recordedInTheLocation"));
        NodeSet<OWLNamedIndividual> audioTrackIndividuals = OwlRepo.getReasoner().getInstances(classExpression);
        audioTrackIndividuals.forEach( ati -> {
            NodeSet<OWLNamedIndividual> recordedInLocationObject = OwlRepo.getReasoner().getObjectPropertyValues(ati.getRepresentativeElement(), propertyExpression);
            namedIndividuals.forEach( o -> {
                if (recordedInLocationObject.containsEntity(o)) {
                    Set<OWLLiteral> fileNameLiteral = OwlRepo.getReasoner().getDataPropertyValues(ati.getRepresentativeElement(), owlDataProperty);
                    fileNameLiteral.forEach( l -> audioFileNames.put(o, l.getLiteral()));
                }
            });
        });
        return audioFileNames;
    }

    public static NodeSet<OWLClass> getRealEstateSubClasses() {
        IRI iri = IRI.create(baseIRI + "g#RealEstate");
        return OwlRepo.getReasoner().getSubClasses(dataFactory.getOWLClass(iri));
    }

    public static OWLClass getBlockOfFlatsOWLClass() {
        return dataFactory.getOWLClass(IRI.create(baseIRI + "g#BlockOfFlats"));
    }

    public static OWLNamedIndividual getNewTrack(OWLNamedIndividual individual) {
        return dataFactory.getOWLNamedIndividual(baseIRI + "#Track_" + individual.getIRI().getShortForm() + String.format("_%04d", getTrackCount(individual) + 1));
    }
    public static int getTrackCount(OWLNamedIndividual point) {
        OWLClassExpression voiceClassExp = dataFactory.getOWLClass(IRI.create(baseIRI + "#Voice"));
        OWLObjectPropertyExpression recordedInLocationExp = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "#recordedInTheLocation"));
        AtomicInteger counter = new AtomicInteger(0);
        NodeSet<OWLNamedIndividual> voiceInstances = OwlRepo.getReasoner().getInstances(voiceClassExp);
        voiceInstances.forEach( v -> {
            NodeSet<OWLNamedIndividual> objectPropertyValues = OwlRepo.getReasoner().getObjectPropertyValues(v.getRepresentativeElement(), recordedInLocationExp);
            if(objectPropertyValues.containsEntity(point)) counter.getAndIncrement();
        });
        return counter.get();
    }
}
