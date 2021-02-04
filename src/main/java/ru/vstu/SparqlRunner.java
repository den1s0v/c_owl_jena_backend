package ru.vstu;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.ext.xerces.xs.StringList;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
//import org.apache.jena.util.PrintUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class SparqlRunner {
    public static void main(String args[]) {

        long startTime = System.nanoTime();
        if(args.length < 3) {
            System.out.println("Please provide 3 commandline arguments:\n" +
                    " 1) Path to input RDF file\n" +
                    " 2) Path to SPARQL rule-style queries file\n" +
                    " 3) Path to location where to store the output N-Triples file\n" +
                    ""
            );
            if(false)  /// debug only
                runReasoning("c:\\D\\Work\\YDev\\CompPr\\c_owl\\test_data\\test_make_trace_output.rdf",
                        "c:\\D\\Work\\YDev\\CompPr\\c_owl\\sparql_from_swrl.ru",
                        "sparql_output.n3");
        } else {
            runReasoning(args[0], args[1], args[2]);

//        testGenericReasoner();
//        tutorial_3();
        }

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Total run time of the SPARQL runnung: " + String.valueOf((float)(estimatedTime / 1000 / 1000) / 1000) + " seconds.");
        System.exit(0);
    }

    public static String joinList(String sep, List<String> list) {
        StringBuilder res = new StringBuilder();
        for (String el : list) {
            if (res.length() > 0)
                res.append(sep);
            res.append(el);
        }
        return res.toString();
    }

    public static List<String> splitRules(String rules_str) {
        final String sep = " ;";
        final String PREFIX = "PREFIX";
        List<String> prefixLines = new ArrayList<String>();
        List<String> separateQueries = new ArrayList<String>();
        String[] lines = rules_str.split("\n");
        StringBuilder query = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("#") || line.isEmpty())
                continue;
            if (line.startsWith(PREFIX)) {
                prefixLines.add(line);
                continue;
            }
            query.append("\n").append(line);
            if (line.contains(sep)){
                separateQueries.add(joinList("\n", prefixLines) + query);
            }
        }
        return separateQueries;
    }

    public static void runReasoning(String in_rdf_url, String rules_path, String out_rdf_path) {
        // Register a namespace for use in the rules
//        String baseURI = "http://vstu.ru/poas/ctrl_structs_2020-05_v1#";
//        PrintUtil.registerPrefix("my", baseURI);

        // read rules as string
        String rules_str;
        try {
            rules_str = new String(Files.readAllBytes(Paths.get(rules_path)));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot read file: " + rules_path);
            return;
        }

        List<String> rules = splitRules(rules_str);

        Model data = RDFDataMgr.loadModel(in_rdf_url);
        // Construct a dataset basing on the loaded model
        Dataset dataset = DatasetFactory.create(data);

        List<UpdateRequest> requests = new ArrayList<>();
        // Build up the request then execute it.
        // This is the preferred way for complex sequences of operations.
        for (String rule_str : rules) {
            UpdateRequest request = UpdateFactory.create();
            request.add(rule_str);
            requests.add(request);
        }

        long startTime = System.nanoTime();  // start intensive work
        long lapTime = startTime;

        long prev_NTriples = 0;
        long NTriples = data.size();
        System.out.println("Starting reasoning from NTriples: " + NTriples);
        for(int i = 1; prev_NTriples < NTriples && i < 1000; i+=1)
        {
            // perform the operations.
            for (UpdateRequest request : requests) {
                UpdateAction.execute(request, dataset);
                System.out.print(".");
            }
            System.out.println();

            // retrieve the size of model
            prev_NTriples = NTriples;
            NTriples = dataset.getDefaultModel().size();

            // measure the time of iteration
            String elapsedTime = String.valueOf((float)((System.nanoTime() - lapTime) / 1000 / 1000) / 1000);
            lapTime = System.nanoTime();
            System.out.println("Iteration: " + i + ", NTriples: " + NTriples + " \t("+elapsedTime+" s.)");
        }
//        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
//        InfModel inf = ModelFactory.createInfModel(reasoner, data);
//        inf.prepare();

        long estimatedTime = System.nanoTime() - startTime;  // measure time of intensive work
        System.out.println("Time spent on reasoning: " + String.valueOf((float)(estimatedTime / 1000 / 1000) / 1000) + " seconds.");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(out_rdf_path);
            RDFDataMgr.write(out, dataset.getDefaultModel(), Lang.NTRIPLES);  // Lang.NTRIPLES  or  Lang.RDFXML

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Cannot write to file: " + out_rdf_path);
        }
    }

    /** Build an update request up out of individual Updates specified as strings.
 *  See UpdateProgrammatic for another way to build up a request.
 *  These two approaches can be mixed.
 */

//private class UpdateExecuteOperations
//{
    static { LogCtl.setLogging(); }
//    public static void main(String []args)
//    {
//        // Create an empty DatasetGraph (has an empty default graph and no named graphs)
//        Dataset dataset = DatasetFactory.createTxnMem() ;
//
//        ex1(dataset) ;
//        ex2(dataset) ;
//        ex3(dataset) ;
//    }

    public static void ex1(Dataset dataset)
    {
        // Execute one operation.
        UpdateAction.parseExecute("LOAD <file:etc/update-data.ttl>", dataset) ;
    }

    public static void ex2(Dataset dataset)
    {
        // Execute a series of operations at once.
        // See ex3 for a better way to build up a request
        // For maximum portability, multiple operations should be separated by a ";".
        // The "\n" imporves readability and parser error messages.
        String cmd = String.join(" ;\n",
                "DROP ALL",
                "CREATE GRAPH <http://example/g2>",   // Not needed for most datasets
                "LOAD <file:etc/update-data.ttl> INTO GRAPH <http://example/g2>") ;
        // check string created
        System.out.println(cmd) ;
        UpdateAction.parseExecute(cmd, dataset) ;
    }

    public static void ex3(Dataset dataset)
    {
        // Build up the request then execute it.
        // This is the preferred way for complex sequences of operations.
        UpdateRequest request = UpdateFactory.create() ;
        request.add("DROP ALL")
                .add("CREATE GRAPH <http://example/g2>") ;
        // Different style.
        // Equivalent to request.add("...")
        UpdateFactory.parse(request, "LOAD <file:etc/update-data.ttl> INTO GRAPH <http://example/g2>") ;

        // And perform the operations.
        UpdateAction.execute(request, dataset) ;

        System.out.println("# Debug format");
        SSE.write(dataset) ;

        System.out.println();

        System.out.println("# N-Quads: S P O G") ;
        RDFDataMgr.write(System.out, dataset, Lang.NQUADS) ;
    }
}
