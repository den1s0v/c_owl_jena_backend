package ru.vstu.builtins;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
//import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.FBRuleInfGraph;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.*;

import java.util.*;

public class PPUtil {

    static final MatchOptions defaultMatchOptions = new MatchOptions();


    public static class PathMatch {
        public List<Node> Ss;  // coherent lists
        public List<Node> Os;  //  i.e. i-th item of Ss maps to i-th item of Os.
        //// PPath ppath = null;
        PathMatch() {
            Ss = new ArrayList<>();
            Os = new ArrayList<>();
        }
        PathMatch(Node s, Node o) {
            Ss = new ArrayList<>(); Ss.add(s);
            Os = new ArrayList<>(); Os.add(o);
        }
        PathMatch(Set<Node> ss, Set<Node> os) {
            Ss = new ArrayList<>(ss);
            Os = new ArrayList<>(os);
        }
        PathMatch(PathMatch other) {
            Ss = new ArrayList<>(other.Ss);
            Os = new ArrayList<>(other.Os);
        }
        public boolean isEmpty() {
            return Ss.isEmpty() && Os.isEmpty();
        }
        public int size() {
            assert Ss.size() == Os.size();
            return Ss.size();
        }
        public void add(Node s, Node o) {
            Ss.add(s); Os.add(o);
        }
        public void addAll(PathMatch other) {
            if (other.size() > 0)
                Ss.addAll(other.Ss); Os.addAll(other.Os);
        }
        public List<Pair<Node, Node>> getPairs() {
            List<Pair<Node, Node>> pairs = new ArrayList<>();
            int size = Ss.size();
            for (int i = 0; i < size; ++i) {
                pairs.add(new ImmutablePair<>(Ss.get(i), Os.get(i)));
            }
            return pairs;
        }
        public void remove(Node s, Node o) {
            int size = Ss.size();
            if (s == null && o != null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (Os.get(i) == o) {
                        Ss.remove(i);
                        Os.remove(i);
                    }
                }
            }
            else if (s != null && o == null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (Ss.get(i) == s) {
                        Ss.remove(i);
                        Os.remove(i);
                    }
                }
            }
            else if (s != null /*&& o != null*/) {
                for (int i = size - 1; i >= 0; --i) {
                    if (Ss.get(i) == s && Os.get(i) == o) {
                        Ss.remove(i);
                        Os.remove(i);
                        break;  // assume only one unique match exists
                    }
                }
            }
        }
        public void remove(PathMatch other) {
            remove(other.Ss, other.Os);
        }
        public void remove(Collection<Node> s, Collection<Node> o) {
            int size = Ss.size();
            if (s == null && o != null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (o.contains(Os.get(i))) {
                        Ss.remove(i);
                        Os.remove(i);
                    }
                }
            }
            else if (s != null && o == null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (s.contains(Ss.get(i))) {
                        Ss.remove(i);
                        Os.remove(i);
                    }
                }
            }
            else if (s != null /*&& o != null*/) {
                for (int i = size - 1; i >= 0; --i) {
                    if (s.contains(Ss.get(i)) && o.contains(Os.get(i))) {
                        Ss.remove(i);
                        Os.remove(i);
                    }
                }
            }
        }
        public boolean retainAll(Collection<Node> s, Collection<Node> o) {
            boolean changed = false;
            int size = Ss.size();
            if (s == null && o != null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (!o.contains(Os.get(i))) {
                        Ss.remove(i);
                        Os.remove(i);
                        changed = true;
                    }
                }
            }
            else if (s != null && o == null) {
                for (int i = size - 1; i >= 0; --i) {
                    if (!s.contains(Ss.get(i))) {
                        Ss.remove(i);
                        Os.remove(i);
                        changed = true;
                    }
                }
            }
            else if (s != null /*&& o != null*/) {
                for (int i = size - 1; i >= 0; --i) {
                    if (!(s.contains(Ss.get(i)) && o.contains(Os.get(i)))) {
                        Ss.remove(i);
                        Os.remove(i);
                        changed = true;
                    }
                }
            }
            return changed;
        }
    }

    public static class MatchOptions {
        public final boolean any;
        MatchOptions() {any = false;}
        MatchOptions(boolean any) {this.any = any;}
    }


    public abstract static class PPath {
        public PathMatch match(Node S, Node O, RuleContext context) {
            return match(S, O, context, defaultMatchOptions);
        }
        public abstract PathMatch match(Node S, Node O, RuleContext context, MatchOptions options);
        /** Searching over set of candidates (or within only if both provided) */
        public PathMatch match(Collection<Node> Ss, Collection<Node> Os, RuleContext context) {
            return match(Ss, Os, context, defaultMatchOptions);
        }
        public PathMatch match(Collection<Node> Ss, Collection<Node> Os, RuleContext context, MatchOptions options) {
            PathMatch matches = new PathMatch();
//            if (Ss != null && Ss.isEmpty() || Os != null && Os.isEmpty())
//                return matches;  // early exit
            if (Ss != null && (Os == null || Ss.size() <= Os.size())) {
                for(Node s: Ss) {
                    PathMatch innerMatches = match(s, null, context);
                    if (Os != null) {
                        innerMatches.retainAll(null, Os);
                    }
                    matches.addAll(innerMatches);
                }
            }
            else if (Os != null /*&& (Ss == null || Os.size() <= Ss.size())*/) {
                for(Node O: Os) {
                    PathMatch innerMatches = match(null, O, context);
                    if (Ss != null) {
                        innerMatches.retainAll(Ss, null);
                    }
                    matches.addAll(innerMatches);
                }
            }
            return matches;
        }
    }

    public static class PPSimple extends PPath {
        PPSimple(Node property) {this.property = property;}
        PPSimple(Node property, boolean inverse) {this.property = property; this.inverse = inverse;}
        Node property;
        boolean inverse = false;

        @Override
        public PathMatch match(Node S, Node O, RuleContext context, MatchOptions options) {
            PathMatch matches = new PathMatch();
            Iterator<Triple> ni;
            if (!inverse)
                ni = context.find(S, property, O);
            else
                ni = context.find(O, property, S);
            while (ni.hasNext()) {
                Node s = ni.next().getSubject();
                Node o = ni.next().getObject();
                matches.add(s, o);
                if (options.any)
                    break;
            }
            return matches;
        }
    }

    public static class PPSeq extends PPath {
        PPSeq(List<PPath> paths) {this.paths = paths;}
        List<PPath> paths;

        @Override
        public PathMatch match(Node S, Node O, RuleContext context, MatchOptions options) {
            if (paths.isEmpty())
                return new PathMatch();
            int size = paths.size();
            if (size == 1) {
                return paths.get(0).match(S, O, context, options);
            }

            // two or more paths in the sequence ...
            List<PathMatch> matches = new ArrayList<>(size);

            PathMatch m;
            // very left
            m = paths.get(0).match(S, null, context);
            if (m.isEmpty()) return m;  // exit immediately
            matches.set(0, m);

            // very right
            m =  paths.get(size - 1).match(null, O, context);
            if (m.isEmpty()) return m;  // exit immediately
            matches.set(size - 1, m);

            int L = 0, dL = +1;
            int R = size - 1, dR = -1;

            // use simple strategy of intersecting smallest sets first
            while (L < R) {
                boolean nowLeft = matches.get(L).size() <= matches.get(R).size();
                if (nowLeft) {
                    PathMatch prevMatch = matches.get(L);
                    L += dL;
                    m = paths.get(L).match(prevMatch.Os, null, context);
                    if (m.isEmpty()) return m;  // exit immediately
                    matches.set(L, m);
                }
                else {
                    PathMatch prevMatch = matches.get(R);
                    R += dR;
                    m = paths.get(R).match(null, prevMatch.Ss, context);
                    if (m.isEmpty()) return m;  // exit immediately
                    matches.set(R, m);
                }
                if (nowLeft || L == R)
                {
                    // propagate reduces back
                    for (int i = L - dL; i >= 0; i -= dL) {
                        boolean changed = matches.get(i).retainAll(null, matches.get(i + dL).Ss);
                        if (!changed) break;
                    }
                }
                if (!nowLeft || L == R)
                {
                    // propagate reduces back
                    for (int i = R - dR; i < size; i -= dR) {
                        boolean changed = matches.get(i).retainAll(matches.get(i + dR).Os, null);
                        if (!changed) break;
                    }
                }
            }

            // now find mappings from subjects to objects
            PathMatch finalMatch = new PathMatch();
            for (Node s : matches.get(0).Ss) {
                // find what `o` matches `s` through the sequence of paths
                Set<Node> nodeSet = new HashSet<Node>(List.of(s));
                for (PathMatch mm : matches) {
                    Set<Node> newNodeSet = new HashSet<>();
                    for (Pair<Node, Node> pair: mm.getPairs()) {
                        if (nodeSet.contains(pair.getLeft())) {
                            newNodeSet.add(pair.getRight());
                        }
                    }
                    if (newNodeSet.isEmpty())
                        break;
                    nodeSet = newNodeSet;
                }
                // add the mappings found
                for (Node o : nodeSet) {
                    finalMatch.add(s, o);
                    if (options.any)
                        return finalMatch;
                }
            }

            return finalMatch;
        }
    }

    public static class PPAlt extends PPath {
        PPAlt(List<PPath> paths) {this.paths = paths;}
        List<PPath> paths;

        @Override
        public PathMatch match(Node S, Node O, RuleContext context, MatchOptions options) {
            if (paths.isEmpty())
                return new PathMatch();
            if (paths.size() == 1) {
                return paths.get(0).match(S, O, context, options);
            }

            // two or more paths in the alternative ...
            PathMatch matches = new PathMatch();
            PathMatch m;
            for (PPath pp : paths) {
                m = pp.match(S, O, context, options);
                if (!m.isEmpty()) {
                    if (options.any)
                        return m;  // early exit
                    if (!matches.isEmpty())
                        m.remove(matches);  // remove new duplicates
                    matches.addAll(m);
                }
            }

            return matches;
        }
    }

    public static class PPRepetition extends PPath {
        PPRepetition(PPath path, int minRepeat, int maxRepeat) {this.path = path; this.minRepeat = minRepeat; this.maxRepeat = maxRepeat; }
        PPRepetition(PPath path, int minRepeat) {this.path = path; this.minRepeat = minRepeat; this.maxRepeat = -1; }
        PPath path;
        int minRepeat, maxRepeat;

        @Override
        public PathMatch match(Node S, Node O, RuleContext context, MatchOptions options) {
            PathMatch matches = new PathMatch();
            if (minRepeat == 0) {
                if (S == null && O != null)
                    matches.add(O, O);
                else if (S != null && O == null)
                    matches.add(S, S);
                else if (S != null /*&& O != null*/)
                    // check that S and O are exactly the same.
                    // TODO: think about checking owl:SameAs here (and everywhere too?).
                    if (S.equals(O)) {
                        matches.add(S, O);
                        return matches;  // solution is found
                    }
            }
            if (maxRepeat == 0) {
                return matches;
            }
            PathMatch finalMatch; // will accumulate final result (starting from minRepeat)
            //  `matches` will track s -- o mapping

            // initial 1 step
            PathMatch m;
            m = path.match(S, O, context, options);
            matches.addAll(m);

            if (minRepeat <= 1) {
                if (maxRepeat == 1 || options.any && !m.isEmpty()) {
                    return matches;
                }
                finalMatch = matches;
            } else {
                finalMatch = new PathMatch();
            }

            // two or more (maxRepeat) steps in the repetition ...

            // search when any of nodes is unbound (S or O or both)
            if (S == null || O == null) {
                int L = 0, dL = -1;
                int R = 1, dR = +1;

                Set<Node> leftWaveNodes = new HashSet<>(matches.Ss);
                Set<Node> rightWaveNodes = new HashSet<>(matches.Os);

                Set<Node> visitedNodes = new HashSet<>();
                visitedNodes.addAll(leftWaveNodes);
                visitedNodes.addAll(rightWaveNodes);

                // use simple strategy of breadth-first search growing till reached maximum steps of nothing found
                while (!leftWaveNodes.isEmpty() && !rightWaveNodes.isEmpty()
                        && (maxRepeat < 0 || R - L < maxRepeat)) {
                    if (S == null) { // can grow left
                        L += dL;
                        Set<Node> newWaveNodes = new HashSet<>();
                        for (Node n : leftWaveNodes) {
                            m = path.match(null, n, context);
                            if (m.isEmpty())
                                continue;
                            Set<Node> newNodes = new HashSet<>(m.Ss);
                            newNodes.removeAll(visitedNodes);
                            if (newNodes.isEmpty())
                                continue;
                            // add to matches keeping "wide" s--o mapping
                            for (Pair<Node, Node> pair : matches.getPairs()) {
                                if (pair.getLeft() != n)
                                    continue;
                                Node o = pair.getRight();
                                for (Node newN : newNodes) {
                                    matches.add(newN, o);
                                    if (minRepeat <= R - L) {
                                        finalMatch.add(newN, o);
                                        if (options.any) return finalMatch;
                                    }
                                }
                            }
                            newWaveNodes.addAll(newNodes);
                        }
                        leftWaveNodes = newWaveNodes;
                        visitedNodes.addAll(newWaveNodes);
                    }
                    if (O == null) { // can grow right
                        R += dR;
                        Set<Node> newWaveNodes = new HashSet<>();
                        for (Node n : rightWaveNodes) {
                            m = path.match(n, null, context);
                            if (m.isEmpty())
                                continue;
                            Set<Node> newNodes = new HashSet<>(m.Os);
                            newNodes.removeAll(visitedNodes);
                            if (newNodes.isEmpty())
                                continue;
                            // add to matches keeping "wide" s--o mapping
                            for (Pair<Node, Node> pair : matches.getPairs()) {
                                if (pair.getRight() != n)
                                    continue;
                                Node s = pair.getLeft();
                                for (Node newN : newNodes) {
                                    matches.add(s, newN);
                                    if (minRepeat <= R - L) {
                                        finalMatch.add(s, newN);
                                        if (options.any) return finalMatch;
                                    }
                                }
                            }
                            newWaveNodes.addAll(newNodes);
                        }
                        rightWaveNodes = newWaveNodes;
                        visitedNodes.addAll(newWaveNodes);
                    }
                }
            }
            else  // search for a connection when both nodes are bound (S and O)
            {
                int L = 0, dL = +1;
                int R = 0, dR = +1;

                Set<Node> leftWaveNodes = new HashSet<>(List.of(S));
                Set<Node> rightWaveNodes = new HashSet<>(List.of(O));

                Set<Node> visitedLeftNodes = new HashSet<>(leftWaveNodes);
                Set<Node> visitedRightNodes = new HashSet<>(rightWaveNodes);

                // use simple strategy of breadth-first search growing till a connection found or reached maximum steps of nothing found
                while (!leftWaveNodes.isEmpty() && !rightWaveNodes.isEmpty()
                        && (maxRepeat < 0 || R + L < maxRepeat)) {
                    if (leftWaveNodes.size() <= rightWaveNodes.size()) { // it's better to grow from left
                        L += dL;
                        Set<Node> newWaveNodes = new HashSet<>();
                        for (Node n : leftWaveNodes) {
                            m = path.match(n, null, context);
                            if (m.isEmpty())
                                continue;
                            Set<Node> newNodes = new HashSet<>(m.Os);
                            newNodes.removeAll(visitedLeftNodes);
                            if (newNodes.isEmpty())
                                continue;
                            // check intersection with other group
                            if (minRepeat <= R + L) {
                                Set<Node> intersection = new HashSet<>(newNodes); // use the copy constructor
                                intersection.retainAll(visitedRightNodes);
                                if (!intersection.isEmpty()) {
                                    finalMatch.add(S, O);
                                    return finalMatch;
                                }
                            }

                            newWaveNodes.addAll(newNodes);
                        }
                        leftWaveNodes = newWaveNodes;
                        visitedLeftNodes.addAll(newWaveNodes);
                    }
                    else   // it's better to grow from right
                    {
                        R += dR;
                        Set<Node> newWaveNodes = new HashSet<>();
                        for (Node n : rightWaveNodes) {
                            m = path.match(null, n, context);
                            if (m.isEmpty())
                                continue;
                            Set<Node> newNodes = new HashSet<>(m.Ss);
                            newNodes.removeAll(visitedRightNodes);
                            if (newNodes.isEmpty())
                                continue;
                            // check intersection with other group
                            if (minRepeat <= R + L) {
                                Set<Node> intersection = new HashSet<>(newNodes); // use the copy constructor
                                intersection.retainAll(visitedLeftNodes);
                                if (!intersection.isEmpty()) {
                                    finalMatch.add(S, O);
                                    return finalMatch;
                                }
                            }

                            newWaveNodes.addAll(newNodes);
                        }
                        rightWaveNodes = newWaveNodes;
                        visitedRightNodes.addAll(newWaveNodes);
                    }
                }
            }

            return finalMatch;
        }
    }

    public static void main(String[] args) {
        // Using to parse PP with standard functions. Use this classes to build my own tree.
        Path path = PathParser.parse("rdf:type/rdfs:subClassOf*", PrefixMapping.Standard);

        if (path instanceof P_Seq)
            ((P_Seq)path).getLeft();

        path.visit(new PathVisitorBase() {
            @Override
            public void visit(P_Seq pathSeq) {
                super.visit(pathSeq);
                pathSeq.getLeft();
            }
        });

        String s = path.toString();

//        Graph g = new FBRuleInfGraph();
//        Model m = ModelFactory.createModelForGraph(g);

        // m.(path)
//        PathLib.execTriplePath()

        int i = 1;
    }
}
