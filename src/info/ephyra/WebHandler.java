package info.ephyra;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import info.ephyra.querygeneration.Query;
import info.ephyra.querygeneration.QueryGeneration;
import info.ephyra.search.Result;

import javax.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange he) throws IOException {

        // Serve for POST requests only
        if (he.getRequestMethod().equalsIgnoreCase("POST")) {

            try {

                // REQUEST Headers
                Headers requestHeaders = he.getRequestHeaders();
                Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

                int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                // REQUEST Body
                InputStream is = he.getRequestBody();
                JsonReader jsonReader = Json.createReader(is);
                JsonObject input = jsonReader.readObject();
                String question = input.getString("question");
                JsonArray answersArr = input.getJsonArray("answers");

                if (answersArr.size() > 0) {
                    String[] answers = new String[answersArr.size()];
                    for (int a=0; a<answersArr.size(); a++) {
                        answers[a] = answersArr.getString(a);
                    }

                    OpenEphyra instance = OpenEphyra.GetSingleton();
                    Result[] results = instance.askFactoidWithAnswers(question, answers, OpenEphyra.FACTOID_MAX_ANSWERS, OpenEphyra.FACTOID_ABS_THRESH);

                    // RESPONSE Headers
                    //Headers responseHeaders = he.getResponseHeaders();

                    // Send RESPONSE Headers
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);

                    JsonObjectBuilder job = Json.createObjectBuilder();
                    for (int r=0; r<results.length; r++) {
                        String percent = String.format("%2.2f", results[r].getScore());
                        //JsonObject result = Json.createObjectBuilder()
                        //        .add(results[r].getAnswer(), results[r].getScore()).build();
                        //jab.add(result);
                        job.add(results[r].getAnswer(), percent);
                    }

                    JsonObject answerResponse = Json.createObjectBuilder()
                            .add("SortedAnswers", job).build();

                    OutputStream os = new ByteArrayOutputStream();

                    // RESPONSE Body


                    JsonWriter jw = Json.createWriter(os);
                    jw.writeObject(answerResponse);
                    jw.close();

                    System.out.println(os.toString());

                    ((ByteArrayOutputStream) os).writeTo(he.getResponseBody());
                }



                he.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
