package uk.ac.ebi.spot;

import com.sun.istack.internal.NotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by siiraa on 26/02/15.
 */


public class SubclassFetcher {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException{

        File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/efo_release_candidate.owl");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology localEFO = manager.loadOntologyFromOntologyDocument(inFile);

        //ontology loaded successfully?
        System.out.println(" loading from: " + localEFO);

        OWLDataFactory df = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(localEFO);
        OWLObjectProperty derivesFrom = df.getOWLObjectProperty(IRI.create("http://www.obofoundry.org/ro/ro.owl#derives_from"));
        OWLObjectProperty efoDerivesFrom = df.getOWLObjectProperty(IRI.create("http://www.ebi.ac.uk/efo/derives_from"));
        OWLObjectProperty bearerOf = df.getOWLObjectProperty(IRI.create("http://purl.org/obo/owl/OBO_REL#bearer_of"));
        OWLObjectProperty efoBearerOf = df.getOWLObjectProperty(IRI.create("http://www.ebi.ac.uk/efo/EFO_0001377"));
        OWLObjectProperty partOf = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"));
        OWLObjectProperty efoPartOf = df.getOWLObjectProperty(IRI.create("http://www.ebi.ac.uk/efo/part_of"));
        OWLAnnotationProperty label = df.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2000/01/rdf-schema#label"));
        OWLAnnotationProperty defCite = df.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/definition_citation"));
        OWLAnnotationProperty btoDefCite = df.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/BTO_definition_citation"));
        OWLAnnotationProperty cloDefCite = df.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/CLO_definition_citation"));

        reasoner.precomputeInferences();

        IRI parentIRI = IRI.create("http://www.ebi.ac.uk/efo/EFO_0000322");
        OWLClass parentCls = df.getOWLClass(parentIRI);

        NodeSet<OWLClass> subclasses = reasoner.getSubClasses(parentCls, true);
        Set<OWLClass> flatSubclasses = subclasses.getFlattened();
        FileWriter outFile = new FileWriter("/Users/siiraa/000_EBIwork/ProjWorkSpace/writeFile/clOut_run2.txt");
        outFile.write("EFO cell line\tlabel\tdefinition citation\tBTO reference\tCLO reference\tObjProp1\tObjProp2\tObjProp3\tObjProp4\tObjProp5\tObjProp6\tObjProp7\n");


        for (OWLClass cls : flatSubclasses){
            List<String> outLine = new ArrayList<String>();
            outLine.add(cls.getIRI().toString());

            String labelVal = "";
            String defCiteVal = "";
            String btoVal = "";
            String cloVal = "";

            for(OWLAnnotationAssertionAxiom annoax : localEFO.getAnnotationAssertionAxioms(cls.getIRI())) {

                if(annoax.getProperty().equals(label)){
                     labelVal = ((OWLLiteral) annoax.getValue()).getLiteral();
                }
                if(annoax.getProperty().equals(defCite)){
                     defCiteVal = ((OWLLiteral) annoax.getValue()).getLiteral();
                }
                if(annoax.getProperty().equals(btoDefCite)){
                    btoVal = ((OWLLiteral) annoax.getValue()).getLiteral();
                }
                if(annoax.getProperty().equals(cloDefCite)){
                    cloVal = ((OWLLiteral) annoax.getValue()).getLiteral();
                }

            }

            outLine.add(labelVal);
            outLine.add(defCiteVal);
            outLine.add(btoVal);
            outLine.add(cloVal);

            for(OWLSubClassOfAxiom ax: localEFO.getSubClassAxiomsForSubClass(cls)){
                OWLClassExpression superCls = ax.getSuperClass();

                for(OWLClass c : superCls.getClassesInSignature()){
                    outLine.add(c.toString());
                }

                if(superCls instanceof OWLObjectSomeValuesFrom){
                    OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superCls;
                    OWLClassExpression expression = some.getFiller();
                    if(!expression.isAnonymous()){
                        System.out.println("first nested expression " + expression.asOWLClass().toString());
                    }
                }
            }

            System.out.println(outLine);
            for(String word : outLine){
                outFile.write(word+"\t");
            }
            outFile.write("\n");
        }

        outFile.close();
        System.out.println("finished.");
    }

    private static class RestrictionVisitor extends OWLClassExpressionVisitorAdapter{
        @NotNull
        private final Set<OWLClass> processedClasses;
        private final Set<OWLObjectPropertyExpression> restrictedProperties;
        private final Set<OWLOntology> onts;

        RestrictionVisitor(Set<OWLOntology> onts){
            restrictedProperties = new HashSet<OWLObjectPropertyExpression>();
            processedClasses = new HashSet<OWLClass>();
            this.onts = onts;
        }
        @Override
        public void visit(OWLClass desc){
            processedClasses.add(desc);
            for(OWLOntology ont : onts){
                for(OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(desc)){
                    ax.getSuperClass().accept(this);
                }
            }
        }
        @Override
        public void visit(OWLObjectSomeValuesFrom desc){
            restrictedProperties.add(desc.getProperty());

        }
    }


}
