package pl.pkubicki.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pl.pkubicki.models.OWLAudioTrack;
import pl.pkubicki.models.OWLPoint;

import java.io.File;
import java.util.Locale;

public class OwlManagement {
    private static final File owlFile = new File("src/main/java/pl/pkubicki/CityOnto.owl");
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private static OWLOntology ontology;
    static {
        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    private static final IRI baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");

    public static void savePoint(OWLPoint point) {
        if (point == null) return;
        addPointAxiom(point);
        addPointLabelAnno(point);
        addPointCommentAnno(point);
        addGpsClass(point);
        addGpsLocationData(point);

        try {
            ontology.saveOntology();
        } catch(OWLOntologyStorageException e) {
            System.out.println("Error saving to ontology: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addPointCommentAnno(OWLPoint point) {
        OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(point.getComment(), "PL"));
        OWLAxiom commentAxiom = dataFactory.getOWLAnnotationAssertionAxiom(point.getPointIndividual().getIRI(), commentAnno);
        ontology.add(commentAxiom);
    }

    private static void addPointLabelAnno(OWLPoint point) {
        OWLAnnotation labelAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSLabel(), dataFactory.getOWLLiteral(point.getLabel(), "PL" ));
        OWLAxiom labelAxiom = dataFactory.getOWLAnnotationAssertionAxiom(point.getPointIndividual().getIRI(), labelAnno);
        ontology.add(labelAxiom);
    }

    private static void addPointAxiom(OWLPoint point) {
        OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(point.getRealEstateType(), point.getPointIndividual());
        ontology.add(axiom);
    }

    private static void addGpsClass(OWLPoint point) {
        OWLClass gpsClass = dataFactory.getOWLClass(IRI.create(baseIRI + "#GPSCoordinates"));
        ontology.add(dataFactory.getOWLClassAssertionAxiom(gpsClass, point.getPointIndividual()));
    }

    private static void addGpsLocationData(OWLPoint point) {
        OWLDataProperty gpsCoordsProp = dataFactory.getOWLDataProperty(IRI.create(baseIRI + "#location_gps_coordinates"));
        String gps = String.format(Locale.ROOT, "%1$.6f, %2$.6f", point.getLatitude(), point.getLongitude());
        OWLAxiom gpsAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(gpsCoordsProp, point.getPointIndividual(), dataFactory.getOWLLiteral(gps));
        ontology.add(gpsAxiom);
    }

    public static void saveAudioTrack(OWLAudioTrack track) {
        if (track == null) return;
        addVoiceClass(track.getTrackIndividual());
        addRecordedInLocationObject(track.getTrackIndividual(), track.getPointIndividual());
        addFileNameData(track.getTrackIndividual());
        try {
            ontology.saveOntology();
        } catch(OWLOntologyStorageException e) {
            System.out.println("Error saving to ontology: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addVoiceClass(OWLNamedIndividual trackIndividual) {
        OWLClassExpression voiceClassExp = dataFactory.getOWLClass(IRI.create(baseIRI + "#Voice"));
        OWLClassAssertionAxiom classAxiom = dataFactory.getOWLClassAssertionAxiom(voiceClassExp, trackIndividual);
        ontology.add(classAxiom);
    }

    private static void addRecordedInLocationObject(OWLNamedIndividual trackIndividual, OWLNamedIndividual pointIndividual) {
        OWLObjectPropertyExpression recInLocationExp = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "#recordedInTheLocation"));
        OWLObjectPropertyAssertionAxiom objectPropertyAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(recInLocationExp, trackIndividual, pointIndividual);
        ontology.add(objectPropertyAxiom);
    }

    private static void addFileNameData(OWLNamedIndividual trackIndividual) {
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create(baseIRI + "#fileName"));
        OWLDataPropertyAssertionAxiom fileNameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, trackIndividual, trackIndividual.getIRI().getShortForm() + ".mp3");
        ontology.add(fileNameAxiom);
    }
}
