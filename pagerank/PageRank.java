/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();
    Hashtable<Integer,String> idToName = new Hashtable<Integer,String>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.00001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 10000;


    /* --------------------------------------------- */


    public PageRank( String filename ) {
        int noOfDocs = readDocs( filename );
        buildIdToNameDoc();
        computePagerank( noOfDocs, true, "power-iteration" );
        computeApproximatedPageRank(1, true, "method1");
        computeApproximatedPageRank(2, true, "method2");
        computeApproximatedPageRank(3, true, "method3");
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
        int fileIndex = 0;
        try {
            System.err.print( "Reading file... " );
            BufferedReader in = new BufferedReader( new FileReader( filename ));
            String line;
            while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
                int index = line.indexOf( ";" );
                String title = line.substring( 0, index );
                Integer fromdoc = docNumber.get( title );
                //  Have we seen this document before?
                if ( fromdoc == null ) {	
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put( title, fromdoc );
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
                while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get( otherTitle );
                    if ( otherDoc == null ) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put( otherTitle, otherDoc );
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if ( link.get(fromdoc) == null ) {
                        link.put(fromdoc, new Hashtable<Integer,Boolean>());
                    }
                    if ( link.get(fromdoc).get(otherDoc) == null ) {
                        link.get(fromdoc).put( otherDoc, true );
                        out[fromdoc]++;
                    }
                }
            }
            if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
                System.err.print( "stopped reading since documents table is full. " );
            }
            else {
                System.err.print( "done. " );
            }
            // Compute the number of sinks.
            for ( int i=0; i<fileIndex; i++ ) {
                if ( out[i] == 0 )
                    numberOfSinks++;
            }
        }
        catch ( FileNotFoundException e ) {
            System.err.println( "File " + filename + " not found!" );
        }
        catch ( IOException e ) {
            System.err.println( "Error reading file " + filename );
        }
        System.err.println( "Read " + fileIndex + " number of documents" );
        return fileIndex;
    }


    /* --------------------------------------------- */


    public double dist(double[] x, double[] xstar)
    {
        double dist = 0.;

        for(int k = 0; k < x.length; k++)
        {
            dist += Math.abs(x[k] - xstar[k]);
        }

        /* System.out.println("Distance: " + dist); */
        return dist;

    }

    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs, boolean save, String filename ) {

        double c = 1. - BORED;
        int N = docNumber.size();
        /* System.out.println(numberOfDocs); */
        /* System.out.println(N); */

        // Initialization of the pagerank
        double[] x = initializePageRank(N);
        /* double[] x = new double[N]; */
        /* x[0] = 1; */

        double[] xStar = new double[N];

        double[] xNull = new double[N];
        double[] xOld = new double[N];

        /* printArray(x); */
        /* printArray(xStar); */
        /* printArray(xNull); */

        /* double epsilon = 0.1; */

        int numIter = 0;
        /* for(int k = 0; k < 1000; k++) */
        while(numIter < MAX_NUMBER_OF_ITERATIONS && dist(x, xOld) > EPSILON)
        {
            /* System.out.println("Sinks: " + numberOfSinks); */
            for(Map.Entry<Integer, Hashtable<Integer, Boolean>> entry: link.entrySet())
            {
                int i = entry.getKey();
                /* System.out.println(entry.getKey() + " -> " + out[entry.getKey()]); */
                for(Map.Entry<Integer, Boolean> column: entry.getValue().entrySet())
                {
                    int j = column.getKey();
                    xStar[j] += x[i]*c/out[i];
                    /* System.out.print(","+ column.getKey()); */
                }

                xStar[i] += (1-c)/N;
                xStar[i] += numberOfSinks/(N*N);
                /* System.out.println(); */
            }

            System.arraycopy(x, 0, xOld, 0, x.length);
            System.arraycopy(xStar, 0, x, 0, xStar.length);
            System.arraycopy(xNull, 0, xStar, 0, xNull.length);
            /* printArray(xStar); */
            /* printArray(x); */
            x = normalize(x);
            numIter++;
            /* printArray(x); */
        }

        /* printArray(x); */
        System.out.println("Num iterations: " + numIter);
        printSortedPageRank(x, save, filename); 
        /* sumArray(x); */
        /* printDocNumber(); */
    }

    public double[] initializePageRank(int N)
    {
        double[] x = new double[N];
        for(int k = 0; k < N; k++)
        {
            x[k] = 1./N;
        }
        return x;
    }

    public void printSortedPageRank(double[] x, boolean p, String output)
    {
        ArrayList<PR> myPageRanks = new ArrayList<PR>();

        for( int k = 0; k < x.length; k++)
        {
            myPageRanks.add(new PR(x[k], k));
        }

        Collections.sort(myPageRanks);
        Collections.reverse(myPageRanks);
        /* int i = 1; */
        /* for(PR pr: myPageRanks) */
        /* { */
        /*     if( i > 20 ) */
        /*     { */
        /*         break; */
        /*     } */
        /*     System.out.println(i + ". " + idToName.get(pr.index) + "  " + pr.score); */
        /*     i++; */
        /* } */

        if(p)
        {
            System.out.println("saving");
            savePageRankOnDisk(output, myPageRanks);
        }

    }

    public void sumArray(double[] x)
    {
        double sum = 0.;

        for( int k = 0; k < x.length; k++)
        {
            sum += x[k];
        }

        System.out.println(sum);
    }

    public void printDocNumber()
    {
        for(Map.Entry<String, Integer> e: docNumber.entrySet())
        {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }

    public double[] normalize(double[] x)
    {
        double sum = 0.;

        for( int k = 0; k < x.length; k++)
        {
            sum += x[k];
        }
        for( int k = 0; k < x.length; k++)
        {
            x[k] = x[k]/sum;
        }

        return x;

    }

    public void buildIdToNameDoc()
    {
        for(Map.Entry<String, Integer> e: docNumber.entrySet())
        {
           idToName.put(e.getValue(), e.getKey()); 
        }
    }

    /**
     * Starting page idPage
     * Return the page on which the surfer goes 
     */
    protected int randomSurf(int idPage, int N)
    {
        Random randomGenerator = new Random();
        double bored = randomGenerator.nextDouble();

        if(bored > BORED || out[idPage] == 0)
        {
            // Return random page between 0 and N-1
            return randomGenerator.nextInt(N);
        }
        else
        {
            int alea = randomGenerator.nextInt(out[idPage]);
            int[] outlinks = new int[out[idPage]];
            int i = 0;
            for(Map.Entry<Integer, Boolean> o: link.get(idPage).entrySet())
            {
                outlinks[i] = o.getKey();
                i++;
            }

            return outlinks[alea];
        }

    }


    public void computeApproximatedPageRank(int method, boolean save, String filename)
    {
        Random randomGenerator = new Random();
        double c = 1. - BORED;
        int N = docNumber.size();
        double[] n = new double[N];
        int T = 30;
        int numberOfRuns = 1000;

        if( method == 1 )
        {

        numberOfRuns = 100000;

            for( int i = 0; i < numberOfRuns; i++ )
            {
                int start = randomGenerator.nextInt(N);
                for(int t = 0; t < T; t++)
                {
                    int run = randomSurf(start, N);
                    start = run;
                    if( t == T-1 )
                    {
                        n[run] += 1;
                        break;
                    }
                }
            }

            // Normalization
            for( int i = 0; i < N; i++ )
            {
                n[i] = n[i] / (numberOfRuns);
            }

        }
        else if( method == 2 )
        {
            int M = 20;
            T = 20;
            numberOfRuns = N;

            for(int i = 0; i < numberOfRuns; i++)
            {
                int start = i;
                for(int m = 0; m < M; m++)
                {
                    for(int t = 0; t < T; t++)
                    {
                        int run = randomSurf(start, N);
                        start = run;

                        if( t == T-1 )
                        {
                            n[run] += 1;
                            break;
                        }
                    }
                }
            }

            // Normalization
            for( int i = 0; i < N; i++ )
            {
                n[i] = n[i] / (numberOfRuns*M);
            }
        }
        else if( method == 3 )
        {
            numberOfRuns = 100000;

            for( int i = 0; i < numberOfRuns; i++ )
            {
                int start = randomGenerator.nextInt(N);
                for(int t = 0; t < T; t++)
                {
                    int run = randomSurf(start, N);
                    start = run;
                    n[run] += 1;
                }
            }

            // Normalization
            for( int i = 0; i < N; i++ )
            {
                n[i] = n[i] / (numberOfRuns*T);
            }

        }

        // Normalization
        /* int sum = 0; */

        /* for( int i = 0; i < N; i++ ) */
        /* { */
        /*     sum += n[i]; */
        /* } */
        /* for( int i = 0; i < N; i++ ) */
        /* { */
        /*     n[i] = n[i] / sum; */
        /* } */

        printSortedPageRank(n, save, filename);
    }

    


    /* --------------------------------------------- */

    public void printArray(double[] array)
    {
        for( int i = 0; i < array.length; i++ )
        {
            System.out.print("," + array[i]);
        }
        System.out.println();
    }


    public static void main( String[] args ) {
        if ( args.length != 1 ) {
            System.err.println( "Please give the name of the link file" );
        }
        else {
            new PageRank( args[0] );
        }
    }

    protected void savePageRankOnDisk(String fileName, ArrayList<PR> myPageRanks)
    {
        // Format s
        System.out.println("Saving on disk...");
        String s = "";

        int i = 0;
        int topRank = docNumber.size();

        for(PR pr: myPageRanks)
        {
            if(i < topRank)
            {
                if( i == topRank - 1)
                {
                    s += idToName.get(pr.index) + " " + pr.score;
                    break;
                }
                else
                {
                    s += idToName.get(pr.index) + " " + pr.score+"\n";
                }
            }
            i++;
        }

        FileWriter output = null;
        BufferedWriter writer = null;

        try {
            output = new FileWriter(fileName);
            writer = new BufferedWriter(output);
            writer.write(s);
            /* System.out.println(s); */
            System.out.println("Data saved!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fail1...");
        } finally {
            if (output != null) {
                try {
                    /* output.flush(); */
                    /* output.close(); */
                    writer.flush();
                    output.close();
                    System.out.println("closed");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Fail...");
                }
            }
            else
            {
                System.out.println("No output...");
            }
        }
    }
}
