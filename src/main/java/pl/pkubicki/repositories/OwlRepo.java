package pl.pkubicki.repositories;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class OwlRepo {
    private static final File owlFile = new File("ontology/CityOnto.owl");
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    private static OWLOntology ontology;
    static {
        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        } catch (OWLOntologyCreationException e) {
            System.out.println("Error creating ontology: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static OWLReasoner getReasoner() {
        return reasonerFactory.createReasoner(ontology);
    }
    public static Collection<OWLOntology> getOntologyCollection() {
        return Collections.singleton(ontology);
    }
    public static void add(OWLAxiom axiom) {
        ontology.add(axiom);
        try {
            ontology.saveOntology();
        } catch(OWLOntologyStorageException e) {
            System.out.println("Error saving to ontology: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
