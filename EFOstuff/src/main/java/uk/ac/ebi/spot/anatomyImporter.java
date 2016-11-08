package uk.ac.ebi.spot;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by siiraa on 03/11/16.
 * In preparation of EFO3, EFO-namespaced anatomy terms that can be replaced by UBERON, FMA, MA, PO, ZFA, FBbt will be
 * deprecated for these reusable imports
 * This script obsoletises out the old term, and rename the entity to the import URIs
 * Input file is read from "/Users/siiraa/000_EBIwork/EFO/EFO3/anatomyimports.txt"
 */


public class anatomyImporter {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        //open EFO
        File inFile = new File("/Users/siiraa/Desktop/tempWorkSpace/efo.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology localEFO = manager.loadOntologyFromOntologyDocument(inFile);
        System.out.println(" loading from localEFO: " + localEFO);

        List<AddAxiom> axiomToAdd = new ArrayList<AddAxiom>();


        //fetch label property IRI
        OWLDataFactory factory = manager.getOWLDataFactory();


        //open input file - readLine "oldURI \t newURI"
        String inputFile = "/Users/siiraa/000_EBIwork/EFO/EFO3/anatomyimports.txt";

        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String currentLine;
        while((currentLine = br.readLine()) != null){
            String[] array = currentLine.split("\t");
            String oldURI = array[0];
            String newURI = array[1];

            //rename EFO_xxxxxxx to new OBO purl
            OWLClass cls = factory.getOWLClass(IRI.create(oldURI));
            OWLClass newCls = factory.getOWLClass(IRI.create(newURI));
            OWLEntityRenamer entityRenamer = new OWLEntityRenamer(localEFO.getOWLOntologyManager(), Collections.singleton(localEFO));
            List<OWLOntologyChange> changes = entityRenamer.changeIRI(cls, newCls.getIRI());

            manager.applyChanges(changes);

            //System.out.println("changes applied - URI changed");

            //create oldURI (EFO_xxxxxxx) as a child of 'obsolete class' http://www.geneontology.org/formats/oboInOwl#ObsoleteClass
            OWLClass parentCls = factory.getOWLClass(IRI.create("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass"));
            OWLClass obsoleteCls = factory.getOWLClass(IRI.create(oldURI));
            OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(obsoleteCls, parentCls);
            axiomToAdd.add(new AddAxiom(localEFO, ax));


            //with label obsolete_ + oldURI.label
            //fetch label, create a clean string of obsolete_clsLabel
            for(OWLAnnotation annotation : newCls.getAnnotations(localEFO, factory.getRDFSLabel())){
                if(annotation.getValue() instanceof OWLLiteral){
                    String labelStr = ("obsolete_" + annotation.getValue().toString());
                    String newLabel = labelStr.replace("\"", "");
                    String cleanLabel = newLabel.replace("^^xsd:string","");

                    //System.out.println(cleanLabel);

                    OWLAnnotation labelAnno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),factory.getOWLLiteral(cleanLabel));
                    OWLAnnotationAssertionAxiom labelAnnoAx = factory.getOWLAnnotationAssertionAxiom(obsoleteCls.getIRI(), labelAnno);
                    //System.out.println("add label axiom is " + labelAnnoAx);

                    manager.addAxiom(localEFO, labelAnnoAx);
                    System.out.println(labelAnnoAx);

                }
            }
            //System.out.println(obsoleteCls);

            //create 'term replaced by' annotation property http://purl.obolibrary.org/obo/IAO_0100001
            OWLAnnotationProperty replacedBy = factory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0100001"));
            OWLLiteral importCls = factory.getOWLLiteral(newURI);
            OWLAnnotationAssertionAxiom replaceAx = factory.getOWLAnnotationAssertionAxiom(replacedBy, obsoleteCls.getIRI(), importCls);
            axiomToAdd.add(new AddAxiom(localEFO, replaceAx));

            //and others...

            //obsolete in version [2.78] Nov.2016
            OWLAnnotationProperty obsoleteInVersion = factory.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/obsoleted_in_version"));
            OWLLiteral inVersion = factory.getOWLLiteral("2.78");
            OWLAnnotationAssertionAxiom obsoleteVersionAx = factory.getOWLAnnotationAssertionAxiom(obsoleteInVersion, obsoleteCls.getIRI(), inVersion);
            axiomToAdd.add(new AddAxiom(localEFO, obsoleteVersionAx));


            //set obsolete flag true
            OWLAnnotationProperty orgClassFlag = factory.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/organizational_class"));
            OWLLiteral obsoleteFlag = factory.getOWLLiteral("true");
            OWLAnnotationAssertionAxiom obsoleteFlagAx = factory.getOWLAnnotationAssertionAxiom(orgClassFlag, obsoleteCls.getIRI(), obsoleteFlag);
            axiomToAdd.add(new AddAxiom(localEFO, obsoleteFlagAx));



        }
        //System.out.println(axiomToAdd);
        manager.applyChanges(axiomToAdd);
        manager.saveOntology(localEFO);
        System.out.println("done");


    }
}
