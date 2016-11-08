package uk.ac.ebi.spot;


import com.sun.deploy.xml.BadTokenException;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by olgavrou on 15/06/2016.
 */
public class atlasCrawler {

    public static void main(String[] args){

        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();

        String line = "";
        XMLNode species = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/Users/olgavrou/Downloads/atlas_accession.txt"));
            while ((line = br.readLine()) != null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accession", line);
                String xml =  httpRequestHandler.executeHttpGet("http://www.ebi.ac.uk/arrayexpress/xml/v2/experiments", params);
                XMLParser xmlparser = new XMLParser(xml);
                XMLNode xmlNode = xmlparser.parse();
                if (xmlNode != null){
                    XMLNode xmlNode2 =  xmlNode.getNested();
                    if (xmlNode2 != null){
                        XMLNode xmlNode3 = xmlNode2.getNested();
                        if (xmlNode3 != null){
                            System.out.println(line + "\t" + xmlNode3.getNested());
                        } else {
                            System.out.println(line + "\t" );
                        }
                    } else {
                        System.out.println(line + "\t" );
                    }
                } else {
                    System.out.println(line + "\t" );
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            e.printStackTrace();
        } catch (NullPointerException e){
            System.out.println(line + "\t" + species);
        } catch (BadTokenException e) {
            e.printStackTrace();
        }

    }

}