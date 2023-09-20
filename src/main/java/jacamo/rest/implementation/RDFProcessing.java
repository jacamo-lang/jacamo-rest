package jacamo.rest.implementation;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class RDFProcessing {

    public final static String TURTLE = "text/turtle";

    public final static String JSONLD = "application/ld+json";

    public static final ValueFactory rdf = SimpleValueFactory.getInstance();

    public static String baseUrl = "http://172.23.35.17:8080/";

    static String writeToString(RDFFormat format, Model model) {
        OutputStream out = new ByteArrayOutputStream();

        try {
            Rio.write(model, out, format,
                    new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return out.toString();
    }
}
