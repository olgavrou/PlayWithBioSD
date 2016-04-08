package uk.ac.ebi.spot.biosdfieldfix;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.biosdfieldfix.utils.PropertiesManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by olgavrou on 07/04/2016.
 */

public class App  {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String solrSearchServer;
    private int solrQueueSize;

    public String getSolrSearchServer(){
        return  this.solrSearchServer;
    }

    public int getSolrQueueSize() {
        return solrQueueSize;
    }

    public App() {
        PropertiesManager propertiesManager = new PropertiesManager(null);
        Properties properties = propertiesManager.getProperties();
        this.solrSearchServer = properties.getProperty("solr.searchapi.server");
        this.solrQueueSize =  Integer.valueOf(properties.getProperty("solrIndexer.queueSize"));
    }

    public static void main(String[] args){
        App app = new App();

        PropertiesManager propertiesManagerReplacements = new PropertiesManager("replacements.properties");
        Properties props = propertiesManagerReplacements.getProperties();
        String clade = props.getProperty("biosd.group");
        props.remove("biosd.group");

        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String value = (String) e.getValue();
            String[] attributes = value.split(",");
            ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(attributes));
            if (arrayList.size() > 1) {
                String toReplaceWith = arrayList.get(0); // the first field is the one we want to keep and replace the rest of the fields with
                arrayList.remove(0);

                SearchSolr searchSolr = new SearchSolr(app.getSolrSearchServer(), app.getSolrQueueSize());

                for (String field : arrayList) { // for each field
                    try {
                        ArrayList accessions = searchSolr.findSamplesInClade(clade, field); // get the accessions that have the field to be replaced
                        //TODO: get the samples from the BioSD and replace the fields with the toReplaceWith field and push them back to the BioSD
                    /*for (Object s : accessions){
                        System.out.println(s);
                    }*/
                        System.out.println(accessions.size());
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

}


