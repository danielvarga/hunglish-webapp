/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Nov 28, 2004
 *
 */

package mokk.nlp.bicorpus.index.lucene;

import java.io.File;
import java.io.IOException;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.bicorpus.index.BiCorpusSearcher;
import mokk.nlp.bicorpus.index.SearchRequest;
import mokk.nlp.bicorpus.index.query.ParseException;
import mokk.nlp.irutil.SearchException;
import mokk.nlp.irutil.SearchResult;
import mokk.nlp.irutil.lucene.highlight.*;
import mokk.nlp.irutil.lucene.Mapper;

import mokk.nlp.jmorph.Lemmatizer;

import org.apache.avalon.fortress.util.ContextManagerConstants;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import org.apache.lucene.search.highlight.TokenSources;

/**
 * @author hp
 * 
 * Implements the BiCorpusSearcher interface using the Jakarta Lucene search
 * engine API. It reads the index created by LuceneBiCorpusIndexer.
 * 
 * @avalon.component
 * @avalon.service type=mokk.nlp.bicorpus.index.BiCorpusSearcher
 * @x-avalon.info name=lucene-searcher
 * @x-avalon.lifestyle type=singleton
 */
public class LuceneBiCorpusSearcher implements BiCorpusSearcher,

 Component, LogEnabled, Configurable, Initializable, Serviceable, Disposable,
        Contextualizable {
    private Logger logger;

    private ServiceManager manager;

    private String indexDir;

    private IndexSearcher searcher = null;

    private IndexReader indexReader = null;

    private String leftLemmatizerId;
    private String rightLemmatizerId;

    private String m_mapperId;

//    private String m_highlighterId;

    private Mapper m_mapper = null;

    private Lemmatizer leftLemmatizer;
    private Lemmatizer rightLemmatizer;

    //private Highlighter m_highlighter;

    private File contextDirectory;

    private LuceneQueryBuilder queryBuilder;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {

        if (indexReader != null) {
            try {
                indexReader.close();
                indexReader = null;
            } catch (IOException e) {
                logger.error("can't gracefuly close indexReader", e);
            }
        }

        if (leftLemmatizer != null) {
            manager.release(leftLemmatizer);
        }

        if (rightLemmatizer != null) {
            manager.release(rightLemmatizer);
        }

        if (m_mapper != null) {
            manager.release(m_mapper);
        }

        //	if(m_highlighter != null) {
        //	    manager.release(m_highlighter);
        //	}

    }

    /*
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        contextDirectory = (File) context
                .get(ContextManagerConstants.CONTEXT_DIRECTORY);
        logger.info("context directory:" + contextDirectory);

    }

    /**
     * @avalon.dependency type="mokk.nlp.jmorph.Lemmatizer"
     * @avalon.dependency type="mokk.nlp.irutil.lucene.Mapper"
     * 
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        indexDir = config.getChild("index-dir").getValue();
        if (indexDir == null) {
            throw new ConfigurationException("no index-dir specified");
        }
        logger.info("index searched in directory: " + indexDir);

        leftLemmatizerId = config.getChild("left-lemmatizer").getValue();
        logger.info("using left lemmatizer: " + leftLemmatizerId);

        rightLemmatizerId = config.getChild("right-lemmatizer").getValue();
        logger.info("using right lemmatizer: " + rightLemmatizerId);

        m_mapperId = config.getChild("mapper").getValue();
        logger.info("using mapper to recontstruct bisentence objects: "
                + m_mapperId);



    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {

        leftLemmatizer = (Lemmatizer) manager.lookup(Lemmatizer.ROLE + "/" +leftLemmatizerId);
        rightLemmatizer = (Lemmatizer) manager.lookup(Lemmatizer.ROLE + "/" +rightLemmatizerId);
        m_mapper = (Mapper) manager.lookup(Mapper.ROLE + "/" + m_mapperId);

        indexReader = IndexReader.open(getContextualizedPath(indexDir));
        searcher = new IndexSearcher(indexReader);
        ;

        logger.info("indexReader opened in:" + indexDir);

        //	m_highlighter = (Highlighter) manager.lookup(Highlighter.ROLE + "/" +
        // m_highlighterId);
        queryBuilder = new LuceneQueryBuilder(new mokk.nlp.bicorpus.index.lucene.QueryParser(leftLemmatizer, rightLemmatizer));

    }

    private String getContextualizedPath(String file) {
        if (file.startsWith("/")) {
            return file;
        }

        return contextDirectory.getAbsolutePath() + "/" + file;
    }

    public SearchResult search(SearchRequest request) throws SearchException {

        Query query;
        try {
            query = queryBuilder.parseRequest(request);
        } catch (ParseException e1) {
            throw new SearchException(e1.getMessage(), e1);
        }
        SearchResult result = new SearchResult();

        Hits h = null;
        try {
            h = searcher.search(query);

        } catch (IOException ioe) {
            throw new SearchException(ioe);
        }
        int l = h.length();

        if (logger.isDebugEnabled()) {
            logger.debug("query = " + query + " found = " + l);
        }

        result.setTotalCount(l);
        result.setStartOffset(request.getStartOffset());
        int end = request.getStartOffset() + request.getMaxResults();
        if (end > l) {
            end = l;
        }
        result.setEndOffset(end);

        // kiemeleshez
        String queryTerms[] = SimpleQueryTermExtractor.getTerms(query);
        Highlighter highlighter = new Highlighter("b", null);
        
        // kiolvassuk az aktualis lapon levo dokumentumokat, csinalunk beloluk
        // bimondatot, plusz adunk hozza highlightingot
        for (int i = request.getStartOffset(); i < end; i++) {
            Document d;

            try {
                d = h.doc(i);
            } catch (IOException ioe) {

                throw new SearchException(ioe);
            }

            BiSentence bis = (BiSentence) m_mapper.toResource(d);
            if (request.isLeftQuery()) {

                try {
                    TokenStream leftTokens = TokenSources.getTokenStream(
                            indexReader, h.id(i), "left_stemmed");

    
                    bis.setLeftSentence(highlighter.highlight(bis.getLeftSentence(), leftTokens, queryTerms));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (request.isRightQuery()) {
                TokenStream rightTokens;
                try {
                    rightTokens = TokenSources.getTokenStream(indexReader, h.id(i), "right_stemmed");
                    bis.setRightSentence(highlighter.highlight(bis.getRightSentence(), rightTokens, queryTerms));

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            result.addToHits(bis);
        }

        return result;

    }

    public void close() throws IOException {
        searcher.close();
    }
}
