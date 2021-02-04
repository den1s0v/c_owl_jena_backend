package ru.vstu;

import org.apache.jena.ext.xerces.impl.xs.util.StringListImpl;
import org.apache.jena.ext.xerces.xs.StringList;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.PrintUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/** Tutorial 3 Statement attribute accessor methods
 */
public class Main {
    public static void main(String args[]) {

        long startTime = System.nanoTime();
        if(args.length < 3) {
            System.out.println("Please provide (the optional mode and) 3 commandline arguments:\n" +
                    " 0) Mode to run in: 'jena' (the default) or 'sparql'\n" +
                    " 1) Path to input RDF file\n" +
                    " 2) Path to Jena rules file\n" +
                    " 3) Path to location where to store the output N-Triples file\n" +
                    ""
            );
            if(false)  /// debug only
                runReasoning("c:\\D\\Work\\YDev\\CompPr\\c_owl\\test_data\\test_make_trace_output.rdf",
                    "all.rules",
                    "jena_output.n3");
        } else {
            boolean is_Sparql = args.length == 4 && args[0].toLowerCase().contains("sparql");
            if(args.length == 4) {
                // remove the first 'mode' element
                args[0] = args[1];
                args[1] = args[2];
                args[2] = args[3];
            }
            if(is_Sparql) {
                System.out.println("Naive SPARQL mode.");
                SparqlRunner.runReasoning(args[0], args[1], args[2]);
            }
            else {
                System.out.println("Jena mode.");
                runReasoning(args[0], args[1], args[2]);
            }
        }

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Total run time " + String.valueOf((float)(estimatedTime / 1000 / 1000) / 1000) + " seconds.");
        System.exit(0);
    }

    public static String findIriForPrefix(String rules_path, String prefix) {
        // read rules as string
        List<String> rules_lines;
        try {
            rules_lines = (Files.readAllLines(Paths.get(rules_path)));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot read file: " + rules_path);
            return "";
        }

        String pattern = "@prefix " + prefix + ": ";
        for(String line : rules_lines)
        {
            if(line.startsWith(pattern))
            {
                int ib = line.indexOf('<') + 1;
                int ie = line.indexOf('>');
                String iri = line.substring(ib, ie);
                System.out.println("::::::: Found IRI for prefix '" + prefix + "': " + iri);
                return iri;
            }
        }
        System.out.println("::::::: Warning: IRI for prefix '" + prefix + "' not found!");

        return "";
    }

    public static void runReasoning(String in_rdf_url, String rules_path, String out_rdf_path) {
        // Register a namespace for use in the rules
//        String baseURI = "http://vstu.ru/poas/ctrl_structs_2020-05_v1#";
//        String baseURI = "http://penskoy.n/expressions1#
        String baseURI = findIriForPrefix(rules_path, "my");
        PrintUtil.registerPrefix("my", baseURI);

        List<Rule> rules = Rule.rulesFromURL(rules_path);
        Model data = RDFDataMgr.loadModel(in_rdf_url);

        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
//        reasoner.setOWLTranslation(true);               // not needed in RDFS case
//        reasoner.setTransitiveClosureCaching(true);     // not required when there is no use of transitivity

        long startTime = System.nanoTime();

        InfModel inf = ModelFactory.createInfModel(reasoner, data);
        inf.prepare();

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Time spent on reasoning: " + String.valueOf((float)(estimatedTime / 1000 / 1000) / 1000) + " seconds.");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(out_rdf_path);
            RDFDataMgr.write(out, inf, Lang.NTRIPLES);  // Lang.NTRIPLES  or  Lang.RDFXML

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Cannot write to file: " + out_rdf_path);
        }
    }
}