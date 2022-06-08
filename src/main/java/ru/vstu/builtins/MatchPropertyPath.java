package ru.vstu.builtins;


import java.util.*;

import org.apache.jena.graph.* ;
import org.apache.jena.reasoner.rulesys.* ;
import org.apache.jena.reasoner.rulesys.builtins.* ;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * PP(S, P1, P2, ..., Pn, O) Finds all paths from S to O through sequenced properties P*, each of whose can be
 *  an ordinal property or
 *  a string literal containing a SPARQL 1.1 Property Path expression (negation `!` is not supported!).
 * This operation is expensive.
 */
public class MatchPropertyPath extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "PP";
    }

    /* Do not override to keep arbitrary number of parameters *
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
    @Override
    public int getArgLength() {
        return 3;
    }
     */

    /**
     * Check the argument list length.
     */
    @Override
    public void checkArgs(int length, RuleContext context) {
        int expected = 3;
        if (length < expected) {
            throw new BuiltinException(this, context, "builtin " + getName() + " requires at least " + expected + " arguments but saw " + length);
        }
    }

    void parsePropertyPath(Node[] args, int startIndex, int endIndex) {

//        Path pp = PathFactory.pathLink(RDF.type.asNode());
//        pp.


    }

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        checkArgs(length, context);
        List<Node> values = new ArrayList<>();
        Node S = getArg(0, args, context);
        Node O = getArg(length - 1, args, context);


//        for (Iterator<Triple> ni = context.find(a0, a1, null); ni.hasNext(); ) {
//            Node v = ni.next().getObject();
//            if (v.isLiteral()) {
//                // Can't just use contains because distinct objects may
//                // be semantically equal
//                boolean gotit = false;
//                for ( Node value : values )
//                {
//                    if ( v.sameValueAs( value ) )
//                    {
//                        gotit = true;
//                        break;
//                    }
//                }
//                if (!gotit) {
//                    values.add(v);
//                }
//            }
//        }
        return context.getEnv().bind(args[2], Util.makeIntNode(values.size()));
    }

}