package uk.ac.ebi.spot;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.*;

/**
 * Created by siiraa on 07/10/15.
 */
public class xRefAdder {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        //File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/efo_disease_module.owl");
        File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/efo_ordo_module.owl");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        OWLAnnotationProperty omimDefCite = df.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/OMIM_definition_citation"));

        OWLOntology moduleOnt = manager.loadOntologyFromOntologyDocument(inFile);
        System.out.println(".......loading from: " + moduleOnt);

        List<AddAxiom> annotationToAdd = new ArrayList<AddAxiom>();


        //reading input file, each line is URI \t OMIM ID string
        //FileInputStream input = new FileInputStream("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/insert_OMIM_xRef_EFO.txt");
        FileInputStream input = new FileInputStream("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/insert_OMIM_xRef_ORDO.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String strLine;

        Map<String, List<String>> iri2omims = new HashMap<String, List<String>>();
        while((strLine = br.readLine()) != null){
            String[] splitLine = strLine.split("\\t");
            //System.out.println(splitLine[0] + "  ||   " + splitLine[1]);
            String efoIRI =splitLine[0];
            String omim = splitLine[1];
            List<String> omims = iri2omims.get(efoIRI);
            if(omims != null){
                omims.add(omim);
            }else{
                omims = new ArrayList<String>();
                omims.add(omim);
            }
            iri2omims.put(efoIRI, omims);
        }


        Set<OWLClass> owlClasses = moduleOnt.getClassesInSignature();
        System.out.println("owlClasses = " + owlClasses.size());
        System.out.println("#classes to add OMIM to " + iri2omims.size());
        //System.out.println(iri2omims);
        //{http://www.ebi.ac.uk/efo/EFO_0000224=[OMIM:612376], http://www.ebi.ac.uk/efo/EFO_0000588=[OMIM:156240], http://www.ebi.ac.uk/efo/EFO_0000621=[OMIM:256700


        for(OWLClass owlClass : owlClasses){
            List<String> omims = iri2omims.get(owlClass.getIRI().toString());

            //if omims is null, go to the next owlClass
            if(omims == null){
                continue;
            }

            for(String omim : omims){
                //add omim annotation property to owlClass
                OWLLiteral newVal = df.getOWLLiteral(omim);
                OWLAnnotationAssertionAxiom newAx = df.getOWLAnnotationAssertionAxiom(omimDefCite, owlClass.getIRI(), newVal);
                annotationToAdd.add(new AddAxiom(moduleOnt, newAx));

                System.out.println(newAx);
            }
        }

        br.close();
        manager.applyChanges(annotationToAdd);
        manager.saveOntology(moduleOnt);
        System.out.println("ontology is updated...");


    }
}
