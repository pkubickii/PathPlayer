package pl.pkubicki.util;

import com.google.common.base.CaseFormat;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import pl.pkubicki.models.OWLAudioTrack;
import pl.pkubicki.models.OWLPoint;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static pl.pkubicki.util.OwlUtils.getIndividualsInPointProximity;

public class OwlUtilsTest {
    OWLOntologyManager manager;
    OWLOntology ontology;
    OWLDataFactory dataFactory;
    OWLReasonerFactory reasonerFactory;
    OWLReasoner reasoner;
    IRI baseIRI;
    OWLPoint point;

    @BeforeEach
    void setUp() throws OWLOntologyCreationException {
        File file = new File("D:\\CityOntoTest.owl");
        manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument(file);
        dataFactory = manager.getOWLDataFactory();
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        baseIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi");
        point = new OWLPoint(
                52.16354555,
                22.27205220610704,
                "Testowy %&$ LABel uPH TeStOwY!",
                "UPH - Dom Studenta Nr1 przy ulicy 3 Maja",
                OwlUtils.getBlockOfFlatsOWLClass());
    }

    @Test
    void nameGeneratorTest() {
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(baseIRI + "#" + point.getName());
        OWLAudioTrack audio = new OWLAudioTrack(individual);
        System.out.format("\t point: %s; %n\t audio_name: %s", audio.getPointIndividual().getIRI().getShortForm(), audio.getName());
    }

    void saveToOWL() throws OWLOntologyStorageException {

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(baseIRI + "#" + point.getName());
        OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(point.getRealEstateType(), individual);
        ontology.add(axiom);
        OWLAnnotation labelAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSLabel(), dataFactory.getOWLLiteral(point.getLabel(), "PL" ));

        OWLAnnotation commentAnno = dataFactory.getRDFSComment(point.getComment());
        OWLAxiom labelAxiom = dataFactory.getOWLAnnotationAssertionAxiom(individual.getIRI(), labelAnno);
        OWLAxiom commentAxiom = dataFactory.getOWLAnnotationAssertionAxiom(individual.getIRI(), commentAnno);
        ontology.add(labelAxiom);
        ontology.add(commentAxiom);
  //      manager.applyChange(new AddAxiom(ontology, labelAxiom));
 //       manager.applyChange(new AddAxiom(ontology, commentAxiom));
        OWLClass gpsClass = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates"));
        ontology.add(dataFactory.getOWLClassAssertionAxiom(gpsClass, individual));
        OWLDataProperty gpsCoordsProp = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#location_gps_coordinates"));
        String gps = String.format(Locale.ROOT, "%1$.6f, %2$.6f", point.getLatitude(), point.getLongitude());

        OWLAxiom gpsAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(gpsCoordsProp, individual, dataFactory.getOWLLiteral(gps, new OWLDatatypeImpl(IRI.create("http://www.w3.org/2000/01/rdf-schema#PlainLiteral"))));
        ontology.add(gpsAxiom);
        ontology.saveOntology();
    }

    @Test
    public void testString2() {
        System.out.println(nameCreator(point.getLabel(), point.getRealEstateType().getIRI().getShortForm()));
    }
    private String nameCreator(String string, String classPrefix) {
        classPrefix = classPrefix.substring(0, 1).toLowerCase() + classPrefix.substring(1);
        string = string.replaceAll("[^a-zA-Z0-9]", " ");
        string = StringUtils.replaceChars(string, " ", "_");
        string = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string);
        return classPrefix + string;
    }

    void saveAudio() throws OWLOntologyStorageException {
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(baseIRI + "#" + point.getName());
        OWLClassExpression classExpression = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#Voice"));
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#fileName"));
        OWLObjectPropertyExpression propertyExpression = dataFactory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#recordedInTheLocation"));
        AtomicInteger counter = new AtomicInteger();
        counter.getAndIncrement();
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(classExpression);
        instances.forEach( o -> {
            NodeSet<OWLNamedIndividual> objectPropertyValues = reasoner.getObjectPropertyValues(o.getRepresentativeElement(), propertyExpression);
            if(objectPropertyValues.containsEntity(individual)) counter.getAndIncrement();
        });
        OWLNamedIndividual trackIndividual = dataFactory.getOWLNamedIndividual(baseIRI + "#Track_" + point.getName() + String.format("_%04d", counter.get()));
        OWLClassAssertionAxiom classAxiom = dataFactory.getOWLClassAssertionAxiom(classExpression, trackIndividual);
        ontology.add(classAxiom);
        OWLObjectPropertyAssertionAxiom objectPropertyAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(propertyExpression, trackIndividual, individual);
        ontology.add(objectPropertyAxiom);
        OWLDataPropertyAssertionAxiom fileNameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, trackIndividual, trackIndividual.getIRI().getShortForm() + ".mp3");
        ontology.add(fileNameAxiom);
        ontology.saveOntology();
    }


    void testString() {
        String myString = "UPH - Dom&& Studenta Nr$ 1 % ";
        myString = myString.trim();
        myString = myString.replaceAll("[^a-zA-Z0-9]", " ");
        System.out.println(myString);
        String s = StringUtils.replaceChars(myString, " ", "_");
        String to = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s);
        System.out.println(s);
        System.out.println(to);
    }

    //Learning tests:
    public void owlToNodeSetGpsClass() throws OWLOntologyCreationException {
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


    public void createPlaceFromGeo() throws IOException {
        JsonNominatimClient nominatimClient;
        Properties PROPS = new Properties();
        String PROPS_PATH = "properties/nominatim.properties";
        InputStream in = OwlUtilsTest.class.getResourceAsStream(PROPS_PATH);
        PROPS.load(in);
//        PROPS.setProperty("nominatim.server.url","https://nominatim.openstreetmap.org/");
//        PROPS.setProperty("nominatim.headerEmail","kubicki.przemyslaw@gmail.com");
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager connexionManager = new SingleClientConnManager(null, registry);
        HttpClient httpClient = new DefaultHttpClient(connexionManager, null);
//        HttpClient.newBuilder()
//                .version(Version.HTTP_2)
//                .followRedirects(Redirect.SAME_PROTOCOL)
//                .proxy(ProxySelector.of(new InetSocketAddress("https://nominatim.openstreetmap.org/", 443)))
//                .authenticator(Authenticator.getDefault())
//                .build();
        String baseUrl = PROPS.getProperty("nominatim.server.url");
        String email = PROPS.getProperty("nominatim.headerEmail");
        nominatimClient = new JsonNominatimClient(baseUrl, httpClient, email);

//        Address address = nominatimClient.getAddress(1.64891269513038, 48.1166561643464);
        Address address = nominatimClient.getAddress(22.271528, 52.162995);
        System.out.println(ToStringBuilder.reflectionToString(address, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println("RESZTA: ");
        System.out.println(address.getDisplayName());

        List<Address> addresses = nominatimClient.search("Żytnia, Siedlce");
        for (Address place : addresses) {
            System.out.println(ToStringBuilder.reflectionToString(place, ToStringStyle.MULTI_LINE_STYLE));
        }
        System.out.println("Display name: ");
        List<String> addressList = new ArrayList<>();
        for (Address place : addresses) {
            System.out.println(place.getDisplayName());
            System.out.println(place.getLatitude() + ", " + place.getLongitude());
            addressList.add(place.getDisplayName());
        }
        ObservableList<Address> ov = FXCollections.observableList(addresses);
        System.out.println(ov.get(0).getDisplayName() + " COORDS: " + ov.get(0).getLatitude() + ", " + ov.get(0).getLongitude());
        System.out.println(PROPS_PATH);
    }

    public void testOWLWalker() throws OWLOntologyCreationException {
        File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        OWLClassExpression owlClassExpression = dataFactory.getOWLClass(owlGpsClassIRI);
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(owlClassExpression);
        //System.out.println(instances);

        OWLAnnotationProperty comment = dataFactory.getRDFSComment();
        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
        OWLAnnotationProperty label = dataFactory.getRDFSLabel();

        OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
            @Override
            public void visit(OWLAnnotation node) {
                if (getCurrentAnnotation().getProperty().isLabel() && getCurrentAnnotation().getValue().asLiteral().get().hasLang("pl")) {
                    OWLAxiom currentAxiom = getCurrentAxiom();
                    String string = getCurrentAnnotation().getValue().toString();
                    String[] strings = string.split("\"");
                    System.out.println(strings[1]);
                }
                System.out.println("-");
            }
        };
        walker.walkStructure(visitor);

    }


    public void testOWLAxiomWalker() throws Exception{
        LatLng poi = new LatLng(52.162995, 22.271528);
        File owlFile= new File("src/main/java/pl/pkubicki/CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        IRI owlGpsClassIRI = IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavig#GPSCoordinates");
        OWLClassExpression owlGPSClassExpression = dataFactory.getOWLClass(owlGpsClassIRI);
        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
        Set<OWLNamedIndividual> set = getIndividualsInPointProximity(poi, 200.0, LengthUnit.METER);

        Map<OWLNamedIndividual, String> individualsLabels = new HashMap<OWLNamedIndividual, String>();
        OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
            @Override
            public void visit(OWLAnnotationAssertionAxiom axiom) {
                boolean label = axiom.getProperty().isLabel();
                if(axiom.getProperty().isLabel()){
                    set.forEach(individual -> {
                        if(axiom.getSubject().asIRI().get().getRemainder().hashCode() == individual.getIRI().getRemainder().hashCode()) {
                            individualsLabels.put(individual, axiom.getValue().asLiteral().get().getLiteral());
                        }
                    });
                }
            }
        };
        walker.walkStructure(visitor);
        System.out.println(individualsLabels);
    }


    public void testOWLAudioFilesSearch() throws OWLOntologyCreationException {
        LatLng poi = new LatLng(52.162995, 22.271528);
        File owlFile= new File("src/main/java/pl/pkubicki/CityOnto.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlFile);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        Set<OWLNamedIndividual> set = getIndividualsInPointProximity(poi, 200.0, LengthUnit.METER);
        Map<OWLNamedIndividual, String> individualsLabels = OwlUtils.getIndividualsWithLabels(set);

        OWLClassExpression classExpression = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#Voice"));
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#fileName"));
        OWLObjectPropertyExpression propertyExpression = dataFactory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/lm/ontologies/2019/0/CityOntoNavi#recordedInTheLocation"));

        HashMap<OWLNamedIndividual, String> audioFileNames = new HashMap<>();

        Set<OWLLiteral> audioFileNamesLiterals = new HashSet<>();
        NodeSet<OWLNamedIndividual> audioTrackIndividuals = reasoner.getInstances(classExpression);
        audioTrackIndividuals.forEach( ati -> {
            NodeSet<OWLNamedIndividual> recordedInLocationObject = reasoner.getObjectPropertyValues(ati.getRepresentativeElement(), propertyExpression);
            set.forEach( o -> {
                if (recordedInLocationObject.containsEntity(o)) {
                    audioFileNamesLiterals.addAll(reasoner.getDataPropertyValues(ati.getRepresentativeElement(), owlDataProperty));
                    Set<OWLLiteral> fileNameLiteral = reasoner.getDataPropertyValues(ati.getRepresentativeElement(), owlDataProperty);
                    fileNameLiteral.forEach( l -> {
                        audioFileNames.put(o, l.getLiteral());
                    });

                }
            });
        });
        System.out.println(audioFileNames);
        
        individualsLabels.forEach((individual, label) -> {
            NodeSet<OWLNamedIndividual> objectPropertyValues = reasoner.getObjectPropertyValues(individual, propertyExpression);
        });
    }
}