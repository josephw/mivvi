/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A BK-tree implementation over a {@link Metric}.
 * 
 * @author joe
 */
public class BkTree<K>
{
    private final Metric<K> metric;
    private final BkTreeNode root;
    
    public BkTree(Metric<K> m, Collection<K> contents)
    {
        this.metric = m;

        List<K> list = new ArrayList<K>(contents);
        Collections.shuffle(list);
        
        BkTreeNode root = null;
        
        for(K i : list) {
            if(root == null) {
                root = new BkTreeNode(i);
            } else {
                root.add(i);
            }
        }
        
        this.root = root;
    }

    public void find(K target, int maxDistance, Collection<K> results)
    {
        if (root != null) {
            root.find(target, maxDistance, results);
        }
    }
    
    class BkTreeNode
    {
        private final K value;
        private final SortedMap<Integer, BkTreeNode> children;

        public BkTreeNode(K v)
        {
            this.value = v;
            this.children = new TreeMap<Integer, BkTreeNode>();
        }
        
        void add(K n)
        {
            int d = metric.dist(value, n);

            BkTreeNode c = children.get(Integer.valueOf(d));
            if(c == null) {
                children.put(Integer.valueOf(d), new BkTreeNode(n));
            } else {
                c.add(n);
            }
        }
        
        public String toString()
        {
            return value + ":\n" + children;
        }
        
        void find(K target, int maxDistance, Collection<K> result)
        {
            int d = metric.dist(value, target);

            if(d <= maxDistance) {
                result.add(value);
            }
            
            for(int i = d - maxDistance; i <= d + maxDistance; i++) {
                BkTreeNode c = children.get(Integer.valueOf(i));
                if(c != null) {
                    c.find(target, maxDistance, result);
                }
            }
        }
    }
}
