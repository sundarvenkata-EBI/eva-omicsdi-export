package uk.ac.ebi.eva.bd2k;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.xml.validator.parser.marshaller.OmicsDataMarshaller;

import uk.ac.ebi.eva.bd2k.client.StudyEvaWSClient;
import uk.ac.ebi.eva.bd2k.conf.ExporterConfiguration;
import uk.ac.ebi.eva.bd2k.export.StudyExporter;
import uk.ac.ebi.eva.bd2k.transform.StudyTransformerImpl;

import java.io.IOException;
import java.util.Properties;

public class StudyExporterAppl {

    private static final Logger logger = LoggerFactory.getLogger(StudyExporterAppl.class);

    public static void main(String[] args) {
        try {
            StudyExporterCommandLine commandLine = parseCommandLine(args);
            Properties properties = loadProperties();
            ExporterConfiguration configuration = new ExporterConfiguration(properties);
            StudyExporter exporter = new StudyExporter(
                    new StudyEvaWSClient(configuration.getEvaStudiesWSURL(), new RestTemplate()),
                    new StudyTransformerImpl(), new OmicsDataMarshaller(), commandLine.getOutputDirectory());
            exporter.export();
        } catch (IOException e) {
            logger.error("Cannot load properties: " + e.getMessage());
        } catch (ParameterException e) {
            logger.error("Command line error: " + e.getMessage());
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(StudyExporterAppl.class.getResourceAsStream("/configuration.properties"));
        return properties;
    }

    private static StudyExporterCommandLine parseCommandLine(String[] args) {
        StudyExporterCommandLine commandLine = new StudyExporterCommandLine();
        JCommander jc = new JCommander(commandLine);
        jc.parse(args);

        if (commandLine.printHelp()) {
            jc.usage();
            System.exit(-1);
        }

        return commandLine;
    }
}
