package uk.ac.ebi.spot.biosdfieldfix.utils;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by olgavrou on 08/04/2016.
 */
public class HttpRequestHandler {
    /*
       Gets the uri path and a set of parameters to define the query, builds uri and executes a GET method
       Returns the content of the GET method
       @path: the main path of the uri
       @params: a set of parameters that will be set in the get method
    */
    public String executeHttpGet(String path, Map<String,String> params) throws IOException, URISyntaxException {
        HttpGet httpGet;
        URI uri = null;
        if(params == null){
            httpGet = new HttpGet(path);
        } else {
            URIBuilder uriBuilder = new URIBuilder()
                    .setPath(path);
            for (String key : params.keySet()) {
                uriBuilder.addParameter(key, params.get(key));
            }

            uri = uriBuilder.build();
            httpGet = new HttpGet(uri);

        }

        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpClient client = HttpClientBuilder.create().build();

        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " on GET: " + uri);
        }

        return getStringFromInputStream(response.getEntity().getContent());

    }

    /*
        Gets an input stream and returns the String from that stream
        @is: the input stream
     */
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        InputStreamReader inputStreamReader = new InputStreamReader(is);
        try {
            br = new BufferedReader(inputStreamReader);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
