package org.kafsemo.mivvi.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;

public class RDFTripleTopologicalSorterTest
{

    private static Model asModel(Collection<? extends Statement> statements)
    {
        return new LinkedHashModel(statements);
    }

    @Test
    public void emptyListIsLeftUntouched()
    {
        List<Statement> sorted = RDFTripleTopologicalSorter.sorted(asModel(Collections.<Statement> emptyList()));
        assertEquals(Collections.emptyList(), sorted);
    }

    private static <T> List<T> shuffled(List<T> original)
    {
        List<T> toShuffle = new ArrayList<T>(original);
        Collections.shuffle(toShuffle);
        return toShuffle;
    }

    private static <T> List<T> sortedBy(List<T> original, Comparator<? super T> comparator)
    {
        List<T> toSort = new ArrayList<T>(original);
        Collections.sort(toSort, comparator);
        return toSort;
    }

    static List<Statement> statementsWithSubjects(List<? extends Resource> subjects)
    {
        List<Statement> statements = new ArrayList<Statement>();
        for (Resource rsrc : subjects)
        {
            statements.add(new StatementImpl(rsrc, DC.TITLE, new BNodeImpl("a")));
        }
        return statements;
    }

    @Test
    public void rootStatementsAreReturnedInSubjectOrderForUris()
    {
        List<URI> subjects = new ArrayList<URI>();
        for (int i = 0; i <= 9; i++)
        {
            subjects.add(new URIImpl(String.format("http://test/%02d", i)));
        }

        List<Statement> statements = statementsWithSubjects(subjects);

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void rootStatementsAreReturnedInSubjectOrderForBlankNodes()
    {
        List<BNode> subjects = new ArrayList<BNode>();
        for (int i = 0; i <= 9; i++)
        {
            subjects.add(new BNodeImpl(String.format("%02d", i)));
        }

        List<Statement> statements = statementsWithSubjects(subjects);

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void blankNodesSortAfterUrisForSubjects()
    {
        List<Resource> subjects = new ArrayList<Resource>();

        for (int i = 0; i <= 9; i++)
        {
            subjects.add(new URIImpl(String.format("http://test/%02d", i)));
        }

        for (int i = 0; i <= 9; i++)
        {
            subjects.add(new BNodeImpl(String.format("%02d", i)));
        }

        List<Resource> list = shuffled(subjects);
        Collections.sort(list, RDFTripleTopologicalSorter.RESOURCE_ORDER);

        assertEquals(subjects, list);
    }

    @Test
    public void onlyUsedAsSubjectsIdentifiesResourcesNeverUsedAsObjects()
    {
        List<Statement> statements = new ArrayList<Statement>();

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)), Matchers.emptyIterable());

        statements.add(new StatementImpl(new URIImpl("http://test/"), DC.TITLE, new LiteralImpl("test")));

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)),
                Matchers.<Resource> contains(new URIImpl("http://test/")));
    }

    private static final URI POINTS_TO = new URIImpl("http://test/#points-to");

    @Test
    public void onlyUsedAsSubjectsDoesNotReturnResourcesUsedAsObjects()
    {
        List<Statement> statements = new ArrayList<Statement>();

        statements.add(new StatementImpl(new URIImpl("http://test/"), DC.TITLE, new LiteralImpl("test")));
        statements.add(new StatementImpl(new URIImpl("http://example.test/"), POINTS_TO, new URIImpl("http://test/")));

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)),
                Matchers.<Resource> contains(new URIImpl("http://example.test/")));
    }

    @Test
    public void selfReferentialStatementIsStillWritten()
    {
        List<Statement> statements = new ArrayList<Statement>();

        statements.add(new StatementImpl(new URIImpl("http://test/"), DC.TITLE, new URIImpl("http://test/")));

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)), Matchers.emptyIterable());

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void selfReferentialStatementsAreSortedBySubjectThenPredicate()
    {
        List<Statement> statements = new ArrayList<Statement>();

        for (int i = 0; i < 10; i++)
        {
            Resource subject = new URIImpl(String.format("http://test/%02d", i));

            statements.add(new StatementImpl(subject, new URIImpl("http://test/#p1"), subject));
            statements.add(new StatementImpl(subject, new URIImpl("http://test/#p2"), subject));
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void chainedStatementsSortTopologically()
    {
        List<Statement> statements = new ArrayList<Statement>();

        Resource subject = new BNodeImpl("a");

        for (int i = 0; i <= 9; i++)
        {
            // Random so they don't sort lexically, trailing index to help visual
            // examination
            Resource object = new BNodeImpl(Double.toString(Math.random()) + "-" + i);
            statements.add(new StatementImpl(subject, POINTS_TO, object));
            subject = object;
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void loopedStatementsSortTopologicallyWithLexicallyFirstStatementAsFirst()
    {
        List<Statement> statements = new ArrayList<Statement>();

        Resource subject = new BNodeImpl("a");

        for (int i = 0; i <= 9; i++)
        {
            // Random so they don't sort lexically, trailing index to help visual
            // examination
            Resource object = new BNodeImpl("b" + Double.toString(Math.random()) + "-" + i);
            statements.add(new StatementImpl(subject, POINTS_TO, object));
            subject = object;
        }

        /* Form a loop */
        statements.add(new StatementImpl(subject, POINTS_TO, new BNodeImpl("a")));

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void rdfTypeStatementsSortFirst()
    {
        List<URI> predicates = new ArrayList<URI>();

        predicates.add(RDF.TYPE);
        predicates.add(DC.TITLE);

        List<URI> list = new ArrayList<URI>(predicates);
        Collections.reverse(list);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void statementsWithLiteralObjectsSortAheadOfStatementsWithResourceObjects()
    {
        List<Statement> statements = new ArrayList<Statement>();

        for (int i = 0; i <= 9; i++)
        {
            Resource subject = new BNodeImpl(String.format("a%02d", i));

            statements.add(new StatementImpl(subject, new URIImpl("http://test/z"), new LiteralImpl("Title 1")));
            statements.add(new StatementImpl(subject, new URIImpl("http://test/z"), new LiteralImpl("Title 2")));
            statements.add(new StatementImpl(subject, new URIImpl("http://test/a"), new URIImpl("http://test/#a")));
            statements.add(new StatementImpl(subject, new URIImpl("http://test/a"), new URIImpl("http://test/#b")));
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void predicatesSortLexicallyByUri()
    {
        List<URI> predicates = new ArrayList<URI>();

        for (int i = 0; i <= 9; i++)
        {
            predicates.add(new URIImpl(String.format("http://example.com/#rel-%02d", i)));
        }

        List<URI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesSortNumerically()
    {
        List<URI> predicates = new ArrayList<URI>();

        /*
         * Intentionally include one and two digit numbers so a lexical sort would be incorrect.
         */
        for (int i = 5; i <= 15; i++)
        {
            predicates.add(new URIImpl(RDF.NAMESPACE + "_" + i));
        }

        List<URI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesTreatsLeadingZeroesAsSignificant()
    {
        List<URI> predicates = new ArrayList<URI>();

        predicates.add(new URIImpl(RDF.NAMESPACE + "_1"));
        predicates.add(new URIImpl(RDF.NAMESPACE + "_9"));
        predicates.add(new URIImpl(RDF.NAMESPACE + "_01"));
        predicates.add(new URIImpl(RDF.NAMESPACE + "_99"));

        List<URI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesSortMutuallyWithOtherPredicates()
    {
        List<URI> predicates = new ArrayList<URI>();

        predicates.add(new URIImpl("http://a.test/"));
        predicates.add(new URIImpl(RDF.NAMESPACE + "_9"));
        predicates.add(new URIImpl(RDF.NAMESPACE + "_10"));
        predicates.add(new URIImpl("http://z.test/"));

        List<URI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void literalsAreSortedLexically()
    {
        List<Literal> literals = new ArrayList<Literal>();

        for (int i = 0; i <= 10; i++)
        {
            literals.add(new LiteralImpl(String.format("Title %02d", i)));
        }

        List<Literal> list = shuffled(literals);

        assertEquals(literals, sortedBy(list, RDFTripleTopologicalSorter.LITERAL_ORDER));
    }

    @Test
    public void resourcesAsObjectsAreSorted()
    {
        List<Value> values = new ArrayList<Value>();

        for (int i = 0; i <= 10; i++)
        {
            values.add(new URIImpl(String.format("http://test/#a%02d", i)));
        }

        for (int i = 0; i <= 10; i++)
        {
            values.add(new BNodeImpl(String.format("Title %02d", i)));
        }

        List<Value> list = shuffled(values);

        assertEquals(values, sortedBy(list, RDFTripleTopologicalSorter.OBJECT_ORDER));
    }

    @Test
    public void literalSortingConsidersLanguageAndDatatype()
    {
        List<Literal> literals = new ArrayList<Literal>();

        for (int lab = 0; lab < 5; lab++)
        {
            String label = String.format("label %02d", lab);

            /* Sort ahead of language-tagged */
            for (int datatype = 0; datatype < 5; datatype++)
            {
                literals.add(new LiteralImpl(label, new URIImpl("http://a.test/" + datatype)));
                literals.add(new LiteralImpl(label, new URIImpl("http://a.test/" + datatype)));
            }

            /* Language-tagged strings */
            for (int lang = 0; lang < 5; lang++)
            {
                literals.add(new LiteralImpl(label, "xx-xx-" + lang));
            }

            /* Sort after language-tagged */
            for (int datatype = 0; datatype < 5; datatype++)
            {
                literals.add(new LiteralImpl(label, new URIImpl("http://z.test/" + datatype)));
                literals.add(new LiteralImpl(label, new URIImpl("http://z.test/" + datatype)));
            }
        }

        List<Literal> list = shuffled(literals);

        assertEquals(literals, sortedBy(list, RDFTripleTopologicalSorter.LITERAL_ORDER));
    }
}
