/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;


public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    public int LIMIT_WTQ = 3;
    public int NUMBER_TERM = 50;
    public boolean OPTIMIZATION = true;

    /**
     * Store the wtq of the query
     */
    public HashMap<String, Double> wtq  = new HashMap<String, Double>();


    /**
     *  Creates a new empty Query 
     */
    public Query() {
	}
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
		StringTokenizer tok = new StringTokenizer( queryString );
        // TODO
		while ( tok.hasMoreTokens() ) {
			terms.add( tok.nextToken() );
			weights.add( new Double(1) );
		}    
	}
	
    /**
     *  Returns the number of terms
     */
    public int size() {
		return terms.size();
	}
	
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
		Query queryCopy = new Query();
		queryCopy.terms = (LinkedList<String>) terms.clone();
		queryCopy.weights = (LinkedList<Double>) weights.clone();
        queryCopy.wtq = (HashMap<String, Double>) ((HashMap<String, Double>) wtq).clone();
		return queryCopy;
	}
	
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {

        long start = System.nanoTime();

        // We change the query in qm according to the formula
        double alpha = .1;
        double beta = .9;
        int sizeDr = 0;
        HashMap<String, Double> wtqm  = new HashMap<String, Double>();
        Collections.sort(results.getList());

        // Alpha pass 
        for(Map.Entry<String, Double> e: wtq.entrySet())
        {
            wtqm.put(e.getKey(), e.getValue()*alpha);
        }


        for(boolean b: docIsRelevant)
        {
            if(b)
            {
                sizeDr++;
            }
        }

        int j = 0;
        for(PostingsEntry pe: results.getList())
        {
            if( j<10 )
            {
                if (docIsRelevant[j])
                {
                    HashMap<String, Integer> listTerms = indexer.index.directMapping.get("" + pe.getDocID());
                    wtqm = _fillWithNewTerms(wtqm, listTerms, beta, sizeDr, pe.getScore());
                }
            }
            else
            {
                break;
            }
            j++;
        }

        /* this.normalizeWtq(); */
        if(OPTIMIZATION)
        {
            wtqm = this.eliminationLowTfIdf(wtqm, NUMBER_TERM);
        }
        this.wtq = wtqm;
        /* printWtq(); */
        this.terms = rebuildTerms();
        indexer.index.search(this, Index.RANKED_QUERY, Index.TF_IDF);
        long end = System.nanoTime();
        System.out.println("OVERALL TIME: " + (end - start) / 1000000);
    }

    private HashMap<String, Double> eliminationLowTfIdf(HashMap<String, Double> w, int numberOfTerms)
    {
        ArrayList<Double> tfidf = new ArrayList<Double>(w.values());
        Collections.sort(tfidf);
        Collections.reverse(tfidf);

        double limit = tfidf.get(numberOfTerms);
        HashMap<String, Double> newW = new HashMap<String, Double>();
        for(Map.Entry<String, Double> e: w.entrySet())
        {
            if(e.getValue() >= limit)
            {
                newW.put(e.getKey(), e.getValue());
            }
        }
        return newW;
    }

    private LinkedList<String> rebuildTerms()
    {
        LinkedList<String> newTerms = new LinkedList<String>();

        for(Map.Entry<String, Double> e: wtq.entrySet())
        {
            newTerms.add(e.getKey());
        }
        return newTerms;
    }

    private HashMap<String, Double> _fillWithNewTerms(HashMap<String, Double> wtqm, HashMap<String, Integer> listTerms, double beta, int sizeDr, double scoreTerm)
    {
        for(Map.Entry<String, Integer> e: listTerms.entrySet())
        {
            String term = e.getKey();
            if(OPTIMIZATION)
            {
                if( e.getValue() > LIMIT_WTQ)
                {
                    if(wtqm.containsKey(term))
                    {
                        wtqm.put(term, wtqm.get(term) + beta / sizeDr * scoreTerm);
                    }
                    else
                    {
                        wtqm.put(term, beta / sizeDr * scoreTerm);
                    }
                }
            }
            else
            {
                if(wtqm.containsKey(term))
                {
                    wtqm.put(term, wtqm.get(term) + beta / sizeDr * scoreTerm);
                }
                else
                {
                    wtqm.put(term, beta / sizeDr * scoreTerm);
                }
            }

        }
        return wtqm;
    }

    public LinkedList<String> getTerms()
    {
        return terms;
    }

    protected void buildWtq(int indexType)
    {
        int sum = 0;
        /* System.out.println("test"); */
        if( indexType == Index.BIWORD_INDEX )
        {
            int i = 0;
            String firstPartBigram = "";
            /* System.out.println("BuildWTQ Biword"); */
            for(String term: this.getTerms())
            {
                /* System.out.println("Term: " + i + " " + term); */
                if(i == 0)
                {
                    firstPartBigram = term;
                }
                else
                {
                    String secondPartBigram = term;
                    wtq.put(firstPartBigram + "-" + secondPartBigram, 1.);
                    sum += 1;
                    firstPartBigram = secondPartBigram;
                }
                i++;
            }
        }
        else
        {
            for(String term: this.getTerms())
            {
                wtq.put(term, 1.);
                sum += 1;
            }
        }

        // Normalization
        for(Map.Entry<String, Double> entry: wtq.entrySet())
        {
            wtq.put(entry.getKey(), entry.getValue() / Math.sqrt(sum));
        }

    }

    public void printWtq()
    {
        System.out.println("Number of tokens: " + wtq.size());
        for(Map.Entry<String, Double> entry: wtq.entrySet())
        {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    public HashMap<String, Double> getWtq()
    {
        return this.wtq;
    }

    private void normalizeWtq()
    {
        double norm = 0.;

        for(Map.Entry<String, Double> e: this.wtq.entrySet())
        {
            norm += e.getValue()*e.getValue();
        }

        // Norm L2
        norm = Math.sqrt(norm);

        for(Map.Entry<String, Double> e: this.wtq.entrySet())
        {
            wtq.put(e.getKey(), e.getValue()/norm);
        }

    }

}

    
