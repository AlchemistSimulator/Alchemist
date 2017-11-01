//package it.unibo.alchemist.boundary.gui.effects;
//
//import it.unibo.alchemist.model.interfaces.Environment;
//import it.unibo.alchemist.model.interfaces.Node;
//import it.unibo.alchemist.model.interfaces.Position;
//import org.jooq.lambda.tuple.Tuple2;
//
//import java.util.Queue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ConcurrentMap;
//
//public abstract class NodeRelatedEffect extends AbstractEffect{
//    @Override
//    public <T> Queue<DrawCommand> computeDrawCommands(Environment<T> environment) {
//        return null;
//    }
//
//    public <T> ConcurrentMap<Node<T>, Tuple2<Position, ConcurrentLinkedQueue<Node<T>>>> getNodes(Environment<T> environment) {
//        final ConcurrentMap<Node<T>, Tuple2<Position, ConcurrentLinkedQueue<Node<T>>>> map = new ConcurrentHashMap<>();
//
//        environment.getNodes().forEach(node -> map.put(
//                node,
//                new Tuple2<Position, ConcurrentLinkedQueue<Node<T>>>(
//                        environment.getPosition(node),
//                        new ConcurrentLinkedQueue<>(environment.getNeighborhood(node).getNeighbors())
//        )));
//
//        return map;
//    }
//
//    public <T> DrawCommand drawNode(final Node<T> node, final Position position, final ConcurrentLinkedQueue<Node<T>> neighbors) {
//
//    }
//
//    @Override
//    public abstract int hashCode();
//
//    @Override
//    public abstract boolean equals(Object obj);
//}
