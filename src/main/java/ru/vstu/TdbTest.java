package ru.vstu;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vstu.util.Checkpointer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
//import ch.qos.logback.;

public class TdbTest {
    public static void main(String[] args) {
        Checkpointer ch = new Checkpointer();

//        org.apache.jena.atlas.logging.LogCtlLog4j2.
//        Logger lg = LoggerFactory.getLogger("org.apache.jena.riot");
//        org.apache.jena.atlas.logging.Log.setlog4j2();

//        Dataset ds = TDBFactory.createDataset();
////        ds.
//
//        DatasetGraph dsg = TDBFactory.createDatasetGraph();
////        dsg.

        ch.hit(null);
        String inputDatasetName = "triple-data/graphs.trig";
        Dataset dataset = RDFDataMgr.loadDataset(inputDatasetName) ;
        ch.hit("Load completed");

//        // Make a TDB-backed dataset
//        String directory = "triple-data" ;
//        Dataset dataset = TDBFactory.createDataset(directory) ;

//        String assemblerFile = "triple-data/tdb-assembler.ttl" ;
//        Dataset dataset = TDBFactory.assembleDataset(assemblerFile) ;

//        // add graphs
//        // create an empty model
//        Model temp_model = ModelFactory.createDefaultModel();
//        // use the RDFDataMgr to find the input file
//        String inputFileName = "triple-data/g-10.ttl";
//        // read the RDF/XML file
//        temp_model.read(inputFileName);
//
//        dataset.addNamedModel("g-10", temp_model);

        List<String> names = Lists.newArrayList(dataset.listNames());
        System.out.println(Arrays.toString(names.toArray()));

        ch.hit("Names retrieved");

        Model model = dataset.getDefaultModel();
//        model.write(System.out, "TURTLE");

        ch.hit("Model retrieved");

        model = dataset.getNamedModel(names.get(0));
//        model.write(System.out, "TURTLE");

        ch.hit("Model-0 retrieved");

        model = dataset.getNamedModel(names.get(1));
//        model.write(System.out, "TURTLE");

        ch.hit("Model-1 retrieved");

        model = dataset.getNamedModel(names.get(4));
//        model.write(System.out, "TURTLE");

        ch.hit("Model-4 retrieved");

        //  ...
//        dataset.begin(ReadWrite.READ) ;
//        // Get model inside the transaction
//        Model model = dataset.getDefaultModel() ;
//        dataset.end() ;

        ch.since_start("whole app", false);
    }
}
