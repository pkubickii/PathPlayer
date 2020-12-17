package pl.pkubicki;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class PrimaryControllerTest {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private File file= new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\TestOnto.owl");
//    private File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");

    private OWLOntology ontology;
    {
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
    private IRI IOR = IRI.create("http://www.co-ode.org/roberts/family-tree.owl");
    OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

    @Test
    public void showOwlNow() {
        System.out.println("Test buttona." + manager.getOntologies().size());
        System.out.println(ontology);
        System.out.println("Jedziemy:");
        System.out.println(IOR);
        OWLClass man = dataFactory.getOWLClass(IOR+"#Man");
        OWLClass woman = dataFactory.getOWLClass(IOR+"#Woman");
        OWLSubClassOfAxiom m_sub_w = dataFactory.getOWLSubClassOfAxiom(man, woman);
        ontology.add(m_sub_w);
        System.out.println(ontology);
        System.out.println(m_sub_w);
        ontology.remove(m_sub_w);
        System.out.println(ontology);
//        ontology.logicalAxioms().forEach(System.out::println);

        // new individual
        OWLIndividual papiez = dataFactory.getOWLNamedIndividual(IOR+"#Papiez");
        OWLClass personClass = dataFactory.getOWLClass(IOR+"#Person");
        OWLClassAssertionAxiom ax = dataFactory.getOWLClassAssertionAxiom(personClass, papiez);
        manager.addAxiom(ontology, ax);

        OWLClass duchowny = dataFactory.getOWLClass(IOR+"#Duchowny");
        OWLSubClassOfAxiom d_sub_p = dataFactory.getOWLSubClassOfAxiom(duchowny, personClass);
        ontology.add(d_sub_p);


        System.out.println("DODAWANIE: \n");
        System.out.println(ontology);
        ontology.signature().filter(e->!e.isBuiltIn()&&e.getIRI().getRemainder().orElse("").startsWith("D")).forEach(System.out::println);
//        ontology.signature().filter(e->!e.isBuiltIn()&&e.getIRI().getFragment().startsWith("P")).forEach(System.out::println);
        ontology.individualsInSignature().filter(e->e.getIRI().getFragment().contains("Papiez")).forEach(System.out::println);
        File fileOut = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\family_new.owl");
        try {
            manager.saveOntology(ontology, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileOut));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}