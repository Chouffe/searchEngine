/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;
import java.util.ListIterator;
import java.lang.Math;
import java.util.Collections;
import java.io.*;
import java.util.regex.*;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /**
     * Mapping of the docID but in reversed mapping
     */
    private HashMap<String, String> docIDsReversed = new HashMap<String, String>();

    /**
     * Contains the pageranks
     */
    private HashMap<Integer, Double> rankingScores = new HashMap<Integer, Double>();

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    /** The tf(term, document) as a double HashMap 
     * term (String) -- HashMap<DocID, tf>
     */
    private HashMap<String, HashMap<Integer, Double>> tf = new HashMap<String, HashMap<Integer, Double>>();

    /** The df(term, document) as a double HashMap 
     * term (String) -- HashMap<DocID, df>
     */
    private HashMap<String, HashMap<Integer, Double>> df = new HashMap<String, HashMap<Integer, Double>>();

    /** 
     * The idf(term, value) as a HashMap
     */
    private HashMap<String, Double> idf = new HashMap<String, Double>();

    /** The tfIdf(term, document) as a double HashMap 
     * term (String) -- HashMap<DocID, df>
     */
    private HashMap<String, HashMap<Integer, Double>> tfIdf = new HashMap<String, HashMap<Integer, Double>>();

    /**
     * Store the wtq of the query
     */
    /* private HashMap<String, Double> wtq  = new HashMap<String, Double>(); */

    public void buildDocIDsReversed()
    {
        String regex = ".*\\/(\\d+)\\.txt$";
        Pattern pattern = Pattern.compile(regex);
        for(Map.Entry<String, String> e: docIDs.entrySet())
        {
            Matcher matcher = pattern.matcher(e.getValue());
            if(matcher.matches())
            {
                String idDoc = matcher.group(1);
                docIDsReversed.put(idDoc, e.getKey());
            }
        }

        /* printDocIDsReversed(); */
    }

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {

        // Compute tf while inserting new tokens in the index
        updateTf(token, docID);
        updateDf(token, docID);

        // Construct direct Mapping
        this._insertIntoDirectMapping(token, docID);

        if( index.containsKey(token) )
        {
            PostingsList p = index.get(token);
            // If the PostingsList does not contain the docID
            if( !p.contains( docID ) )
            {
                p.add(docID, offset);

            }
            // If the PostingsList contains the docID
            // We just need to add the offset into the indexOfList
            else
            {
                p.addIndexOf(docID, offset);
            }
        }
        // If the token is not in the index
        else
        {
            PostingsList p = new PostingsList();
            p.add(docID, offset);
            index.put(token, p);
        }
    }

    private void _insertIntoDirectMapping( String token, int docID )
    {
        /* HashSet<String> tokenList = new HashSet<String>(); */
        HashMap<String, Integer> tokenList = new HashMap<String, Integer>();

        if( directMapping.containsKey("" + docID) )
        {
            tokenList = directMapping.get("" + docID);
        }
        /* else */
        /* { */
        /*     tokenList = new ArrayList<String>(); */
        /* } */

        if(tokenList.containsKey(token))
        {
            tokenList.put(token, tokenList.get(token) + 1);
        }
        else
        {
            tokenList.put(token, 1);
        }


        /* tokenList.add(token); */
        /* System.out.println(tokenList); */
        directMapping.put("" + docID, tokenList);
        /* directMapping.put("" + docID, new ArrayList<String>()); */

    }

    public void updateTf(String token, int docID)
    {
        if( tf.containsKey(token) )
        {
            HashMap<Integer, Double> docsHashMap = tf.get(token);
            if( docsHashMap.containsKey(docID))
            {
                docsHashMap.put(docID, docsHashMap.get(docID) + 1.);
            }
            else
            {
                docsHashMap.put(docID, 1.);
            }
        }
        else
        {
            // Initialize the tf for the docID
            HashMap<Integer, Double> docsHashMap = new HashMap<Integer, Double>();
            docsHashMap.put( docID, 1.);
            tf.put(token, docsHashMap);
        }
    }

    public void updateDf(String token, int docID)
    {
        if( df.containsKey(token) )
        {
            HashMap<Integer, Double> docsHashMap = df.get(token);
            if( !docsHashMap.containsKey(docID))
            {
                docsHashMap.put(docID, 1.);
            }
        }
        else
        {
            // Initialize the df for the docID
            HashMap<Integer, Double> docsHashMap = new HashMap<Integer, Double>();
            docsHashMap.put( docID, 1.);
            df.put(token, docsHashMap);
        }

    }

    /**
     * Print the tf HashMap in the stout
     */
    public void printTf()
    {
        for(Map.Entry<String, HashMap<Integer, Double>> tft : tf.entrySet())
        {
            System.out.println("Term: " + tft.getKey());

            for(Map.Entry<Integer, Double> tftd: tft.getValue().entrySet())
            {
                System.out.print(tftd.getValue() + ",");
                System.out.print(docIDs.get( "" + tftd.getKey()) + " ");
            }
            System.out.println("");
        }
    }

    public void updateTfToW()
    {
        // Can try with different types of frequencies
        int N = index.size();
        HashMap<Integer, Double> maxFt = new HashMap<Integer, Double>();

        // Fill the maxFt hashMap
        /* for(Map.Entry<String, HashMap<Integer, Double>> tft : tf.entrySet()) */
        /* { */
        /*     String t = tft.getKey(); */
        /*     for(Map.Entry<Integer, Double> tftd: tft.getValue().entrySet()) */
        /*     {  */
        /*         int d = tftd.getKey(); */
        /*         if(!maxFt.containsKey(d)) */
        /*         { */
        /*             maxFt.put(d, 0.); */
        /*         } */

        /*         if(maxFt.get(d) < tftd.getValue()) */
        /*         { */
        /*             maxFt.put(d, tftd.getValue()); */
        /*         } */
        /*     } */
        /* } */

        for(Map.Entry<String, HashMap<Integer, Double>> tft : tf.entrySet())
        {
            for(Map.Entry<Integer, Double> tftd: tft.getValue().entrySet())
            { 
                Integer docLength = docLengths.get("" + tftd.getKey());
                /* tft.getValue().put(tftd.getKey(), 1. + Math.log(tftd.getValue())/Math.log(10)); */
                /* tft.getValue().put(tftd.getKey(), 1. + Math.log(100*tftd.getValue()/docLength)/Math.log(10)); */
                tft.getValue().put(tftd.getKey(), tftd.getValue()/docLength.floatValue());
                /* tft.getValue().put(tftd.getKey(), tftd.getValue()/maxFt.get(tftd.getKey())); */
            }
        }
    }

    public void computeIdf()
    {
        /* System.out.println(index.size()); */
        int N = docIDs.size();

        for(Map.Entry<String, HashMap<Integer, Double>> dft : df.entrySet())
        {
                double dfTerm = (double) dft.getValue().size();
                double idfTerm = Math.log(N/dfTerm)/Math.log(10);
                /* double idfTerm = Math.log(N/dfTerm); */
                idf.put(dft.getKey(), idfTerm);
        }

        /* for(Map.Entry<String, PostingsList> p: index.entrySet()) */
        /* { */
        /*     String term = p.getKey(); */
        /*     HashMap<Integer, Double> docsHashMap = new HashMap<Integer, Double>(); */

        /*     for(PostingsEntry pe: p.getValue().getList()) */
        /*     { */

        /*         double dfTerm = (double) pe.getIndexOfList().size(); */
        /*         double idfTerm = Math.log(N/dfTerm)/Math.log(10); */
        /*         docsHashMap.put(pe.getDocID(), idfTerm); */
        /*     } */

        /*     df.put(term, docsHashMap); */
        /* } */
        /* printDocLengths(); */
    }


    public void printDocLengths()
    {
        for(Map.Entry<String, Integer> entry : docLengths.entrySet())
        {
            System.out.println("" + docIDs.get( "" + entry.getKey())  + " -> " + entry.getValue());
        }
    }

    /**
     * Print the idf HashMap in the stout
     */
    public void printIdf()
    {
        for(Map.Entry<String, Double> idft: idf.entrySet())
        {
            System.out.print("Term: " + idft.getKey());
            System.out.print(idft.getValue());
            System.out.println();
        }
    }

    public void printTfIdf()
    {
        for(Map.Entry<String, HashMap<Integer, Double>> tfIdft : tfIdf.entrySet())
        {
            System.out.println("Term: " + tfIdft.getKey());
            for(Map.Entry<Integer, Double> tfIdftd: tfIdft.getValue().entrySet())
            {
                System.out.print(tfIdftd.getValue() + "," + docIDs.get("" + tfIdftd.getKey()) + " ");
            }

            System.out.println("");
        }
    }

    public void buildTfIdf()
    {
        // Computing tf-idf = tf(t, d) * idf(t, d)
        for(Map.Entry<String, HashMap<Integer, Double>> tft : tf.entrySet())
        {
            String t = tft.getKey();
            HashMap<Integer, Double> docsHashMap = new HashMap<Integer, Double>();

            for(Map.Entry<Integer, Double> tftd: tft.getValue().entrySet())
            {
                int d = tftd.getKey();
                docsHashMap.put(d, tftd.getValue() * idf.get(t) );
            }

            tfIdf.put(t, docsHashMap);
        }

        // Normalization
        for(Map.Entry<String, HashMap<Integer, Double>> tfIdft : tfIdf.entrySet())
        {
            double norm = 0.;

            for(Map.Entry<Integer, Double> tfIdftd: tfIdft.getValue().entrySet())
            {
                norm += tfIdftd.getValue()*tfIdftd.getValue();
            }

            // Norm L2
            norm = Math.sqrt(norm);

            for(Map.Entry<Integer, Double> tfIdftd: tfIdft.getValue().entrySet())
            {
                int d = tfIdftd.getKey();
                int docLength = docLengths.get("" + d);
                /* tfIdft.getValue().put(d, tfIdftd.getValue() / docLength); */
                tfIdft.getValue().put(d, tfIdftd.getValue() / norm);
            }
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        if( index.containsKey(token) )
        {
            PostingsList p = index.get(token);
            /* System.out.println(p); */
            /* System.out.println("Number of results: " + p.size()); */
            return p;
        }
        else
        {
            System.out.println("Fail");
        }
        /* printIndex(); */
        return null;
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {
        // 
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        /* printIndex(); */

        /* System.out.println("Search processing"); */
        PostingsList result = new PostingsList();

        // Phrase queries
        if ( queryType == Index.PHRASE_QUERY )
        {

            int i = 0;
            for(String term: query.getTerms())
            {
                /* System.out.println(term); */
                if ( i == 0 )
                {
                    result = getPostings(term);
                }
                else
                {
                    PostingsList p2 = getPostings(term);
                    result = positionalIntersect(result, p2);
                }


                /* System.out.println(result.toStringMin()); */

                i++;
            }
        }
        // Ranked queries
        else if ( queryType == Index.RANKED_QUERY )
        {
            /* System.out.println("Ranked Query"); */

            result = null;
            query.buildWtq(Index.HASHED_INDEX);

            int i = 0;
            for(String term: query.getTerms())
            {
                if( i == 0 )
                {
                    result = getPostings(term);
                    Collections.sort(result.getList(), new DocIDComparator());
                    /* System.out.println(result.toStringMin()); */
                }
                else
                {
                    /* result.getList().addAll(getPostings(term).getList()); */
                    /* result = positionalIntersect(result, getPostings(term)); */
                    PostingsList p2 = getPostings(term);
                    Collections.sort(p2.getList(), new DocIDComparator());
                    result = union(result, p2);
                    /* System.out.println(result.toStringMin()); */
                }

                i++;
            }
            /* printWtq(); */

            if( rankingType == Index.TF_IDF)
            {
                result = scorePostingsList(cosineScore(query), result);
            }
            else if(rankingType == Index.PAGERANK)
            {
                result = scorePostingsList(pageRankScoring(query), result);
                /* System.out.println("Pagerank"); */
            }
            // Combination
            else
            {
                result = scorePostingsList(combinationScoring(query), result);
                /* System.out.println("Combination"); */

            }

            Collections.sort(result.getList());

            // Print out
            /* for( PostingsEntry pe: result.getList()) */
            /* { */
            /*     System.out.print(", " + docIDs.get("" + pe.getDocID()) ); */
            /* } */

        }
        // Intersection queries
        else
        {

            int i = 0;
            for(String term: query.getTerms())
            {
                /* System.out.println(term); */
                if ( i == 0 )
                {
                    result = getPostings(term);
                }
                else
                {
                    result = intersect(result, getPostings(term));
                }
                i++;
            }
        }

        if( result.getList().size() > 0)
        {
            return result;
        }
        else
        {
            return null;
        }
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }


    // TODO
    protected PostingsList union(PostingsList p1, PostingsList p2)
    {
        PostingsList answer = new PostingsList();
        ListIterator it1 = p1.getList().listIterator();
        ListIterator it2 = p2.getList().listIterator();

        while(it1.hasNext() || it2.hasNext())
        {
            /* it1.next(); */
            /* it2.next(); */
            int UNDEFINED = 1000000;
            int docID1 = UNDEFINED;
            int docID2 = UNDEFINED;

            if(it1.hasNext())
            {
                docID1 = ((PostingsEntry)it1.next()).getDocID(); 
            }
            if(it2.hasNext())
            {
                docID2 = ((PostingsEntry)it2.next()).getDocID(); 
            }

            if(docID1 == UNDEFINED)
            {
                answer.add(docID2);
            }
            else if(docID2 == UNDEFINED)
            {
                answer.add(docID1);
            }
            else
            {
                /* if(!answer.getList().contains(docID1)) */
                /* { */
                /*     answer.add(docID1); */
                /* } */
                /* if(!answer.getList().contains(docID2)) */
                /* { */
                /*     answer.add(docID1); */
                /* } */
                if(docID1 < docID2)
                {
                    answer.add(docID1);
                    it2.previous();
                }
                else if(docID2 < docID1)
                {
                    answer.add(docID2);
                    it1.previous();
                }
                else
                {
                    /* System.out.println("Skip one"); */
                    answer.add(docID1);
                }
            }
        }

        return answer;

    }


    protected PostingsList scorePostingsList(HashMap<Integer, Double> scores, PostingsList p)
    {
        for(PostingsEntry pe: p.getList())
        {
            pe.score = scores.get(pe.docID);
        }

        return p;
    }

    /**
     * Compute the scores according to the pagerank only
     */
    protected HashMap<Integer, Double> pageRankScoring(Query query)
    {
        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();

        for(String t: query.getTerms())
        {
            // Calculate wtq and fetch postings list for t
            query.buildWtq(Index.HASHED_INDEX);
            PostingsList p = getPostings(t);
            for(PostingsEntry pe: p.getList())
            {
                // Scalar Product
                int d = pe.getDocID();

                if(scores.containsKey(d))
                {
                    scores.put(d, rankingScores.get(d));
                }
                else
                {
                    scores.put(d, rankingScores.get(d));
                }
            }
        }

        return scores;
    }

    /**
     * Computes a score given a pagerank and a tfidf
     * alpha = MAXTFIDF/MAXPAGERANK
     */
    protected double heuristicCombination(double tfidf, double pagerank, double alpha)
    {
        // A PageRank is more important than a popular page
        double coeff = 1.1; // 10% More important
        return tfidf + alpha*pagerank*coeff;
    }


    /**
     * Computes the scores according to the tfidf and the pageranks
     */
    protected HashMap<Integer, Double> combinationScoring(Query query)
    {
        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();

        for(String t: query.getTerms())
        {
            // Calculate wtq and fetch postings list for t
            query.buildWtq(Index.HASHED_INDEX);
            PostingsList p = getPostings(t);
            for(PostingsEntry pe: p.getList())
            {
                // Scalar Product
                int d = pe.getDocID();

                if(scores.containsKey(d))
                {
                    scores.put(d, scores.get(d) + query.getWtq().get(t) * tfIdf.get(t).get(d));
                }
                else
                {
                    scores.put(d, query.getWtq().get(t) * tfIdf.get(t).get(d));
                }
            }
        }

        // We find the normalization factor between tfidf and pageranks
        double maxTFIDF = 0.;
        double maxPAGERANK = 0.;
        for(Map.Entry<Integer, Double> entry: scores.entrySet())
        {
            if(entry.getValue() > maxTFIDF)
            {
                maxTFIDF = entry.getValue();
            }
        }

        for(Map.Entry<Integer, Double> entry: rankingScores.entrySet())
        {
            if(entry.getValue() > maxPAGERANK)
            {
                maxPAGERANK = entry.getValue();
            }
        }


        for(Map.Entry<Integer, Double> entry: scores.entrySet())
        {
            double combinatedScore = 0.;
            combinatedScore = heuristicCombination(entry.getValue(), rankingScores.get(entry.getKey()), maxTFIDF/maxPAGERANK);
            scores.put(entry.getKey(), combinatedScore);
        }

        return scores;
    }

    /**
     * Computes the scores according to the tfidf only
     */
    protected HashMap<Integer, Double> cosineScore(Query query)
    {
        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();

        for(String t: query.getTerms())
        {
            // Calculate wtq and fetch postings list for t
            query.buildWtq(Index.HASHED_INDEX);
            PostingsList p = getPostings(t);
            for(PostingsEntry pe: p.getList())
            {
                // Scalar Product
                int d = pe.getDocID();

                if(scores.containsKey(d))
                {
                    scores.put(d, scores.get(d) + query.getWtq().get(t) * tfIdf.get(t).get(d));
                }
                else
                {
                    scores.put(d, query.getWtq().get(t) * tfIdf.get(t).get(d));
                }
            }
        }
        return scores;
    }
    /**
     * Intersect two posting lists given a asc sorted order
     * cf algorithm p11 book
     */
    protected PostingsList intersect(PostingsList p1, PostingsList p2)
    {
        PostingsList answer = new PostingsList();
        ListIterator it1 = p1.getList().listIterator();
        ListIterator it2 = p2.getList().listIterator();

        while(it1.hasNext() && it2.hasNext())
        {
            int docID1 = ((PostingsEntry)it1.next()).getDocID(); 
            int docID2 = ((PostingsEntry)it2.next()).getDocID(); 

            if(docID1 == docID2)
            {
                answer.add(docID1);
            }
            else if(docID1 < docID2)
            {
                it2.previous();
            }
            else
            {
                it1.previous();
            }
        }

        return answer;
    }

    /**
     * Phrase queries intersect
     */
    public PostingsList positionalIntersect(PostingsList p1, PostingsList p2)
    {
        PostingsList answer = new PostingsList();
        ListIterator it1 = p1.getList().listIterator();
        ListIterator it2 = p2.getList().listIterator();

        /* System.out.println("P1: " + p1); */
        /* System.out.println("P2: " + p2); */


        while(it1.hasNext() && it2.hasNext())
        {
            PostingsEntry pe1 = (PostingsEntry)it1.next();
            PostingsEntry pe2 = (PostingsEntry)it2.next();

            int docID1 = pe1.getDocID(); 
            int docID2 = pe2.getDocID();

            if(docID1 == docID2)
            {
                LinkedList<Integer> pp1 = pe1.getIndexOfList();
                LinkedList<Integer> pp2 = pe2.getIndexOfList();

                /* System.out.println("DocID -> " + docID1); */
                /* System.out.println("1 -> " + pp1); */
                /* System.out.println("2 -> " + pp2); */

                ArrayList<Integer> l = new ArrayList<Integer>();

                ListIterator itpp1 = pp1.listIterator();

                while( itpp1.hasNext() )
                {
                    ListIterator itpp2 = pp2.listIterator();
                    int position1 = ((Integer) itpp1.next()).intValue();
                    /* System.out.println("Postion 1: " + position1); */

                    while( itpp2.hasNext() )
                    {
                        int position2 = ((Integer) itpp2.next()).intValue();
                        /* System.out.println("Postion 2: " + position2); */


                        if( Math.abs( position1 - position2 ) <= 1 )
                        {
                            /* System.out.println("" + Integer.toString(position1) + " - " + Integer.toString(position2)); */
                            l.add(position2);
                        }
                        else if ( position2 > position1 )
                        {
                            break;
                        }
                    }
                    // Cleanup l
                    /* System.out.println("L: " + l); */
                    ArrayList<Integer> cleanedL = new ArrayList<Integer>();

                    for( int pos: l )
                    {
                        if(Math.abs(pos - position1) <= 1)
                        {
                            cleanedL.add(pos);
                        }
                    }

                    int i = 0;
                    for( int pos: cleanedL )
                    {
                        if( i == 0 )
                        {
                            PostingsList p = new PostingsList();
                            answer.add(pe1.getDocID(), pos);
                        }
                        else
                        {
                            answer.addIndexOf(pe1.getDocID(), pos);
                        }

                        i++;
                    }
                }
            }
            else if (docID1 < docID2)
            {
                it2.previous();
            }
            else
            {
                it1.previous();
            }
        }
        return answer;
    }

    /**
     * Print the whole Index out
     */
    public void printIndex()
    {
        /* System.out.println(index.size()); */
        for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
            System.out.println(entry.getKey() + " (length: " + entry.getValue().size()  + ") -> " + entry.getValue());
        }
    } 

    public void printDocIDsReversed()
    {
        for(Map.Entry<String, String> e: docIDsReversed.entrySet())
        {
            System.out.println(e.getKey() + "->" + e.getValue());
        }

    }


    public void retrievePageRank()
    {
        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader("power-iteration"));

            // TODO
            while ((sCurrentLine = br.readLine()) != null) {
                String[] data = sCurrentLine.split(",");
                /* System.out.println(data[0] + " -> " + data[1]); */
                int idDoc = Integer.parseInt(data[0]);
                if(docIDsReversed.containsKey("" + idDoc))
                {
                    /* System.out.println("contains " + idDoc); */
                    /* System.out.println(docIDsReversed.get("" + idDoc)); */
                    rankingScores.put(Integer.parseInt(docIDsReversed.get("" + idDoc)), Double.parseDouble(data[1]));
                }
                /* System.out.println(data); */

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* BufferedReader br = new BufferedReader(new FileReader("power-iteration")); */
        /* try { */
        /*     StringBuilder sb = new StringBuilder(); */
        /*     String line = br.readLine(); */

        /*     while (line != null) { */
        /*         sb.append(line); */
        /*         sb.append("\n"); */
        /*         line = br.readLine(); */
        /*     } */
        /*     String everything = sb.toString(); */
        /*     System.out.println(everything); */
        /* } finally { */
        /*     br.close(); */
        /* } */

    }
    /* public void scorePostingsList(PostingsLists p, String term) */
    /* { */
    /*     int N = p.getList().size(); */
    /*     for(PostingsEntry pe: p.getList()) */
    /*     { */
    /*         double tftd = pe.getIndexOfList().size(); */
    /*         double wtd = (tftd > 0 ? 1 + Math.log(tftd): 0); */
    /*     } */
    /* } */


}
