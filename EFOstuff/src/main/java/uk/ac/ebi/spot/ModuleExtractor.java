package uk.ac.ebi.spot;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.out;

/**
 * Created by siiraa on 01/04/15.
 */
public class ModuleExtractor {
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        File inFile = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/localEFO/efo_release_candidate.owl");
        File outAx = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/writeFile/efoDiseaseAxioms.owl");
        File outMod = new File("/Users/siiraa/000_EBIwork/ProjWorkSpace/writeFile/efo_disease_module.owl");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology localEFO = manager.loadOntologyFromOntologyDocument(inFile);
        OWLOntology moduleOnt = manager.createOntology(IRI.create("http://www.ebi.ac.uk/efo/efoDiseaseModule"));
        OWLOntology disAxOnt = manager.createOntology(IRI.create("http://www.ebi.ac.uk/efo/efoDiseaseAxiom"));

        //ontology loaded successfully?
        out.println(" loading from: " + localEFO);

        OWLDataFactory df = manager.getOWLDataFactory();

        OWLClass disease = df.getOWLClass(IRI.create("http://www.ebi.ac.uk/efo/EFO_0000408"));

        Set<OWLEntity> seedSig = new HashSet<OWLEntity>();
        OWLReasonerFactory  owlReasonerFactory = new Reasoner.ReasonerFactory();

        OWLReasoner reasoner = owlReasonerFactory.createReasoner(localEFO);

        seedSig.add(disease);
        seedSig.addAll(reasoner.getSubClasses(disease, false).getFlattened());

        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(manager, localEFO, ModuleType.STAR);


        Set<OWLAxiom> mod = sme.extract(seedSig);
        out.println("seedSig size " + seedSig.size());
        out.println("Module size " + mod.size());

        OWLClass cls = null;

        List<AddAxiom> moduleToAdd = new ArrayList<AddAxiom>();
        List<AddAxiom> axiomToAdd = new ArrayList<AddAxiom>();

        Set<OWLClass> diseaseClsSet = new HashSet<OWLClass>();

        for(OWLEntity ent : seedSig){
            cls = ent.asOWLClass();
            diseaseClsSet.add(cls);

            //System.out.println("class: " + cls);

            //if cls contains equivalent classes --- record axioms
            Set<OWLClassExpression> equivCls = cls.getEquivalentClasses(localEFO);

            if(equivCls.isEmpty()) {
                //System.out.println("equiv cls is nothing");
            }
            else{
                OWLAxiom addClsAx = df.getOWLDeclarationAxiom(cls);
                axiomToAdd.add(new AddAxiom(disAxOnt, addClsAx));
                axiomToAdd.add(new AddAxiom(moduleOnt, addClsAx));

                for (OWLClassExpression owlClassExpression : equivCls) {
                    OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(cls, owlClassExpression);
                    //System.out.println("equiv cls axioms = " + equivAx);
                    axiomToAdd.add(new AddAxiom(disAxOnt, equivAx));
                }
            }


            Set<OWLClassExpression> parentCls = cls.getSuperClasses(localEFO);
            for(OWLClassExpression parent : parentCls){
                if(parent.getClass().equals(OWLClassImpl.class)){
                    OWLClass owlParent = (OWLClass) parent;
                    //System.out.println("has direct parent: " + owlParent);
                    OWLAxiom subAx = df.getOWLSubClassOfAxiom(cls, owlParent);
                    //System.out.println("with subclass ax: " + subAx);
                    moduleToAdd.add(new AddAxiom(moduleOnt, subAx));
                }
            }


            for(OWLSubClassOfAxiom ax: localEFO.getSubClassAxiomsForSubClass(cls)){
                axiomToAdd.add(new AddAxiom(disAxOnt, ax));
            }

            for (OWLAnnotationAssertionAxiom annoax : localEFO.getAnnotationAssertionAxioms(cls.getIRI()) ){
                //store [cls, annoax] somewhere, add axiom to axiomToAdd to create a new module ontology
                moduleToAdd.add(new AddAxiom(moduleOnt, annoax));
            }
        }

        //Checking if an object property references a disease class
        Set<OWLObjectProperty> objPropSet = localEFO.getObjectPropertiesInSignature();
        for(OWLObjectProperty tempObj : objPropSet){

            Set<OWLObjectPropertyDomainAxiom> domainList = localEFO.getObjectPropertyDomainAxioms(tempObj);
            Set<OWLObjectPropertyRangeAxiom> rangeList = localEFO.getObjectPropertyRangeAxioms(tempObj);

            for(OWLObjectPropertyDomainAxiom domainAxiom : domainList){
                OWLClass domainCls = domainAxiom.getDomain().asOWLClass();
                if(diseaseClsSet.contains(domainCls)){
                    axiomToAdd.add(new AddAxiom(disAxOnt, domainAxiom));
                    System.out.println("adding domain axiom: " + domainAxiom);
                }

            }

            for(OWLObjectPropertyRangeAxiom rangeAxiom : rangeList){
                OWLClassExpression rangeCls = rangeAxiom.getRange().asOWLClass();
                if(diseaseClsSet.contains(rangeCls)){
                    axiomToAdd.add(new AddAxiom(disAxOnt, rangeAxiom));
                    System.out.println("adding range axiom " + rangeAxiom);
                }

            }
        }

        //System.out.println(diseaseClsSet);
        //Now check for any other classes in localEFO whose axioms include reference to some disease
        //OWLObjectProperty bearerOf = df.getOWLObjectProperty(IRI.create("http://purl.org/obo/owl/OBO_REL#bearer_of"));

        for(OWLClass c : localEFO.getClassesInSignature()){

            for(OWLDisjointClassesAxiom djAx : localEFO.getDisjointClassesAxioms(c)){
                Set<OWLClassExpression> djCls = djAx.getClassExpressions();
                for(OWLClassExpression djExp : djCls){
                    if(djExp.isAnonymous() == false) {
                        //System.out.println("djExp = " + djExp + " \nand djExp class = " + djExp.getClass());
                        OWLClass namedCls = djExp.asOWLClass();
                        if (diseaseClsSet.contains(namedCls)) {
                            axiomToAdd.add(new AddAxiom(disAxOnt, djAx));
                            System.out.println("adding dj axiom: " + djAx + "for class " + c);
                        }
                    }
                }

            }

            for(OWLEquivalentClassesAxiom eqAx : localEFO.getEquivalentClassesAxioms(c)){
                if(eqAx.containsNamedEquivalentClass()){
                    for(OWLClass namedClassInAxiom : eqAx.getClassesInSignature()){
                        if(diseaseClsSet.contains(namedClassInAxiom)){
                            axiomToAdd.add(new AddAxiom(disAxOnt, eqAx));
                            //System.out.println(c + " contains a class from disease list of class " + namedClassInAxiom);
                        }
                    }
                }
            }

            for(OWLSubClassOfAxiom subAx : localEFO.getSubClassAxiomsForSubClass(c)){
                OWLClassExpression superCls = subAx.getSuperClass();

                if(superCls instanceof OWLAnonymousClassExpression){
                    for(OWLClass namedClassInAxiom : superCls.getClassesInSignature()){
                        if(diseaseClsSet.contains(namedClassInAxiom)){
                            axiomToAdd.add(new AddAxiom(disAxOnt, subAx));
                            //System.out.println("superclass expression for class: " + c + " is " + superCls );
                            //System.out.println("with axiom: " + subAx);
                        }
                    }
                }

                /*if(superCls instanceof OWLObjectSomeValuesFrom){
                    OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) superCls;
                    if (svf.getProperty().equals(bearerOf)) {
                        axiomToAdd.add(new AddAxiom(disAxOnt, subAx));
                        System.out.println("superclass expression for class: " + c + " is " + superCls );
                        System.out.println("with axiom: " + subAx);

                    }

                }*/
            }
        }


        manager.applyChanges(moduleToAdd);
        manager.applyChanges(axiomToAdd);

        System.out.println(disAxOnt);
        System.out.println(moduleOnt);

        manager.saveOntology(disAxOnt, IRI.create(outAx.toURI()));
        manager.saveOntology(moduleOnt, IRI.create(outMod.toURI()));

        System.out.println("disease axioms saved.");
        System.out.println("disease module saved.");

    }
}
