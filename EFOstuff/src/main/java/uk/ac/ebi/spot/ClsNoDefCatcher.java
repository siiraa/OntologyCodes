package uk.ac.ebi.spot;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Finding classes with no annotation property - definition defined by
 * http://www.ebi.ac.uk/efo/definition which has subproperties
 * http://purl.obolibrary.org/obo/IAO_0000115 and
 * http://purl.obolibrary.org/obo/UBPROP_0000001
 * Created by siiraa on 22/05/15.
 */
public class ClsNoDefCatcher {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException{

        File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/localEFO.owl");
        FileWriter outFile = new FileWriter("/Users/siiraa/000_EBIwork/ProjWorkSpace/writeFile/classesNoDef.txt");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology localEFO = manager.loadOntologyFromOntologyDocument(inFile);
        System.out.println("loading " + localEFO + "...");

        OWLDataFactory df = manager.getOWLDataFactory();

        List<String> outputList = new ArrayList<String>();

        IRI efoDef = IRI.create("http://www.ebi.ac.uk/efo/definition");
        IRI oboDef = IRI.create("http://purl.obolibrary.org/obo/IAO_0000115");
        IRI extDef = IRI.create("http://purl.obolibrary.org/obo/UBPROP_0000001");
        String rdfsLabel = "http://www.w3.org/2000/01/rdf-schema#label";



        for(OWLClass cls : localEFO.getClassesInSignature()){
            /*iterate through annotation properties of each cls. If a definition is found, skip to the next class.
            If there is not a definition for that class, put class URI and label in tab-delimited outFile text file.
             */
            List<IRI> iriList = new ArrayList<IRI>();
            IRI tempIRI;
            for(OWLAnnotationAssertionAxiom annoax : localEFO.getAnnotationAssertionAxioms(cls.getIRI())){
                tempIRI = annoax.getProperty().getIRI();
                iriList.add(tempIRI);
            }

            //System.out.println(cls + "has annotation properties " + iriList);
            if(iriList.contains(efoDef) || iriList.contains(oboDef) || iriList.contains(extDef)){
                //System.out.println("skipping class containing definition...");
            }
            else{
                //System.out.println(cls);
                for(IRI findLabel : iriList){
                    //System.out.println(findLabel.toString());
                    if(findLabel.toString().contains(rdfsLabel)){

                        for(OWLAnnotationAssertionAxiom aAxiom : localEFO.getAnnotationAssertionAxioms(cls.getIRI())){
                            if(aAxiom.getProperty().equals(df.getRDFSLabel())){
                                outFile.write(cls + "\t" + aAxiom.getValue() + "\n");
                                System.out.println("writing file...");
                            }

                        }

                    }
                }

            }

        }
        outFile.close();
        System.out.println("finished.");
    }
}
