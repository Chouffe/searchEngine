/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
        return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    public void add( int docID ){
        PostingsEntry p = new PostingsEntry(docID);
        list.add(p);
    }

    public void add( int docID, int indexOf ){
        PostingsEntry p = new PostingsEntry(docID, indexOf);
        list.add(p);
    }

    public void add( PostingsEntry pe )
    {
        PostingsEntry pnew = new PostingsEntry(pe.getDocID(), pe.getScore());
        list.add(pnew);
    }
    
    public void add(LinkedList<PostingsEntry> peList)
    {
        for(PostingsEntry pe: peList)
        {
            // TODO: Clone??
            list.add(pe);
        }
    }

    /**
     * Add indexOf int the PostingsEntry 
     */
    /* public void addIndexOf( PostingsEntry pe, int indexOf ) */
    /* { */
    /*     pe.addIndexOf( indexOf );  */
    /* } */

    public PostingsEntry addIndexOf( int docID, int indexOf )
    {
        // We retrieve the PostEntry having the right docID
        for( PostingsEntry pe: list ){
            if ( pe.getDocID() == docID ){
                pe.addIndexOf( indexOf );
                return pe;
             }
        }

        PostingsEntry pe = new PostingsEntry(docID, indexOf);
        list.add( pe );
        return pe;
    }

    @Override
    public String toString()
    {
        String result = "[";
        for (PostingsEntry pe : list)
        {
            result += "," + pe.toString();
        }
        result += "]";

        return result;
    }

    public String toStringMin()
    {
        String result = "[";
        for (PostingsEntry pe : list)
        {
            result += "," + pe.toStringMin();
        }
        result += "]";

        return result;
    }

    public boolean contains( int id )
    {
        return list.contains( new PostingsEntry( id ) );
    }

    public LinkedList<PostingsEntry> getList()
    {
        return list;
    }

    public double getMaxScore()
    {
        return  list.getLast().getScore();
    }

    public double getMinScore()
    {
        return  list.getFirst().getScore();
    }
}



