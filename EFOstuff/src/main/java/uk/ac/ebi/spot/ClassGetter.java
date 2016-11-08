package uk.ac.ebi.spot;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.out;

/**
 * Created by siiraa on 11/03/15.
 */
public class ClassGetter {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/efo_release_candidate.owl");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology localEFO = manager.loadOntologyFromOntologyDocument(inFile);
        System.out.println(" loading from localEFO: " + localEFO);

        OWLDataFactory df = manager.getOWLDataFactory();
        OWLReasonerFactory  owlReasonerFactory = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = owlReasonerFactory.createReasoner(localEFO);
        OWLClass disease = df.getOWLClass(IRI.create("http://www.ebi.ac.uk/efo/EFO_0000408"));
        Set<OWLEntity> seedSig = new HashSet<OWLEntity>();
        seedSig.add(disease);
        seedSig.addAll(reasoner.getSubClasses(disease, false).getFlattened());

        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(manager, localEFO, ModuleType.STAR);


        Set<OWLAxiom> mod = sme.extract(seedSig);
        out.println("seedSig size " + seedSig.size());
        out.println("Module size " + mod.size());

        for(OWLEntity ent : seedSig) {
            System.out.println(ent);
        }
        /*

        //get all classes under phenotypes

        //DIAB_00006 is phenotype in DIAB ontology
        //EFO_0000408 is EFO:disease
        //EFO_0000651 is EFO:phenotype
        IRI parentIRI = IRI.create("http://www.ebi.ac.uk/efo/EFO_0000408");
        OWLClass parentCls = df.getOWLClass(parentIRI);
        System.out.println(parentCls.getSubClasses(localEFO));

        /*for(OWLClass temp: parentCls.getSubClasses(localEFO)){
            System.out.println(temp);
        }

        reasoner.precomputeInferences();

        NodeSet<OWLClass> subclasses = reasoner.getSubClasses(parentCls, true);
        Set<OWLClass> flatSubclasses = subclasses.getFlattened();
        FileWriter outFile = new FileWriter("/Users/siiraa/000_EBIwork/ProjWorkSpace/writeFile/EFO_diseases.txt");
        String clsStr;

        for(OWLClass cls : flatSubclasses){
            clsStr = cls.toString();
            System.out.println(clsStr);
            outFile.write(clsStr);
            outFile.write("\n");
        }
        outFile.close();*/
        System.out.println("finished.");
    }
}
