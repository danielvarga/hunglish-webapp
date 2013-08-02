package hu.mokk.hunglish.lucene.query;

import hu.mokk.hunglish.lucene.query.exception.BracketInsideQuotationException;
import hu.mokk.hunglish.lucene.query.exception.ClosingBracketButNoOpeningException;
import hu.mokk.hunglish.lucene.query.exception.ClosingBracketInsideQuatation;
import hu.mokk.hunglish.lucene.query.exception.NoClosingBracketException;
import hu.mokk.hunglish.lucene.query.exception.NoClosingQuoteException;
import hu.mokk.hunglish.lucene.query.exception.NotValidCharacterException;
import hu.mokk.hunglish.lucene.query.exception.OpeningBracketAfterOtherException;
import hu.mokk.hunglish.lucene.query.exception.QueryException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 
 * @author Peter Halacsy <peter at halacsy.com>, bpgergo@gmail.com
 *
 */
public class HunglishQuerySyntaxParser {

    public static void main(String args[]) throws Exception {
        HunglishQuerySyntaxParser qp = new HunglishQuerySyntaxParser();
        System.out.println(qp.parse("-<ablak> \"ajto ablak\"", "-<don't know>"));
    }
    public QueryStructure parse(String hu, String en) //throws QueryException //throws Exception 
    {
        QueryStructure qs = new QueryStructure();

        if(hu != null && hu.length() > 0) {
            parseHunglishSearchbox(hu, QueryPhrase.Field.HU, qs);
        }
        
        if(en != null && en.length() > 0) {
            parseHunglishSearchbox(en, QueryPhrase.Field.EN, qs);
        }
        return qs;
    }

 

    void parseHunglishSearchbox(String q, QueryPhrase.Field field,
            QueryStructure queryStructure) //throws QueryException 
    {
        String[] ps = new String[0];
		try {
			ps = primitiveParse(q);
		} catch (QueryException e) {
			if (e instanceof NoClosingQuoteException){
				String q2 = q.replaceAll("\"", "");
				try {
					ps = primitiveParse(q2);
				} catch (QueryException e1) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException(e);
			}
		}

        for (int i = 0; i < ps.length; ++i) {
            String p = ps[i];

            QueryPhrase queryPhrase = null;
			try {
				queryPhrase = parseAnnotatedPhrase(p, field);
			} catch (QueryException e) {
				throw new RuntimeException(e);
			}

            queryStructure.addPhrase(queryPhrase);
        }
    }

    private QueryPhrase parseAnnotatedPhrase(String pc, QueryPhrase.Field field)
            throws QueryException 
        {
        int mode = noparaMode;

        QueryPhrase.Qualifier qualifier;
        boolean stemmed;

        String[] terms;

        String p = pc;

        if (p.charAt(0) == '-') {
            qualifier = QueryPhrase.Qualifier.MUSTNOT;
            p = p.substring(1);
        } else if (p.charAt(0) == '+') {
            qualifier = QueryPhrase.Qualifier.MUST;
            p = p.substring(1);
        } else {
            qualifier = QueryPhrase.Qualifier.SHOULD;
        }

        char start = p.charAt(0);
        char end = p.charAt(p.length() - 1);

        if (start == '<') {
            mode = bracketMode;

            if (end != '>') {
                throw new NoClosingBracketException(p);
                //Exception("no closing bracket: " + p);
            }
            p = p.substring(1, p.length() - 1);
        } else if (start == '"') {
            mode = quoteMode;

            if (end != '"') {
                throw new NoClosingQuoteException(p); 
                //Exception("no closing quotation mark: " + p);
            }
            p = p.substring(1, p.length() - 1);
        }

        stemmed = (mode != bracketMode);

        terms = parsePhrase(p);
        return new QueryPhrase(field, terms, qualifier, stemmed);
    }

    private String[] parsePhrase(String s) throws QueryException {
        LinkedList<String> terms = new LinkedList<String>();

        String q = s + " ";

        StringBuffer collected = new StringBuffer();

        for (int i = 0; i < q.length(); ++i) {

            char c = q.charAt(i);

            if (Character.isWhitespace(c) || (c == '-') || (c == '.')) {

                if (collected.length() > 0) {

                    terms.add(collected.toString());
                    collected = new StringBuffer();
                }
            } else {

                collected.append(c);
            }

            if ((c == '"') || (c == '<') || (c == '>') || (c == '+')) {
                {
                    throw new NotValidCharacterException(Character.toString(c)) ;
                    //Exception("not valid character found: " + c);
                }
            }
        }
        return (String[]) terms.toArray(new String[0]);
    }

    final static int noparaMode = 1;

    final static int bracketMode = 2;

    final static int quoteMode = 3;

    /*
     * Tokenize the text but leaves the " - + < > signs sticked to the tokens
     */
    private String[] primitiveParse(final String qc) throws QueryException 
    {
        ArrayList<String> ps = new ArrayList<String>();

        String q = qc + " ";

        StringBuffer collected = new StringBuffer();
        int mode = noparaMode;

        for (int i = 0; i < q.length(); ++i) {
            char c = q.charAt(i);

            if ((Character.isWhitespace(c)) && (mode == noparaMode)) {
                if (collected.length() > 0) {
                    ps.add(collected.toString());
                    collected = new StringBuffer();
                }
            } else {
                collected.append(c);
            }

            if (c == '"') {
                switch (mode) {
                case noparaMode:
                    mode = quoteMode;
                    break;

                case bracketMode:
                    throw new BracketInsideQuotationException(qc); 
                    //Exception("found bracket inside quotation mark");

                case quoteMode:
                    mode = noparaMode;
                    break;
                }
            }

            if (c == '<') {
                switch (mode) {
                case noparaMode:
                    mode = bracketMode;
                    break;

                case bracketMode:
                    throw new OpeningBracketAfterOtherException(qc); 
                    //Exception("found opening bracket after other ");

                case quoteMode:
                	throw new BracketInsideQuotationException(qc); 
                	//Exception("found opening bracket inside quatation mark");

                }
            }

            if (c == '>') {
                switch (mode) {
                case noparaMode:
                    throw new ClosingBracketButNoOpeningException(qc); 
                    //Exception("found closing bracket but no opening one");

                case bracketMode:
                    mode = noparaMode;
                    break;

                case quoteMode:
                    throw new ClosingBracketInsideQuatation(qc); 
                    //Exception("found closing bracket inside quatation marks");

                }
            }
        }

        if (mode != noparaMode) {
        	//throw new Exception("no closing bracket or quotation mark");
        	if (mode == quoteMode){
        		throw new NoClosingQuoteException(qc);
        	} else if (mode == bracketMode){
        		throw new NoClosingBracketException(qc);
        	}
        }
        return (String[]) ps.toArray(new String[0]);
    }

}