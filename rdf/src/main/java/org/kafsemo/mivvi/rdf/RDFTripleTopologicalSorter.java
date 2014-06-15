package org.kafsemo.mivvi.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

public class RDFTripleTopologicalSorter
{
    public static List<Statement> sorted(Model statements)
    {
        List<Resource> initial = new ArrayList<Resource>(onlyUsedAsSubjects(statements));
        Collections.sort(initial, RESOURCE_ORDER);

        RDFTripleTopologicalSorter sorter = new RDFTripleTopologicalSorter(statements);

        for (Resource r : initial)
        {
            sorter.recursivelyAppend(r);
        }

        sorter.writeRemaining();

        return Collections.unmodifiableList(sorter.result);
    }

    private final Model statements;

    private final List<Statement> result;

    private final Set<Resource> included;

    private RDFTripleTopologicalSorter(Model statements)
    {
        this.statements = statements;
        this.result = new ArrayList<Statement>(statements.size());
        this.included = new HashSet<Resource>(statements.subjects().size());
    }

    /**
     * Write any otherwise unwritten statements, which will be self-referential.
     */
    void writeRemaining()
    {
        List<Resource> remaining = new ArrayList<Resource>();

        for (Resource r : statements.subjects())
        {
            if (!included.contains(r))
            {
                remaining.add(r);
            }
        }
        Collections.sort(remaining, RESOURCE_ORDER);

        for (Resource r : remaining)
        {
            if (!included.contains(r))
            {
                recursivelyAppend(r);
            }
        }
    }

    private void recursivelyAppend(Resource r)
    {
        List<? extends Statement> l = new ArrayList<Statement>(statements.filter(r, null, null));
        Collections.sort(l, STATEMENT_ORDER);

        included.add(r);

        for (Statement s : l)
        {
            result.add(s);

            Value o = s.getObject();
            if (o instanceof Resource && !included.contains(o))
            {
                recursivelyAppend((Resource) o);
            }
        }
    }

    static Collection<Resource> onlyUsedAsSubjects(Model statements)
    {
        Set<Resource> onlySubjects = new HashSet<Resource>();

        for (Resource r : statements.subjects())
        {
            if (!statements.contains(null, null, r))
            {
                onlySubjects.add(r);
            }
        }

        return onlySubjects;
    }

    /**
     * Sort statements in an order that presents well in RDF/XML. Order statements:
     * <ol>
     * <li>By subject</li>
     * <li>With {@link RDF#TYPE} predicates first</li>
     * <li>Then statements with literals as objects</li>
     * <li>Then by predicate</li>
     * <li>Finally by object</li>
     * </ol>
     */
    static final Comparator<Statement> STATEMENT_ORDER = new Comparator<Statement>()
    {
        @Override
        public int compare(Statement arg0, Statement arg1)
        {
            CompBuilder cb = new CompBuilder();

            cb.compare(arg0.getSubject(), arg1.getSubject(), RESOURCE_ORDER);

            cb.compareTrueFirst(arg0.getPredicate().equals(RDF.TYPE), arg1.getPredicate().equals(RDF.TYPE));

            /* Sort statements with literals ahead of resources */
            cb.compareTrueFirst(arg0.getObject() instanceof Literal, arg1.getObject() instanceof Literal);

            cb.compare(arg0.getPredicate(), arg1.getPredicate(), PREDICATE_ORDER);

            cb.compare(arg0.getObject(), arg1.getObject(), OBJECT_ORDER);

            return cb.build();
        }
    };

    /**
     * rdf:type sorts first, indexes sort numerically.
     */
    static final Comparator<URI> PREDICATE_ORDER = new Comparator<URI>()
    {
        public int compare(URI o1, URI o2)
        {
            CompBuilder cb = new CompBuilder();

            cb.compareTrueFirst(o1.equals(RDF.TYPE), o2.equals(RDF.TYPE));
            cb.compareIndexUris(o1, o2);
            cb.compare(o1, o2, RESOURCE_ORDER);

            return cb.build();
        };
    };

    /**
     * Literals sort before resources.
     */
    static final Comparator<Value> OBJECT_ORDER = new Comparator<Value>()
    {
        public int compare(Value o1, Value o2)
        {
            return compareByType(Literal.class, LITERAL_ORDER, RESOURCE_ORDER, o1, o2);
        }
    };

    private static final Comparator<URI> URI_ORDER = new Comparator<URI>()
    {
        @Override
        public int compare(URI o1, URI o2)
        {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private static final Comparator<BNode> BNODE_ORDER = new Comparator<BNode>()
    {
        public int compare(BNode o1, BNode o2)
        {
            return o1.getID().compareTo(o2.getID());
        };
    };

    /**
     * <p>
     * Compare objects, placing instances of the specified type first, ahead of all other objects. Use the provided
     * <code>firstComparator</code> to sort those instances and the <code>secondComparator</code> to sort the others.
     * </p>
     * <p>
     * This is for cases where instances can be divided by type, such as sorting all {@link URI}s ahead of {@link BNode}
     * s, and then using different {@link Comparator}s to sort within those categories.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private static final <A, B> int compareByType(Class<A> first, Comparator<A> firstComparator,
            Comparator<B> secondComparator, Object o1, Object o2)
    {
        if (first.isInstance(o1))
        {
            if (first.isInstance(o2))
            {
                return firstComparator.compare((A) o1, (A) o2);
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if (!first.isInstance(o2))
            {
                return secondComparator.compare((B) o1, (B) o2);
            }
            else
            {
                return 1;
            }
        }
    }

    /**
     * URIs sort before BNodes; URIs sort by URI, BNodes by ID.
     */
    static final Comparator<Resource> RESOURCE_ORDER = new Comparator<Resource>()
    {
        public int compare(Resource o1, Resource o2)
        {

            return compareByType(URI.class, URI_ORDER, BNODE_ORDER, o1, o2);
        }
    };

    private static String index(URI u)
    {
        String s = u.stringValue();
        if (s.startsWith(RDF.NAMESPACE))
        {
            return s.substring(RDF.NAMESPACE.length());
        }
        else
        {
            return null;
        }
    }

    /**
     * A comparison builder to track the first comparison that breaks the tie.
     */
    private static class CompBuilder
    {
        private int result = 0;

        void compareTrueFirst(boolean a, boolean b)
        {
            if (result != 0)
            {
                return;
            }

            result = -Boolean.valueOf(a).compareTo(Boolean.valueOf(b));
        }

        <T extends Comparable<? super T>> void compare(T a, T b)
        {
            if (result != 0)
            {
                return;
            }

            result = a.compareTo(b);
        }

        <T> void compare(T a, T b, Comparator<? super T> cmp)
        {
            if (result != 0)
            {
                return;
            }

            result = cmp.compare(a, b);
        }

        void compareIndexUris(URI o1, URI o2)
        {
            if (result != 0)
            {
                return;
            }

            String i1 = index(o1), i2 = index(o2);

            if (i1 == null || i2 == null)
            {
                return;
            }

            compare(i1.length(), i2.length());
            compare(i1, i2);
        }

        int build()
        {
            return result;
        }
    }

    private static URI typeFor(Literal literal)
    {
        if (literal.getLanguage() != null)
        {
            return RDF.LANGSTRING;
        }
        else
        {
            return literal.getDatatype();
        }
    }

    static final Comparator<Literal> LITERAL_ORDER = new Comparator<Literal>()
    {
        @Override
        public int compare(Literal o1, Literal o2)
        {
            CompBuilder cb = new CompBuilder();

            cb.compare(o1.getLabel(), o2.getLabel());

            cb.compareTrueFirst(typeFor(o1) == null, typeFor(o2) == null);
            if (typeFor(o1) != null && typeFor(o2) != null)
            {
                cb.compare(typeFor(o1), typeFor(o2), RESOURCE_ORDER);
            }

            String l1 = o1.getLanguage(), l2 = o2.getLanguage();

            cb.compareTrueFirst(l1 == null, l2 == null);

            if (l1 != null && l2 != null)
            {
                cb.compare(l1, l2);
            }

            return cb.build();
        }
    };
}
