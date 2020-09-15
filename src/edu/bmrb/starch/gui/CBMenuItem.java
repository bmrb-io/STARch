package edu.bmrb.starch.gui;

/**
 * CheckBoxMenuItem with extra field.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 7, 2006
 * Time: 1:31:04 PM
 *
 * $Id$
 */

public class CBMenuItem extends javax.swing.JCheckBoxMenuItem {
    private int fId = -1;
    public CBMenuItem() {
        super();
    }
    public CBMenuItem( javax.swing.Action a ) {
        super( a );
    }
    public CBMenuItem( javax.swing.Icon i ) {
        super( i );
    }
    public CBMenuItem( String text ) {
        super( text );
    }
    public CBMenuItem( String text, boolean b ) {
        super( text, b );
    }
    public CBMenuItem( String text, javax.swing.Icon i, boolean b ) {
        super( text, i, b );
    }
    public int getID() {
        return fId;
    }
    public void setID( int id ) {
        fId = id;
    }
}
