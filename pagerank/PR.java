import java.util.*;
import java.io.*;

public class PR implements Comparable<PR>{

    double score = 0.;
    int index = -1;

    public PR(double score, int index)
    {
        this.score = score;
        this.index = index;
    }

    public int compareTo(PR pr2) {
 
        double compareQuantity = pr2.getScore();
 
        //ascending order
        double result = this.score - compareQuantity;
        int r = 0;
        if( result > 0.)
        {
            r = 1;
        }
        else if( result < 0. )
        {
            r = -1;
        }
        else
        {
            r = 0;
        }
        return r;
 
    }

    public double getScore()
    {
        return this.score;
    }  

}
