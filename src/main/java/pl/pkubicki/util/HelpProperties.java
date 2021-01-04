package pl.pkubicki.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class HelpProperties {

    private static final String PATH_NAME = "src/main/resources/pl/pkubicki/properties/helpCues.xml";

    public HelpProperties() {}

    public Properties loadProperties()
    {
        final Properties properties = new Properties();
        try
        {
            final FileInputStream in = new FileInputStream(PATH_NAME);
            properties.loadFromXML(in);
            in.close();
        }
        catch (FileNotFoundException fnfEx)
        {
            System.err.println("Could not read properties from file " + PATH_NAME);
        }
        catch (IOException ioEx)
        {
            System.err.println(
                    "IOException encountered while reading from " + PATH_NAME);
        }
        return properties;
    }

}
