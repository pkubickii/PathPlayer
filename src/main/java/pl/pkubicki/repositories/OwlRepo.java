package pl.pkubicki.repositories;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;

public class OwlRepo {
    private static final File owlFile = new File("src/main/java/pl/pkubicki/CityOnto.owl");
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private static final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    private static final IRI baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");
    private static OWLOntology ontology;
    static {
        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        } catch (OWLOntologyCreationException e) {
            System.out.println("Error creating ontology: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static OWLOntology getOntology() {
        return ontology;
    }
    public static OWLReasoner getReasoner() {
        return reasonerFactory.createReasoner(ontology);
    }
}
