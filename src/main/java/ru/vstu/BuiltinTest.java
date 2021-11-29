package ru.vstu;


import java.io.IOException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;


//* This code is originated in https://stackoverflow.com/questions/23384243/custom-builtin-in-jena

public class BuiltinTest {

    public static void main(String[] args) throws /*OWLOntologyStorageException,
            OWLOntologyCreationException,*/ IOException {

        BuiltinRegistry.theRegistry.register(new BaseBuiltin() {
            @Override
            public String getName() {
                return "mysum";
            }
            @Override
            public int getArgLength() {
                return 3;
            }
            @Override
            public boolean bodyCall(Node[] args, int length, RuleContext context) {
                checkArgs(length, context);
                BindingEnvironment env = context.getEnv();
                Node n1 = getArg(0, args, context);
                Node n2 = getArg(1, args, context);
                if (n1.isLiteral() && n2.isLiteral()) {
                    Object v1 = n1.getLiteralValue();
                    Object v2 = n2.getLiteralValue();
                    Node sum = null;
                    if (v1 instanceof Number && v2 instanceof Number) {
                        Number nv1 = (Number)v1;
                        Number nv2 = (Number)v2;
                        int sumInt = nv1.intValue()+nv2.intValue();
                        sum = Util.makeIntNode(sumInt);
                        return env.bind(args[2], sum);
                    }
                }
                return false;
            }

        });

        // NON SERVE

        //      final String exampleRuleString2 =
        //              "[mat1: equal(?s ?p )\n\t-> print(?s ?p ?o),\n\t   (?s ?p ?o)\n]"+
        //                      "";

        final String exampleRuleString =
                "[matematica:"+
                        "(?p http://www.semanticweb.org/prova_rules_M#totale_crediti ?x), "+
                        "mysum(5,2, ?res)"+
                        " -> " +
//                        "(?p rdf:type  http://www.semanticweb.org/prova_rules_M#:Persona)"+
//                        "(?e rdf:type  http://www.semanticweb.org/prova_rules_M#:Esame)"+
                        "(?p  http://www.semanticweb.org/prova_rules_M#number ?res)"+
//                        "(?e http://www.semanticweb.org/prova_rules_M/persona#crediti_esame ?cr)"+
                        "]";

        System.out.println(exampleRuleString);

        /* I tend to use a fairly verbose syntax for parsing out my rules when I construct them
         * from a string. You can read them from whatever other sources.
         */
        final List<Rule> rules;
        try( final BufferedReader src = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(exampleRuleString.getBytes()))) ) {
            rules = Rule.parseRules(Rule.rulesParserFromReader(src));
        }


        /* Construct a reasoner and associate the rules with it  */
        // create an empty non-inferencing model

        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);


        /* Create & Prepare the InfModel. If you don't call prepare, then
         * rule firings and inference may be deferred until you query the
         * model rather than happening at insertion. This can make you think
         * that your Builtin is not working, when it is.
         */

        InfModel infModel = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        infModel.prepare();
//        infModel.createResource(RDFS.Class);
        final Property p2 = infModel.createProperty("http://www.semanticweb.org/prova_rules_M#number");
        final Resource s = infModel.createResource();
        final Property p = infModel.createProperty("http://www.semanticweb.org/prova_rules_M#totale_crediti");
        final Resource o = infModel.createResource();
        infModel.add(s,p,o);

        //write down the result in RDFXML form
        infModel.write(System.out);

    }
}