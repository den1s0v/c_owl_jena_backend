package ru.vstu;

import org.apache.thrift.TException;

// Generated code
import ru.vstu.thrift_gen_server.*;

import java.util.HashMap;

public class ServerRequestHandler implements JenaReasoner.Iface {

//    private HashMap<Integer,SharedStruct> log;

    public ServerRequestHandler() {
//        log = new HashMap<Integer, SharedStruct>();
    }

    public boolean ping() {
        System.out.println("ping()");
        return true;
    }

    public void saveRdf(java.nio.ByteBuffer rdfData, java.lang.String filename) throws org.apache.thrift.TException {
        System.out.println("saveRdf(" +rdfData + ", " + filename + ")");
    }

    public java.nio.ByteBuffer runReasoner(java.nio.ByteBuffer rdfData, java.lang.String rulePaths) throws org.apache.thrift.TException {
        /// !!!
        return rdfData;
    }

    public void stop() {
        System.out.println("\nStopping the server now as received the stop() signal.");
        System.exit(0);
    }

}

