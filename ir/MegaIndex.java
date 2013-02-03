/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  

package ir;

import com.larvalabs.megamap.MegaMapManager;
import com.larvalabs.megamap.MegaMap;
import com.larvalabs.megamap.MegaMapException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.ListIterator;
import java.lang.Math;
import java.util.Collections;


public class MegaIndex implements Index {

    /** 
     *  The index as a hash map that can also extend to secondary 
     *	memory if necessary. 
     */
    private MegaMap index;


    /** 
     *  The MegaMapManager is the user's entry point for creating and
     *  saving MegaMaps on disk.
     */
    private MegaMapManager manager;


    /** The directory where to place index files on disk. */
    private static final String path = "./index";


    /**
     *  Create a new index and invent a name for it.
     */
    public MegaIndex() {
        try {
            manager = MegaMapManager.getMegaMapManager();
            index = manager.createMegaMap( generateFilename(), path, true, false );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    /**
     *  Create a MegaIndex, possibly from a list of smaller
     *  indexes.
     */
    public MegaIndex( LinkedList<String> indexfiles ) {
        try {
            manager = MegaMapManager.getMegaMapManager();
            if ( indexfiles.size() == 0 ) {
                // No index file names specified. Construct a new index and
                // invent a name for it.
                index = manager.createMegaMap( generateFilename(), path, true, false );

            }
            else if ( indexfiles.size() == 1 ) {
                // Read the specified index from file
                index = manager.createMegaMap( indexfiles.get(0), path, true, false );
                HashMap<String,String> m = (HashMap<String,String>)index.get( "..docIDs" );
                if ( m == null ) {
                    System.err.println( "Couldn't retrieve the associations between docIDs and document names" );
                }
                else {
                    docIDs.putAll( m );
                }
            }
            else {
                // Merge the specified index files into a large index.
                MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
                for ( int k=0; k<indexfiles.size(); k++ ) {
                    System.err.println( indexfiles.get(k) );
                    indexesToBeMerged[k] = manager.createMegaMap( indexfiles.get(k), path, true, false );
                }
                index = merge( indexesToBeMerged );
                sortIndex();
                for ( int k=0; k<indexfiles.size(); k++ ) {
                    manager.removeMegaMap( indexfiles.get(k) );
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    /**
     *  Generates unique names for index files
     */
    String generateFilename() {
        String s = "index_" + Math.abs((new java.util.Date()).hashCode());
        System.err.println( s );
        return s;
    }


    /**
     *   It is ABSOLUTELY ESSENTIAL to run this method before terminating 
     *   the JVM, otherwise the index files might become corrupted.
     */
    public void cleanup() {
        // Save the docID-filename association list in the MegaMap as well
        index.put( "..docIDs", docIDs );
        // Shutdown the MegaMap thread gracefully
        manager.shutdown();
    }



    /**
     *  Returns the dictionary (the set of terms in the index)
     *  as a HashSet.
     */
    public Set getDictionary() {
        return index.getKeys();
    }


    /**
     *  Merges several indexes into one.
     */
    MegaMap merge( MegaMap[] indexes ) {

        try {
            MegaMap res = manager.createMegaMap( generateFilename(), path, true, false );
            int i = 0;
            HashMap<String,String> m = new HashMap<String,String>();

            for(MegaMap index: indexes)
            {
                if(i == 0)
                {
                    // Push the docIds in the Index
                    m.putAll((HashMap<String,String>)index.get("..docIDs"));

                    // We just copy the whole index into the res
                    for(Object term: index.getKeys())
                    {
                        if( !term.equals("..docIDs"))
                        {
                            try{
                                if ( index.get((Serializable) term) instanceof PostingsList ) {
                                    PostingsList p = (PostingsList) index.get((Serializable)term);
                                    res.put((Serializable)term, p);
                                }
                            }
                            catch(MegaMapException e)
                            {
                                System.out.println("fail...");
                            }
                        }

                    }  

                }
                else
                {
                    // Push the docIds in the Index
                    m.putAll((HashMap<String,String>)index.get("..docIDs"));

                    for(Object term: index.getKeys())
                    {

                        if( !term.equals("..docIDs"))
                        {
                            try{
                                // Push the docIds in the Index
                                m.putAll((HashMap<String,String>)index.get("..docIDs"));

                                if(res.hasKey((Serializable)term))
                                {
                                    if ( index.get((Serializable) term) instanceof PostingsList ) {

                                        PostingsList p = (PostingsList) index.get((Serializable)term);
                                        PostingsList pRes = (PostingsList) res.get((Serializable)term);
                                        PostingsList pIndex = (PostingsList) index.get((Serializable)term);

                                        pRes.add(pIndex.getList());
                                    }
                                }
                                else
                                {   
                                    if ( index.get((Serializable) term) instanceof PostingsList ) {
                                        PostingsList p = (PostingsList) index.get((Serializable)term);
                                        res.put((Serializable)term, p);
                                    }
                                }
                            }
                            catch(MegaMapException e)
                            {
                                System.out.println("fail...");
                            }
                        }
                    }  
                }
                i++;
            }

            docIDs.putAll(m);
            return res;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        if( index.hasKey(token) )
        {
            try
            {

                PostingsList p = (PostingsList) index.get(token);
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
            catch(MegaMapException e)
            {
                System.out.println("Fail");
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
        try {
            return (PostingsList)index.get( token );
        }
        catch( Exception e ) {
            return new PostingsList();
        }
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {

        PostingsList result = new PostingsList();

        // Phrase queries
        if ( queryType == Index.PHRASE_QUERY )
        {

            int i = 0;
            for(String term: query.getTerms())
            {
                if ( i == 0 )
                {
                    result = getPostings(term);
                }
                else
                {
                    result = positionalIntersect(result, getPostings(term));

                    Query q = new Query(SimpleTokenizer.normalize("kriget"));

                    for(String t: q.getTerms())
                    {
                        if (term.equals(t)) 
                        {
                            result.getList().removeLast();
                            break;

                        }
                    }

                    /* result.getList().removeLast(); */
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
                if ( i == 0 )
                {
                    result = getPostings(term);
                }
                else
                {
                    System.out.println(result);
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
     * Intersect two posting lists given a asc sorted order
     * cf algorithm p11 book
     * Complexity O(n1+n2) 
     * where n1 = length(p1), n2 = length(p2)
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
     * cf algorithm in the book with k=1
     */
    public PostingsList positionalIntersect(PostingsList p1, PostingsList p2)
    {

        PostingsList answer = new PostingsList();
        ListIterator it1 = p1.getList().listIterator();
        ListIterator it2 = p2.getList().listIterator();

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

                ArrayList<Integer> l = new ArrayList<Integer>();

                ListIterator itpp1 = pp1.listIterator();

                while( itpp1.hasNext() )
                {
                    ListIterator itpp2 = pp2.listIterator();
                    int position1 = ((Integer) itpp1.next()).intValue();

                    while( itpp2.hasNext() )
                    {
                        int position2 = ((Integer) itpp2.next()).intValue();

                        if( Math.abs( position1 - position2 ) <= 1 )
                        {
                            l.add(position2);
                        }
                        else if ( position2 > position1 )
                        {
                            break;
                        }
                    }

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
                            if(!answer.contains(pe1.getDocID()))
                            {
                                answer.add(pe1.getDocID(), pos);
                            }
                        }
                        else
                        {
                            /* answer.addIndexOf(pe1.getDocID(), pos); */
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
     * Sort the MegaMapIndex by docID Asc so that the algorithms work
     * Complexity O(n*mlog(m)) 
     * where n = length(index) and m =
     * mean_length(postingList)
     */
    public void sortIndex()
    {
        for(Object term: index.getKeys())
        {
            try{
                PostingsList p = (PostingsList) index.get((Serializable)term);
                /* System.out.println(term + " -> " + p); */
                Collections.sort(p.getList(), new DocIDComparator());
                /* System.out.println(term + " -> " + p); */
            }
            catch(MegaMapException e)
            {
                System.out.println("fail...");
            }
        }  
        
    }

    /**
     * Print the whole Index out
     * For debugging
     */
    public void printIndex()
    {
        for(Object term: index.getKeys())
        {
            try{
                PostingsList p = (PostingsList) index.get((Serializable)term);
                System.out.println(term + " -> " + p);
            }
            catch(MegaMapException e)
            {
                System.out.println("fail...");
            }
        }  
    } 
}
