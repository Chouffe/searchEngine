/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;


public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     * Store the wtq of the query
     */
    private HashMap<String, Double> wtq  = new HashMap<String, Double>();


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
		while ( tok.hasMoreTokens() ) {
			terms.add( tok.nextToken() );
			weights.add( new Double(1) );
		}    
        buildWtq();
        printWtq();
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
		return queryCopy;
	}
	
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {

        // We change the query in qm according to the formula
        double alpha = .1;
        double beta = .9;


        int i = 0;
        for(boolean b: docIsRelevant)
        {
            if(b)
            {
                System.out.println(i);
            }
            i++;
        }

        System.out.println(results);
        /* for(PostingsEntry pe: PostingsList.getList()) */
        /* { */
        /*     PostingsEntry toAdd = new PostingsEntry(pe); */
        /* } */
		// results contain the ranked list from the current search
        // 
		// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
		
		//
		//  YOUR CODE HERE
		//
    }

    public LinkedList<String> getTerms()
    {
        return terms;
    }

    protected void buildWtq()
    {
        int sum = 0;
        for(String term: this.getTerms())
        {
            wtq.put(term, 1.);
            sum += 1;
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

}

    
