package ir;

import java.util.Comparator;

public class DocIDComparator implements Comparator<PostingsEntry>
{
    @Override
    public int compare(PostingsEntry p1, PostingsEntry p2) {
        return (p2.getDocID()>p1.getDocID() ? -1 : (p1.getDocID()==p2.getDocID() ? 0 : 1));
    }
}
