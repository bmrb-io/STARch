package edu.bmrb.starch;

/**
 * Table column.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 7, 2006
 * Time: 4:51:15 PM
 *
 * $Id$
 */

public class Column {
    public enum ValTypes {
        INT,
        STRING
    }
    private String fName = null;
    private int fGroupId = 0;
    private ColTypes fType = null;
    private ValTypes fValType = null;
    private boolean fRequired = true;
    public Column( String name ) {
        fName = name;
    }
    public String getDbName() {
        return fName;
    }
    public void setDbName( String name ) {
        fName = name;
    }
    public int getGroupId() {
        return fGroupId;
    }
    public void setGroupId( int id ) {
        fGroupId = id;
    }
    public String getLabel() {
        return Utils.ColNameToLabel( fName );
    }
    public ColTypes getType() {
        return fType;
    }
    public void setType( ColTypes type ) {
        fType = type;
    }
    public ValTypes getValType() {
        return fValType;
    }
    public void setValType( ValTypes type ) {
        fValType = type;
    }
    public boolean isRequired() {
        return fRequired;
    }
    public void setRequired( boolean flag ) {
        fRequired = flag;
    }
    public String toString() {
        StringBuilder buf = new StringBuilder( "Column: " );
        buf.append( fName );
        buf.append( '(' );
        buf.append( Utils.ColNameToLabel( fName ) );
        buf.append( ") type " );
        buf.append( fValType.name() );
        buf.append( " val " );
        buf.append( fType.name() );
        buf.append( " group " );
        buf.append( fGroupId );
        buf.append( " required " );
        buf.append( fRequired );
        return buf.toString();
    }
}
