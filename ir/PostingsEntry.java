/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public LinkedList<Integer> indexOfList = new LinkedList<Integer>();

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }

    /**
     * Constructors
     */
    public PostingsEntry( int id )
    {
        this.docID = id;
    }

    public PostingsEntry( int id, double sc )
    {
        this.docID = id;
        this.score = sc;
    }

    public PostingsEntry( int id, int offset )
    {
        this( id );
        indexOfList.add( offset );
    }

    public PostingsEntry(PostingsEntry pe)
    {
        // We clone everything

        this.docID = pe.getDocID();
        this.score = pe.getScore();
        this.indexOfList = new LinkedList<Integer>();

    }


    public int getDocID()
    {
        return this.docID;
    }

    public double getScore()
    {
        return this.score;
    }

    @Override
    public String toString()
    {
        String result = "";
        result += docID + " -> ";
        result += printIndexOfList();
        return result;
    }
    public String toStringMin()
    {
        return "" + docID;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }

        PostingsEntry pe = (PostingsEntry) obj;
        if( pe.getDocID() == this.docID )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
        
    public void addIndexOf(int indexOf)
    {
        indexOfList.add(indexOf);
    }

    public LinkedList<Integer> getIndexOfList()
    {
        return indexOfList;
    }

    /** 
     * Return the indexOfList
     */
    public String printIndexOfList()
    {
        String result = "[";
        for( int indexOf: indexOfList )
        {
            result += ", " + indexOf;
        }
        result += "]";
        return result;
    }
    
    //
    //  YOUR CODE HERE
    //

}

    
