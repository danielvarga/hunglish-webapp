/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 19, 2005
 *
 */

package mokk.nlp.bicorpus.index.lucene;

import java.util.HashMap;

import mokk.nlp.bicorpus.index.SearchRequest;
import mokk.nlp.bicorpus.index.query.ParseException;
import mokk.nlp.irutil.lucene.analysis.AnalyzerFactory;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;

/**
 * SearchRequest objektumbol csinal lucene szamara ertheto Queryt
 */
public class LuceneQueryBuilder {

	private HashMap sourceFilterCache;
	private AnalyzerFactory af;

	// private QueryParser qp = null;

	/**
     * 
     */
	public LuceneQueryBuilder(AnalyzerFactory af) {
		// this.qp = qp;
		this.af = af;
		sourceFilterCache = new HashMap();

	}

	public static void printBytes(byte[] array, String name) {
		for (int k = 0; k < array.length; k++) {
			System.out.println(name + "[" + k + "] = " + "0x" + byteToHex(array[k]));
		}
}

static public String byteToHex(byte b) {
	// Returns hex String representation of byte b
	char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
	return new String(array);
}

	private Query parseSearchRequest(SearchRequest request)
			throws ParseException {
		BooleanQuery result = new BooleanQuery();
		try {
//System.out.println("request.getLeftQuery():"+request.getLeftQuery());			
			String leftRequest; // = new String(request.getLeftQuery().getBytes("utf-8"));
//System.out.println("getBytes(\"utf-8\"):"+request.getLeftQuery());
//			leftRequest = new String(request.getLeftQuery().getBytes("utf-8"));  //iso-8859-2"));
//System.out.println("getBytes(\"utf-8\"):"+request.getLeftQuery());			

//System.out.println("utf8:::");
//printBytes(request.getLeftQuery().getBytes("utf-8"), request.getLeftQuery());
//System.out.println("iso:::");
//printBytes(request.getLeftQuery().getBytes("iso-8859-2"), request.getLeftQuery());

			leftRequest = request.getLeftQuery();
			//leftRequest = "idő";
			if (leftRequest != null
					&& leftRequest.length() > 0) {
				Query leftQuery = new QueryParser(Version.LUCENE_CURRENT,
						BisMapper.leftStemmedFieldName, af.getAnalyzer())
						.parse(leftRequest);
				result.add(leftQuery, Occur.SHOULD);
			}
			
		} catch (Exception e) {
			throw new ParseException("left query couldn't be parsed", e);
		}
		try {
			if (request.getRightQuery() != null
					&& request.getRightQuery().length() > 0) {
				Query rightQuery = new QueryParser(Version.LUCENE_CURRENT,
						BisMapper.rightStemmedFieldName, af.getAnalyzer())
						.parse(request.getRightQuery());
				result.add(rightQuery, Occur.SHOULD);
			}
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException("right query couldn't be parsed", e);
		}
		return result;
	}

	public Query parseRequest(SearchRequest request) throws ParseException {

		Query query = parseSearchRequest(request);// qp.parse(request.getLeftQuery(),

		if (query == null) {
			throw new ParseException("no query");
		}

		// duplicate filter
		if (request.isExcludeDuplicates()) {
			query = addSourceFilter(query, BisMapper.isDuplicateName,
					BisMapper.NO);
		}

		query = addSourceFilter(query, "source", request.getSourceId());

		return query;

	}

	private Query addSourceFilter(Query q, String fieldName, String sourceId) {
		if (sourceId == null || sourceId == ""
				|| (sourceId.equals("all") && fieldName.equals("source"))) {
			return q;
		}
		SourceFilter filter = null;
		// TODO: ha valami tobb indexben is keresni kell, akkor itt azzal kell
		// a kulcsot kepezni, mert ott mas, es mas lehet a source filter
		// bitsetsje;

		synchronized (sourceFilterCache) { // check cache
			filter = (SourceFilter) sourceFilterCache.get(sourceId);
			if (filter == null) {
				filter = new CachingSourceFilter(fieldName, sourceId);
				sourceFilterCache.put(sourceId, filter);
			}
		}

		FilteredQuery qf = new FilteredQuery(q, filter);
		return qf;
	}

}