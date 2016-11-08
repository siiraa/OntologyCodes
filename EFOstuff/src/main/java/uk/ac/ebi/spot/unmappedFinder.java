package uk.ac.ebi.spot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by siiraa on 28/10/15.
 */
public class unmappedFinder {
    public static void main(String[] arg) throws IOException {
        File unmapped = new File("/Users/siiraa/000_EBIwork/CTTV/HP_MeSH/efomapped.txt");
        File refFile = new File("/Users/siiraa/000_EBIwork/CTTV/HP_MeSH/3ontmapped.txt");

        //Read unmapped value into a list
        //Scanner scanner1 = new Scanner(unmapped);
        //List<String> listUnmapped = new ArrayList<String>();


        /*
        while(scanner1.hasNext()){
            listUnmapped.add(scanner1.next());
        }
        scanner1.close();

        //Read the comparer - see if unmapped belongs to this list
        Scanner scanner2 = new Scanner(refFile);
        List<String> listRef = new ArrayList<String>();
        while(scanner2.hasNext()){
            listRef.add(scanner2.next());
        }
        scanner2.close();
        */


        BufferedReader br1 = new BufferedReader(new FileReader(unmapped));
        String line;

        List<String> listUnmapped = new ArrayList<String>();
        List<String> listRef = new ArrayList<String>();

        while ((line = br1.readLine()) != null){
            listUnmapped.add(line);

        }

        BufferedReader br2 = new BufferedReader(new FileReader(refFile));
        String line2;

        while ((line2 = br2.readLine()) != null){
            listRef.add(line2);

        }


        System.out.println(listUnmapped);
        System.out.println(listRef);

        //if unmapped in ref --- print to file
        for(String item : listUnmapped){
            if(!(listRef.contains(item))){
                System.out.println(item);
            }
        }


    }
}
