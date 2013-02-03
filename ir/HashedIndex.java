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
import java.util.Map;
import java.util.ArrayList;
import java.util.ListIterator;
import java.lang.Math;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {


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


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        // 
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
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

        System.out.println("Search processing");
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
                    result = positionalIntersect(result, getPostings(term));
                }

                i++;
            }
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
            /* it1.next(); */
            /* it2.next(); */
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

                System.out.println("DocID -> " + docID1);
                System.out.println("1 -> " + pp1);
                System.out.println("2 -> " + pp2);

                ArrayList<Integer> l = new ArrayList<Integer>();

                ListIterator itpp1 = pp1.listIterator();

                while( itpp1.hasNext() )
                {
                    ListIterator itpp2 = pp2.listIterator();
                    int position1 = ((Integer) itpp1.next()).intValue();
                    System.out.println("Postion 1: " + position1);

                    while( itpp2.hasNext() )
                    {
                        int position2 = ((Integer) itpp2.next()).intValue();
                        System.out.println("Postion 2: " + position2);


                        if( Math.abs( position1 - position2 ) <= 1 )
                        {
                            System.out.println("" + Integer.toString(position1) + " - " + Integer.toString(position2));
                            l.add(position2);
                        }
                        else if ( position2 > position1 )
                        {
                            break;
                        }
                    }
                    // Cleanup l
                    System.out.println("L: " + l);
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

}
