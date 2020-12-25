package pl.pkubicki.models;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class OWLPoint {
    private String name;
    private String label;
    private String comment;
    private double latitude;
    private double longitude;
    private OWLClass realEstateType;
    private OWLNamedIndividual pointIndividual;

    private OWLPoint() {

    }

    public OWLPoint(double latitude, double longitude, String label, String comment, OWLClass realEstateType) {
        this.name = nameCreator(label, realEstateType.getIRI().getShortForm());
        this.label = label;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.realEstateType = realEstateType;
        this.pointIndividual =  pointIndividualCreator(this.name);
    }

    private OWLNamedIndividual pointIndividualCreator(String name) {
        IRI baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        return dataFactory.getOWLNamedIndividual(baseIRI + "#" + name);
    }

    private String nameCreator(String string, String classPrefix) {
        classPrefix = classPrefix.substring(0, 1).toLowerCase() + classPrefix.substring(1);
        string = string.replaceAll("[^a-zA-Z0-9]", " ");
        string = StringUtils.replaceChars(string, " ", "_");
        string = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string);
        return classPrefix + string;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getComment() {
        return comment;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public OWLClass getRealEstateType() {
        return realEstateType;
    }

    public OWLNamedIndividual getPointIndividual() {
        return pointIndividual;
    }

    public void setName(String name, OWLClass realEstateType) {
        this.name = nameCreator(name, realEstateType.getIRI().getShortForm());
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRealEstateType(OWLClass realEstateType) {
        this.realEstateType = realEstateType;
    }

    public void setPointIndividual(OWLNamedIndividual pointIndividual) {
        this.pointIndividual = pointIndividual;
    }
}
