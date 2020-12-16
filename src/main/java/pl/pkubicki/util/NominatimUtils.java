package pl.pkubicki.util;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class NominatimUtils {
    private static JsonNominatimClient nominatimClient;
    private static Properties PROPS = new Properties();
    private static final String PROPS_PATH = "src/main/resources/pl/pkubicki/properties/nominatim.properties";
    private static HttpClient httpClient;

    private static void initializeNominatimClient() {
        try {
            InputStream in = new FileInputStream(PROPS_PATH);
            PROPS.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager connexionManager = new SingleClientConnManager(null, registry);
        httpClient = new DefaultHttpClient(connexionManager, null);
        String baseUrl = PROPS.getProperty("nominatim.server.url");
        String email = PROPS.getProperty("nominatim.headerEmail");
        nominatimClient = new JsonNominatimClient(baseUrl, httpClient, email);
    }

    public static ObservableList<Address> search(String query) throws IOException {
        initializeNominatimClient();
        List<Address> addresses = nominatimClient.search(query);
        return FXCollections.observableList(addresses);
    }
}
