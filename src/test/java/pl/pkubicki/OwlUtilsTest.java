package pl.pkubicki;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import pl.pkubicki.util.OwlUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class OwlUtilsTest {
    @Test
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

    @Test
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
}