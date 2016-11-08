package uk.ac.ebi.spot;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;





import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * Created by siiraa on 04/08/16.
 *  * for each class in EFO, find subClassOf or EquivalentCls annonymous axioms, change the class called into a variable.
 * Count each axiom by frequency of use.
 */

public class patternFetcher {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        File inFile = new File("/Users/siiraa/Desktop/tempWorkSpace/efo.owl");
        FileWriter outFile = new FileWriter("/Users/siiraa/Dropbox/JIRA_tickets/FGPTO-1271/rawOutput_expressions.txt");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology EFO = manager.loadOntologyFromOntologyDocument(inFile);

        System.out.println(" loading from: " + EFO);
        //EFO loaded successfully

        OWLDataFactory df = manager.getOWLDataFactory();
        OWLReasonerFactory owlReasonerFactory = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = owlReasonerFactory.createReasoner(EFO);

        Set<OWLClass> classes;
        classes = EFO.getClassesInSignature();


        int n = 0;
        int m = 0;

        for(OWLClass cls : classes){

            for(OWLClassExpression expression : cls.getSuperClasses(EFO)) {
                if(expression.isAnonymous()) {
                    n++;
                    System.out.println(expression);
                    outFile.write(expression.toString()+"\n");

                }

            }

            for(OWLClassExpression expression : cls.getEquivalentClasses(EFO)){
                if(expression.isAnonymous()) {
                    m++;
                    System.out.println(expression);
                    outFile.write(expression.toString()+"\n");
                }
            }
        }
        outFile.close();
        System.out.println("number of cls with annonymous super classes = " + n);
        System.out.println("number of cls with annonymous equiv classes = " + m);




    }
}
