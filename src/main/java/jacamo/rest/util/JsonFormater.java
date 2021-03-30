package jacamo.rest.util;

import java.io.StringWriter;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

public class JsonFormater {

    public static String getAsJsonStr(JsonObject vl) {
        var config = new HashMap<String, Boolean>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);

        var jwf = Json.createWriterFactory(config);
        var sw = new StringWriter();

        try (var jsonWriter = jwf.createWriter(sw)) {

            jsonWriter.writeObject( vl);
            return sw.toString();
        }
    }

}
