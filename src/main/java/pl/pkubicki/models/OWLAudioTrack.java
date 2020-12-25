package pl.pkubicki.models;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import static pl.pkubicki.util.OwlUtils.getTrackCount;

public class OWLAudioTrack {
    private OWLNamedIndividual pointIndividual;
    private String name;
    private OWLNamedIndividual trackIndividual;


    private OWLAudioTrack() {

    }

    public OWLAudioTrack(OWLNamedIndividual point) {
        this.name = nameCreator(point);
        this.pointIndividual = point;
        this.trackIndividual = trackIndividualCreator(this.name);
    }

    private OWLNamedIndividual trackIndividualCreator(String name) {
        IRI baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        return dataFactory.getOWLNamedIndividual(baseIRI + "#" + name);
    }

    private String nameCreator(OWLNamedIndividual point) {
    return String.format("Track_%s_%04d", point.getIRI().getShortForm(), getTrackCount(point)+1);
    }

    public OWLNamedIndividual getPointIndividual() {
        return pointIndividual;
    }

    public OWLNamedIndividual getTrackIndividual() {
        return trackIndividual;
    }

    public void setTrackIndividual(OWLNamedIndividual trackIndividual) {
        this.trackIndividual = trackIndividual;
    }

    public void setPointIndividual(OWLNamedIndividual pointIndividual) {
        this.pointIndividual = pointIndividual;
    }

    public String getName() {
        return name;
    }

    public void setName(OWLNamedIndividual point) {
        this.name = nameCreator(point);
    }
}
