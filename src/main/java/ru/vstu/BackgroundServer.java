package ru.vstu;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import ru.vstu.thrift_gen_server.*;

public class BackgroundServer {

    public static ServerRequestHandler handler;
    public static JenaReasoner.Processor processor;


    public static void init() {
        init(20299);
    }

    public static void init(int port) {
        try {
            handler = new ServerRequestHandler();
            processor = new JenaReasoner.Processor(handler);

            Runnable simple = () -> simple(processor, port);
//            Runnable secure = new Runnable() {
//                public void run() {
//                    secure(processor);
//                }
//            };

            new Thread(simple).start();
//            new Thread(secure).start();
        } catch (Exception x) {
            x.printStackTrace();
        }

    }

    public static void simple(JenaReasoner.Processor processor, int port) {
        try {
            TServerTransport serverTransport = new TServerSocket(port);
            // Use this for a non-threaded server
            // TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        init();
    }
}