package org.kafsemo.mivvi.rdf.tools;

import info.aduna.iteration.Iterations;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.RDFTripleTopologicalSorter;
import org.kafsemo.mivvi.rdf.RdfUtil.Mvi;
import org.kafsemo.mivvi.recognise.EpisodeTitleDetails;
import org.kafsemo.mivvi.recognise.impl.SimpleSeriesData;
import org.kafsemo.mivvi.recognise.impl.SimpleSeriesDetails;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Turn a simple representation ({@link SimpleSeriesData}) into an RDF document.
 */
public class SimpleToRdf
{
    public static void main(String[] args) throws IOException, URISyntaxException, RepositoryException, RDFHandlerException
    {
        SimpleSeriesDetails ssd;

        Reader r = new FileReader("../recognise/src/test/resources/org/kafsemo/mivvi/recognise/TestFilenameProcessor-named-episode-with-alternates-example.txt");

        try {
            ssd = SimpleSeriesData.loadDetails(r);
        } finally {
            r.close();
        }

        MemoryStore ms = new MemoryStore();
        SailRepository sr = new SailRepository(ms);
        sr.initialize();

        SailRepositoryConnection cn = sr.getConnection();

        Resource series = new URIImpl(ssd.id.toString());

        cn.add(series, RDF.TYPE, Mvi.Series);
        cn.add(series, DC.TITLE, new LiteralImpl(ssd.getTitle()));

        for (String desc : ssd.descriptions()) {
            cn.add(series, DC.DESCRIPTION, new LiteralImpl(desc));
        }

        for (EpisodeTitleDetails<java.net.URI> x : ssd.episodeTitlesAndDescriptions) {
            Resource episode = new URIImpl(x.getResource().toString());
            URI relation = x.isPrimary() ? DC.TITLE : DC.DESCRIPTION;

            cn.add(episode, RDF.TYPE, Mvi.Episode);
            cn.add(episode, relation, new LiteralImpl(x.getTitle()));
        }

        Map<String, Resource> episodeSeqs = new HashMap<String, Resource>();
        long nextBlankId = 1;

        for (Entry<String, java.net.URI> e : ssd.episodesByNumber.entrySet()) {
            Resource episode = new URIImpl(e.getValue().toString());

            String[] sa = e.getKey().split("x");
            if (sa.length != 2) {
                cn.add(episode, Mvi.episodeNumber, new LiteralImpl(e.getKey()));
                continue;
            }

            String season = sa[0], number = sa[1];

            Resource seq = episodeSeqs.get(season);
            if (seq == null) {
                seq = new BNodeImpl("b" + Long.toString(nextBlankId++));
                cn.add(seq, RDF.TYPE, RDF.SEQ);
                episodeSeqs.put(season, seq);
            }

            cn.add(seq, new URIImpl(RDF.NAMESPACE + "_" + number), episode);
        }

        Resource seasons = new BNodeImpl("b" + Long.toString(nextBlankId++));

        cn.add(series, Mvi.seasons, seasons);
        cn.add(seasons, RDF.TYPE, RDF.BAG);

        int i = 1;

        for (Map.Entry<String, Resource> e : episodeSeqs.entrySet()) {
            Resource season = new BNodeImpl("b" + Long.toString(nextBlankId++));

            cn.add(seasons, new URIImpl(RDF.NAMESPACE + "_" + (i++)), season);

            cn.add(season, RDF.TYPE, Mvi.Season);
            cn.add(season, Mvi.seasonNumber, new LiteralImpl(e.getKey()));
            cn.add(season, Mvi.episodes, e.getValue());
        }

        cn.setNamespace("mvi", Mivvi.URI);
        cn.setNamespace("dc", DC.NAMESPACE);

        RDFWriter writer;

//        writer = new NTriplesWriter(System.out);
        writer = new TurtleWriter(System.out);
//        writer = new RDFXMLPrettyWriter(System.out);


        RepositoryResult<Namespace> ns = cn.getNamespaces();
        while (ns.hasNext()) {
            Namespace n = ns.next();
            writer.handleNamespace(n.getPrefix(), n.getName());
        }

        writer.startRDF();

        Model model  = new LinkedHashModel();

        Iterations.addAll(cn.getStatements(null, null, null, false), model);

        for (Statement s : RDFTripleTopologicalSorter.sorted(model)) {
            writer.handleStatement(s);
        }

        writer.endRDF();
    }
}
