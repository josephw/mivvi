package org.kafsemo.mivvi.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.hamcrest.Matchers;
import org.junit.Test;

public class RDFTripleTopologicalSorterTest
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

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
            statements.add(VF.createStatement(rsrc, DC.TITLE, VF.createBNode("a")));
        }
        return statements;
    }

    @Test
    public void rootStatementsAreReturnedInSubjectOrderForUris()
    {
        List<IRI> subjects = new ArrayList<IRI>();
        for (int i = 0; i <= 9; i++)
        {
            subjects.add(VF.createIRI(String.format("http://test/%02d", i)));
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
            subjects.add(VF.createBNode(String.format("%02d", i)));
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
            subjects.add(VF.createIRI(String.format("http://test/%02d", i)));
        }

        for (int i = 0; i <= 9; i++)
        {
            subjects.add(VF.createBNode(String.format("%02d", i)));
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

        statements.add(VF.createStatement(VF.createIRI("http://test/"), DC.TITLE, VF.createLiteral("test")));

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)),
                Matchers.<Resource> contains(VF.createIRI("http://test/")));
    }

    private static final IRI POINTS_TO = SimpleValueFactory.getInstance().createIRI("http://test/#points-to");

    @Test
    public void onlyUsedAsSubjectsDoesNotReturnResourcesUsedAsObjects()
    {
        List<Statement> statements = new ArrayList<Statement>();

        statements.add(VF.createStatement(VF.createIRI("http://test/"), DC.TITLE, VF.createLiteral("test")));
        statements.add(VF.createStatement(VF.createIRI("http://example.test/"), POINTS_TO, VF.createIRI("http://test/")));

        assertThat(RDFTripleTopologicalSorter.onlyUsedAsSubjects(asModel(statements)),
                Matchers.<Resource> contains(VF.createIRI("http://example.test/")));
    }

    @Test
    public void selfReferentialStatementIsStillWritten()
    {
        List<Statement> statements = new ArrayList<Statement>();

        statements.add(VF.createStatement(VF.createIRI("http://test/"), DC.TITLE, VF.createIRI("http://test/")));

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
            Resource subject = VF.createIRI(String.format("http://test/%02d", i));

            statements.add(VF.createStatement(subject, VF.createIRI("http://test/#p1"), subject));
            statements.add(VF.createStatement(subject, VF.createIRI("http://test/#p2"), subject));
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void chainedStatementsSortTopologically()
    {
        List<Statement> statements = new ArrayList<Statement>();

        Resource subject = VF.createBNode("a");

        for (int i = 0; i <= 9; i++)
        {
            // Random so they don't sort lexically, trailing index to help visual
            // examination
            Resource object = SimpleValueFactory.getInstance().createBNode(Double.toString(Math.random()) + "-" + i);
            statements.add(SimpleValueFactory.getInstance().createStatement(subject, POINTS_TO, object));
            subject = object;
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void loopedStatementsSortTopologicallyWithLexicallyFirstStatementAsFirst()
    {
        List<Statement> statements = new ArrayList<Statement>();

        Resource subject = VF.createBNode("a");

        for (int i = 0; i <= 9; i++)
        {
            // Random so they don't sort lexically, trailing index to help visual
            // examination
            Resource object = VF.createBNode("b" + Double.toString(Math.random()) + "-" + i);
            statements.add(VF.createStatement(subject, POINTS_TO, object));
            subject = object;
        }

        /* Form a loop */
        statements.add(VF.createStatement(subject, POINTS_TO, VF.createBNode("a")));

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void rdfTypeStatementsSortFirst()
    {
        List<IRI> predicates = new ArrayList<IRI>();

        predicates.add(RDF.TYPE);
        predicates.add(DC.TITLE);

        List<IRI> list = new ArrayList<IRI>(predicates);
        Collections.reverse(list);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void statementsWithLiteralObjectsSortAheadOfStatementsWithResourceObjects()
    {
        List<Statement> statements = new ArrayList<Statement>();

        for (int i = 0; i <= 9; i++)
        {
            Resource subject = VF.createBNode(String.format("a%02d", i));

            statements.add(VF.createStatement(subject, VF.createIRI("http://test/z"), VF.createLiteral("Title 1")));
            statements.add(VF.createStatement(subject, VF.createIRI("http://test/z"), VF.createLiteral("Title 2")));
            statements.add(VF.createStatement(subject, VF.createIRI("http://test/a"), VF.createIRI("http://test/#a")));
            statements.add(VF.createStatement(subject, VF.createIRI("http://test/a"), VF.createIRI("http://test/#b")));
        }

        List<Statement> list = shuffled(statements);

        assertEquals(statements, RDFTripleTopologicalSorter.sorted(asModel(list)));
    }

    @Test
    public void predicatesSortLexicallyByUri()
    {
        List<IRI> predicates = new ArrayList<IRI>();

        for (int i = 0; i <= 9; i++)
        {
            predicates.add(VF.createIRI(String.format("http://example.com/#rel-%02d", i)));
        }

        List<IRI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesSortNumerically()
    {
        List<IRI> predicates = new ArrayList<IRI>();

        /*
         * Intentionally include one and two digit numbers so a lexical sort would be incorrect.
         */
        for (int i = 5; i <= 15; i++)
        {
            predicates.add(VF.createIRI(RDF.NAMESPACE + "_" + i));
        }

        List<IRI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesTreatsLeadingZeroesAsSignificant()
    {
        List<IRI> predicates = new ArrayList<IRI>();

        predicates.add(VF.createIRI(RDF.NAMESPACE + "_1"));
        predicates.add(VF.createIRI(RDF.NAMESPACE + "_9"));
        predicates.add(VF.createIRI(RDF.NAMESPACE + "_01"));
        predicates.add(VF.createIRI(RDF.NAMESPACE + "_99"));

        List<IRI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void listIndexPredicatesSortMutuallyWithOtherPredicates()
    {
        List<IRI> predicates = new ArrayList<IRI>();

        predicates.add(VF.createIRI("http://a.test/"));
        predicates.add(VF.createIRI(RDF.NAMESPACE + "_9"));
        predicates.add(VF.createIRI(RDF.NAMESPACE + "_10"));
        predicates.add(VF.createIRI("http://z.test/"));

        List<IRI> list = shuffled(predicates);

        assertEquals(predicates, sortedBy(list, RDFTripleTopologicalSorter.PREDICATE_ORDER));
    }

    @Test
    public void literalsAreSortedLexically()
    {
        List<Literal> literals = new ArrayList<Literal>();

        for (int i = 0; i <= 10; i++)
        {
            literals.add(VF.createLiteral(String.format("Title %02d", i)));
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
            values.add(VF.createIRI(String.format("http://test/#a%02d", i)));
        }

        for (int i = 0; i <= 10; i++)
        {
            values.add(VF.createBNode(String.format("Title %02d", i)));
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
                literals.add(VF.createLiteral(label, VF.createIRI("http://a.test/" + datatype)));
                literals.add(VF.createLiteral(label, VF.createIRI("http://a.test/" + datatype)));
            }

            /* Language-tagged strings */
            for (int lang = 0; lang < 5; lang++)
            {
                literals.add(VF.createLiteral(label, "xx-xx-" + lang));
            }

            /* Sort after language-tagged */
            for (int datatype = 0; datatype < 5; datatype++)
            {
                literals.add(VF.createLiteral(label, VF.createIRI("http://z.test/" + datatype)));
                literals.add(VF.createLiteral(label, VF.createIRI("http://z.test/" + datatype)));
            }
        }

        List<Literal> list = shuffled(literals);

        assertEquals(literals, sortedBy(list, RDFTripleTopologicalSorter.LITERAL_ORDER));
    }

    @Test
    public void literalWithTheSameLabelAndNoSpecificDatatypeCanBeCompared()
    {
        Literal a = VF.createLiteral("a");
        assertEquals(XMLSchema.STRING, a.getDatatype());

        assertEquals(0, RDFTripleTopologicalSorter.LITERAL_ORDER.compare(a, a));
    }

    @Test
    public void aLiteralWithNoSpecificDatatypeSortsDifferentlyFromOneWith()
    {
        Literal a1 = VF.createLiteral("a"),
                a2 = VF.createLiteral("a", VF.createIRI("http://test/#type"));

        assertThat(RDFTripleTopologicalSorter.LITERAL_ORDER.compare(a1, a2), Matchers.not(0));
    }
}
