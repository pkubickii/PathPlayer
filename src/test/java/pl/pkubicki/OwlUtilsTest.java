package pl.pkubicki;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import pl.pkubicki.util.OwlUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OwlUtilsTest {
    @Test
    public void owlToNodeSetbyGpsClass() throws OWLOntologyCreationException {
        File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        OWLClassExpression owlClassExpression = dataFactory.getOWLClass(owlGpsClassIRI);
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(owlClassExpression);

        System.out.println(instances);

        System.out.println("Reasoner: " + reasoner.getReasonerName() + " version: " + reasoner.getReasonerVersion());

    }

    @Test
    public void createProximitySet() throws OWLOntologyCreationException {
        File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        LatLng poi0 = new LatLng(52.162995, 22.271528);
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        OWLClassExpression owlClassExpression = dataFactory.getOWLClass(owlGpsClassIRI);
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates"));
        NodeSet<OWLNamedIndividual> nodes = reasoner.getInstances(owlClassExpression);

//        Set<OWLNamedIndividual> set = nodes.getFlattened();
//        System.out.println("SetItem: ");
//        set.forEach(item -> {
//            System.out.println(item);
//        });
        System.out.println("NODES: ");
        Set<OWLNamedIndividual> proxList = new HashSet<>();
        nodes.forEach(node -> {
            System.out.println(node);
            System.out.println(reasoner.getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty));
            (reasoner.getDataPropertyValues(node.getRepresentativeElement(), owlDataProperty)).forEach(owlLiteral -> {
                LatLng temp = OwlUtils.stringToLatLng(owlLiteral.getLiteral());
                if (OwlUtils.checkProximity(poi0, temp, 100.0, LengthUnit.METER)) {
                    proxList.add(node.getRepresentativeElement());
                }
            });
        });
        System.out.println("Prox List: ");
        System.out.println(proxList);
    }
}