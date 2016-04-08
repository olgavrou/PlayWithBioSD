package uk.ac.ebi.spot.biosdfieldfix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import uk.ac.ebi.spot.biosdfieldfix.utils.HttpRequestHandler;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by olgavrou on 07/04/2016.
 */
public class SearchSolr {

    private ArrayList accessions = new ArrayList();
    private long numOfSamplesWithFieldInClade = 0;
    private int holdStart = -1;
    private long numOfAllDocs = 0;
    private int solrSearchSize;
    private String solrSearchServer;

    public SearchSolr(String solrSearchServer, int solrSearchSize) {
        this.solrSearchSize = solrSearchSize;
        this.solrSearchServer = solrSearchServer;
    }

    /*
            Finds the samples in the clade based on a given field and returns the accessions of the samples with the field that needs replacement
            @clade: the clade we will be searching solr for (q=<clade>)
            @field: the field we are searching for in order to replace it with a correct field name
         */
    public ArrayList findSamplesInClade(String clade, String field) throws ParseException, IOException, URISyntaxException {


        JSONObject jsonObject = querySolr("*.*", null);
        Object num = getKeyFromJSON(jsonObject, "numFound");
        if (num != null){
            numOfAllDocs = (Long) num;
        } else {
            numOfAllDocs = 0;
        }


        jsonObject = querySolr(clade, field);
        num = getKeyFromJSON(jsonObject, "numFound");
        if (num != null) {
            numOfSamplesWithFieldInClade = (Long) num;
        } else {
            numOfSamplesWithFieldInClade = 0;
        }
        getAccessions(jsonObject); // the first batch of searches is performed since we already queried to find the numOfSamplesWithFieldInClade

        //we need to loop through solr until we have found all the results
        //or until we have exceeded the number of documents
        while ( (numOfSamplesWithFieldInClade != 0) && (Long.valueOf(accessions.size()) < numOfSamplesWithFieldInClade) && (Long.valueOf(accessions.size()) < numOfAllDocs)){

            jsonObject = querySolr(clade, field);

            getAccessions(jsonObject);
        }

        return accessions;
    }

    /*
        Creates the solr query and returns the json object in response
        @clade: the main filter of the query, i.e. q=<clade>
        @field: the field we are searching for in order to replace it with a correct field name
     */
    private JSONObject querySolr(String clade, String field) throws IOException, URISyntaxException, ParseException {
        SolrQuery query = new SolrQuery();

        query.set("q", clade);
        query.set("wt", "json");

        if (field != null) { //no field to search for
            query.setFields(field + "_crt", "accession");
            query.setFilterQueries(field + "_crt:[* TO *]");
            query.setRows(solrSearchSize);
            query.setStart(holdStart + 1);
            holdStart = holdStart + solrSearchSize; // increase the start int
        }
        String finalQuery = solrSearchServer + query.toString();

        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        String results = httpRequestHandler.executeHttpGet(finalQuery, null);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(results);

        return jsonObject;
    }

    /*
        Gets the json object returned from querying solr and adds the accessions found to the accessions ArrayList
        @jsonObject: the json object to be parsed for the accessions
     */
    private void getAccessions(JSONObject jsonObject) {

        JSONArray docs = (JSONArray) getKeyFromJSON(jsonObject,"docs");
        for (Object obj : docs) {
            JSONObject docObject = (JSONObject) obj;
            accessions.add(getKeyFromJSON(docObject, "accession"));
        }
    }

    /*
        Gets a JsonObject and returns the value of the searchKey
        @jsonObject: the jsonObject to parse
        @searchKey: the key whose value we want
     */
    private Object getKeyFromJSON(JSONObject jsonObject, String searchKey) {
        for (Object key : jsonObject.keySet()) {
            //based on your key types
            String keyStr = (String) key;
            Object keyvalue = jsonObject.get(keyStr);

            //get numOfSamplesWithFieldInClade only if it is not set yet
            if (key.equals(searchKey)){
                return  keyvalue;
            }

            //for nested objects iteration if required
            if (keyvalue instanceof JSONObject)
                return getKeyFromJSON((JSONObject) keyvalue, searchKey);
        }
        return null; // nothing was found
    }
}


