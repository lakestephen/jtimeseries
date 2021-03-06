/**
 * Copyright (C) 2011 (nick @ objectdefinitions.com)
 *
 * This file is part of JTimeseries.
 *
 * JTimeseries is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTimeseries is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.od.jtimeseries.identifiable;

import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;

import java.util.*;

/**
 * An event generated when the tree of identifiable nodes changes
 */
public class IdentifiableTreeEvent {

    private final Identifiable rootNode;
    private final String path;
    private final Map<Identifiable, Collection<Identifiable>> nodes;
    private final TreeEventType type;
    private final Object changeDescription;

    public IdentifiableTreeEvent(TreeEventType type, Identifiable rootNode, String path, Identifiable node) {
       this(type, rootNode, path, createSingletonMap(node, Collections.<Identifiable>emptyList()), null);
    }

    public IdentifiableTreeEvent(TreeEventType type, Identifiable rootNode, String path, Identifiable node, Object changeDescription) {
       this(type, rootNode, path, createSingletonMap(node, Collections.<Identifiable>emptyList()), changeDescription);
    }

    public IdentifiableTreeEvent(TreeEventType type, Identifiable rootNode, String path, Identifiable node, Collection<Identifiable> children) {
       this(type, rootNode, path, createSingletonMap(node, children), null);
       //       if ( children.size() > 0 ) {
       //           System.out.println("Created TreeEvent " + this + " with child count " + children.size() + " on node " + node);
       //       }
    }

    /**
     * @param rootNode, the root node of the tree which is changing
     * @param path, path to parent node of nodes which have changed
     * @param nodes, nodes which were modified
     * @param changeDescription, description of change for change events, may be null
     */
    public IdentifiableTreeEvent(TreeEventType type, Identifiable rootNode, String path, Map<Identifiable, Collection<Identifiable>> nodes, Object changeDescription) {
        this.type = type;
        this.rootNode = rootNode;
        this.path = path;
        this.nodes = nodes;
        this.changeDescription = changeDescription;
    }

    /**
     * @return the path to the parent node of the nodes which have changed
     */
    public String getPath() {
        return path;
    }

    /**
     * @return a list of the nodes affected by the event, you should not modify this list
     */
    public Collection<Identifiable> getNodes() {
        return nodes.keySet();
    }

     /**
     * @return a list of the nodes affected by the event and their descendants at the time the event was fired, you should not modify this list
     */
    public Map<Identifiable, Collection<Identifiable>> getNodesWithDescendants() {
        return nodes;
    }

    /**
     * @return the root node of the tree which is changing
     */
    public Identifiable getRootNode() {
        return rootNode;
    }

    public TreeEventType getType() {
        return type;
    }

    public Object getChangeDescription() {
        return changeDescription;
    }

    public <E extends Identifiable> void processNodesAndDescendants(IdentifiableProcessor<E> i, Class<E> clazz) {
        for ( Map.Entry<Identifiable,Collection<Identifiable>> e :  getNodesWithDescendants().entrySet()) {
            //first process top level node
            if ( clazz.isAssignableFrom(e.getKey().getClass())) {
                i.process((E)e.getKey());
            }

            //now its descendants
            for ( Identifiable s : e.getValue()) {
                if ( clazz.isAssignableFrom(s.getClass())) {
                    i.process((E)s);
                }
            }
        }
    }

    public static interface IdentifiableProcessor<E> {
        void process(E identifiable);
    }

    public String toString() {
        return "IdentifiableTreeEvent{" +
            "type=" + type +
            ", path='" + path + '\'' +
            ", rootNode=" + rootNode +
            '}';
    }

    private static Map<Identifiable, Collection<Identifiable>> createSingletonMap(Identifiable node, Collection<Identifiable> c) {
        Map m = new HashMap<Identifiable, Collection<Identifiable>>();
        m.put(node, c);
        return m;
    }

    public static enum TreeEventType {
        CHANGE,
        ADD,
        REMOVE
    }
}
