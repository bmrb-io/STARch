#!/usr/bin/python -u
#
import sys
import os
import sqlite3

sys.path.append( "/bmrb/lib/python" )
from sans.lexer import STARLexer
from sans.handlers import ErrorHandler, ContentHandler2

#
# Reads any NMR-STAR file
# returns tag and value in separate callbacks
# 
class parser2 :
    _ch = None
    _eh = None
    _lex = None
    _blockId = ""

    def __init__( self, lex, ch, eh ) :
        self._lex = lex
        self._ch = ch
        self._eh = eh

    def parse( self ) :
        if self._lex == None : 
            print "Lexer not initialized"
            sys.exit( 1 )
        if self._ch == None : 
            print "Content handler not initialized"
            sys.exit( 1 )
        if self._eh == None : 
            print "Error handler not initialized"
            sys.exit( 1 )
        tag = ""
        val = ""
        needvalue = False
        while True :
            tok = self._lex.yylex()
            if tok == STARLexer.ERROR :
                self._eh.fatalError( self._lex.getLine(), self._lex.getText() )
                return
            elif tok == STARLexer.WARNING :
                if self._eh.warning( self._lex.getLine(), self._lex.getText() ) :
                    return
            elif tok == STARLexer.FILEEND :
                self._ch.endData( self._lex.getLine(), self._blockId )
                return
            elif tok == STARLexer.COMMENT :
                if self._ch.comment( self._lex.getLine(), self._lex.getText() ) :
                    return
            elif tok == STARLexer.DATASTART :
                self._blockId = self._lex.getText()
                if self._ch.startData( self._lex.getLine(), self._blockId ) :
                    return
                if self.parseDataBlock() : 
                    return
            elif tok == STARLexer.SAVESTART :
                if self._ch.startSaveFrame( self._lex.getLine(), self._lex.getText() ) :
                    return
            elif tok == STARLexer.LOOPSTART :
                if self._ch.startLoop( self._lex.getLine() ) :
                    return
                if self.parseLoop() :
                    return
            elif tok == STARLexer.TAGNAME :
                if needvalue :
                    if self._eh.error( self._lex.getLine(), "Value expected, found \"loop_\"" ) :
                        return True
                if self._ch.tag( self._lex.getLine(), self._lex.getText() ) :
                    return True
                needvalue = True
            elif (tok == STARLexer.DVNSINGLE) or (tok == STARLexer.DVNDOUBLE) \
              or (tok == STARLexer.DVNNON) or (tok == STARLexer.DVNSEMICOLON) \
              or (tok == STARLexer.DVNFRAMECODE) :
                if not needvalue :
                    if self._eh.error( self._lex.getLine(), "Value not expected here: %s" % (self._lex.getText()) ) :
                        return True
                needvalue = False
                val = self._lex.getText()
                if tok == STARLexer.DVNSEMICOLON :
                    if val.startswith( "\n" ) : val = val.lstrip( "\n" )
                    n = STARLexer._re_data.search( val )
                    if not n : n = STARLexer._re_saveend.search( val )
                    if not n : n = STARLexer._re_loop.search( val )
                    if not n : n = STARLexer._re_stop.search( val )
                    if not n : n = STARLexer._re_tag.search( val )
                    if n :
                        if self._eh.warning( self._lex.getLine(), "Keyword in value: %s" % (n.group())  ) :
                            return True
                if self._ch.value( self._lex.getLine(), val, tok ) :
                    return True
                val = ""
            else :
                self._eh.fatalError( self._lex.getLine(), "Invalid token: %s" % (self._lex.getText()) )
                return

#
#
#
    def parseDataBlock( self ) :
        if self._lex == None : 
            print "Lexer not initialized"
            sys.exit( 1 )
        if self._ch == None : 
            print "Content handler not initialized"
            sys.exit( 1 )
        if self._eh == None : 
            print "Error handler not initialized"
            sys.exit( 1 )
        tag = ""
        val = ""
        needvalue = False
        while True :
            tok = self._lex.yylex()
            if tok == STARLexer.ERROR :
                print "+ error", self._lex.getText()
                self._eh.fatalError( self._lex.getLine(), self._lex.getText() )
                return True
            elif tok == STARLexer.WARNING :
                if self._eh.warning( self._lex.getLine(), self._lex.getText() ) :
                    return True
            elif tok == STARLexer.FILEEND :
                self._ch.endData( self._lex.getLine(), self._blockId )
                return True
            elif tok == STARLexer.COMMENT :
                if self._ch.comment( self._lex.getLine(), self._lex.getText() ) :
                    return True
            elif tok == STARLexer.SAVESTART :
                if self._ch.startSaveFrame( self._lex.getLine(), self._lex.getText() ) :
                    return True
                if self.parseSaveFrame() : 
                    return True
            elif tok == STARLexer.LOOPSTART :
                if self._ch.startLoop( self._lex.getLine() ) :
                    return
                if self.parseLoop() :
                    return
            elif tok == STARLexer.TAGNAME :
                if needvalue :
                    if self._eh.error( self._lex.getLine(), "Value expected, found \"loop_\"" ) :
                        return True
                if self._ch.tag( self._lex.getLine(), self._lex.getText() ) :
                    return True
                needvalue = True
            elif (tok == STARLexer.DVNSINGLE) or (tok == STARLexer.DVNDOUBLE) \
              or (tok == STARLexer.DVNNON) or (tok == STARLexer.DVNSEMICOLON) \
              or (tok == STARLexer.DVNFRAMECODE) :
                if not needvalue :
                    if self._eh.error( self._lex.getLine(), "Value not expected here: %s" % (self._lex.getText()) ) :
                        return True
                needvalue = False
                val = self._lex.getText()
                if tok == STARLexer.DVNSEMICOLON :
                    if val.startswith( "\n" ) : val = val.lstrip( "\n" )
                    n = STARLexer._re_data.search( val )
                    if not n : n = STARLexer._re_saveend.search( val )
                    if not n : n = STARLexer._re_loop.search( val )
                    if not n : n = STARLexer._re_stop.search( val )
                    if not n : n = STARLexer._re_tag.search( val )
                    if n :
                        if self._eh.warning( self._lex.getLine(), "Keyword in value: %s" % (n.group())  ) :
                            return True
                if self._ch.value( self._lex.getLine(), val, tok ) :
                    return True
                val = ""
            else :
                self._eh.fatalError( self._lex.getLine(), "Invalid token in data block: %s" % (self._lex.getText()) )
                return True

#
#
#
    def parseSaveFrame( self ) :
        if self._lex == None : 
            print "Lexer not initialized"
            sys.exit( 1 )
        if self._ch == None : 
            print "Content handler not initialized"
            sys.exit( 1 )
        if self._eh == None : 
            print "Error handler not initialized"
            sys.exit( 1 )
        tag = ""
        val = ""
        needvalue = False
        while True :
            tok = self._lex.yylex()
            if tok == STARLexer.ERROR :
                self._eh.fatalError( self._lex.getLine(), self._lex.getText() )
                return True
            elif tok == STARLexer.WARNING :
                if self._eh.warning( self._lex.getLine(), self._lex.getText() ) :
                    return
            elif tok == STARLexer.FILEEND :
                if self._eh.error( self._lex.getLine(), "Premature end of file: no closing \"save_\"" ) :
                    return True
                self._ch.endData( self._lex.getLine(), self._blockId )
                return True
            elif tok == STARLexer.STOP :
                self._eh.fatalError( self._lex.getLine(), "Found \"stop_\", expected \"save_\"" )
                return True
            elif tok == STARLexer.COMMENT :
                if self._ch.comment( self._lex.getLine(), self._lex.getText() ) :
                    return True
            elif tok == STARLexer.SAVEEND :
                if needvalue :
                    if self._eh.error( self._lex.getLine(), "Value expected, found \"save_\"" ) :
                        return True
                return self._ch.endSaveFrame( self._lex.getLine(), self._lex.getText() )
            elif tok == STARLexer.LOOPSTART :
                if needvalue :
                    if self._eh.error( self._lex.getLine(), "Value expected, found \"loop_\"" ) :
                        return True
                if self._ch.startLoop( self._lex.getLine() ) :
                    return True
                if self.parseLoop() :
                    return True
            elif tok == STARLexer.TAGNAME :
                if needvalue :
                    if self._eh.error( self._lex.getLine(), "Value expected, found \"loop_\"" ) :
                        return True
                if self._ch.tag( self._lex.getLine(), self._lex.getText() ) :
                    return True
                needvalue = True
            elif (tok == STARLexer.DVNSINGLE) or (tok == STARLexer.DVNDOUBLE) \
              or (tok == STARLexer.DVNNON) or (tok == STARLexer.DVNSEMICOLON) \
              or (tok == STARLexer.DVNFRAMECODE) :
                if not needvalue :
                    if self._eh.error( self._lex.getLine(), "Value not expected here: %s" % (self._lex.getText()) ) :
                        return True
                needvalue = False
                val = self._lex.getText()
                if tok == STARLexer.DVNSEMICOLON :
                    if val.startswith( "\n" ) : val = val.lstrip( "\n" )
                    n = STARLexer._re_data.search( val )
                    if not n : n = STARLexer._re_saveend.search( val )
                    if not n : n = STARLexer._re_loop.search( val )
                    if not n : n = STARLexer._re_stop.search( val )
                    if not n : n = STARLexer._re_tag.search( val )
                    if n :
                        if self._eh.warning( self._lex.getLine(), "Keyword in value: %s" % (n.group())  ) :
                            return True
                if self._ch.value( self._lex.getLine(), val, tok ) :
                    return True
                val = ""
            else :
                self._eh.fatalError( self._lex.getLine(), "Invalid token in saveframe: %s" % (self._lex.getText()) )
                return True

#
#
#
    def parseLoop( self ) :
        if self._lex == None : 
            print "Lexer not initialized"
            sys.exit( 1 )
        if self._ch == None : 
            print "Content handler not initialized"
            sys.exit( 1 )
        if self._eh == None : 
            print "Error handler not initialized"
            sys.exit( 1 )
        numtags = 0
        numvals = 0
        loopcol = 0
        lastline = -1
        wrongline = -1
        wrongcol = -1
        parsingtags = True
        val = ""
        while True :
            tok = self._lex.yylex()
            if tok == STARLexer.ERROR :
                self._eh.fatalError( self._lex.getLine(), self._lex.getText() )
                return True
            elif tok == STARLexer.WARNING :
                if self._eh.warning( self._lex.getLine(), self._lex.getText() ) :
                    return
            elif tok == STARLexer.FILEEND :
                if self._eh.error( self._lex.getLine(), "Premature end of file: no closing \"stop_\"" ) :
                    return True
                self._ch.endData( self._lex.getLine(), self._blockId )
                return True
            elif tok == STARLexer.SAVEEND :
                self._eh.fatalError( self._lex.getLine(), "Found \"save_\", expected \"stop_\"" )
                return True
            elif tok == STARLexer.LOOPSTART :
                self._eh.fatalError( self._lex.getLine(), "\"loop_\" not expected here" )
                return True
            elif tok == STARLexer.COMMENT :
                if self._ch.comment( self._lex.getLine(), self._lex.getText() ) :
                    return True
# normal exit point
            elif tok == STARLexer.STOP :
# loop checks
                if numtags < 1 :
                    if self._eh.error( self._lex.getLine(), "Loop with no tags" ) :
                        return True
                if numvals < 1 :
                    if self._eh.error( self._lex.getLine(), "Loop with no values" ) :
                        return True
# loop count
                if (numvals % numtags) != 0 :
                    if wrongline < 0 : wrongline = self._lex.getLine()
                    if self._eh.warning( wrongline, "Loop count error" ) :
                        return True
                if self._ch.endLoop( self._lex.getLine() ) :
                    return True
                return False
            elif tok == STARLexer.TAGNAME :
                if not parsingtags :
                    if self._eh.error( self._lex.getLine(), "Tag not expected here: %s" % (self._lex.getText()) ) :
                        return True
                numtags += 1
                if self._ch.tag( self._lex.getLine(), self._lex.getText() ) :
                    return True

            elif (tok == STARLexer.DVNSINGLE) or (tok == STARLexer.DVNDOUBLE) \
              or (tok == STARLexer.DVNNON) or (tok == STARLexer.DVNSEMICOLON) \
              or (tok == STARLexer.DVNFRAMECODE) :
                if numtags < 1 :
                    if self._eh.error( self._lex.getLine(), "Loop with no tags: expected tag, found %s" % (self._lex.getText()) ) :
                        return True
                if parsingtags : parsingtags = False
                val = self._lex.getText()
                if tok == STARLexer.DVNSEMICOLON :
                    if val.startswith( "\n" ) : val = val.lstrip( "\n" )
                    n = STARLexer._re_data.search( val )
                    if not n : n = STARLexer._re_saveend.search( val )
                    if not n : n = STARLexer._re_loop.search( val )
                    if not n : n = STARLexer._re_stop.search( val )
                    if not n : n = STARLexer._re_tag.search( val )
                    if n :
                        if self._eh.warning( self._lex.getLine(), "Keyword in value: %s" % (n.group())  ) :
                            return True
                numvals += 1
                loopcol += 1
                if loopcol == numtags :
                    if lastline != self._lex.getLine() :
                        if wrongline < 0 : wrongline = self._lex.getLine()
                        lastline = self._lex.getLine()
                    loopcol = 0
                if self._ch.value( self._lex.getLine(), val, tok ) :
                    return True
                val = ""
            else :
                self._eh.fatalError( self._lex.getLine(), "Invalid token in loop: %s" % (self._lex.getText()) )
                return True

#
# parse 1st loop from NMR-STAR file into sqlite3 table
#
class handler( object, ContentHandler2, ErrorHandler ) :

    _errs = None
    _in_loop = False

    _table = None
    _tags = None
    _row = None
    _col = None
    _sql = None

    _conn = None
    _curs = None

    def __init__( self, conn = None ) :
        self._conn = conn
        self._errs = []

    def _get_conn( self ) :
        """sqlite3 DB connection"""
        return self._conn
    def _set_conn( self, conn ) :
        self._conn = conn
    connection = property( _get_conn, _set_conn )

    def hasErrors( self ) :
        return (len( self._errs ) > 0)
    def _get_errlist( self ) :
        """array of error messages"""
        return self._errs
    errors = property( _get_errlist )

    def _get_table( self ) :
        """table name"""
        return self._table
    table = property( _get_table )

    def fatalError( self, line, msg ) :
        self._errs.append( "fatal parse error in line %d: %s" % (line,msg) )

    def error( self, line, msg ) :
        self._errs.append( "parse error in line %d: %s" % (line,msg) )
        return True

    def warning( self, line, msg ) :
        self._errs.append( "parse warning (treated as error) in line %d: %s" % (line,msg) )
        return True

    def comment( self, line, text ) :
        return False
    def startSaveFrame( self, line, name ) :
        return False
    def endSaveFrame( self, line, name ) :
        return False
    def startData( self, line, name ) :
        return False

    def startLoop( self, line ) :
        if self._conn == None :
            raise UnboundLocalError( "Missing DB connection!" )
        self._in_loop = True
#        self._conn.isolation_level = None
        self._col = 0
        return False

    def tag( self, line, tag ) :
        if not self._in_loop : return False

        if tag.find( "." ) < 0 : raise ValueError( "Invalid tag name: %s" % (tag) )
        self._table = tag.split( "." )[0][1:]
        col = tag.split( "." )[1]

        if self._tags == None : 
            self._tags = []
            self._row = []
# create table
            self._curs = self._conn.cursor()
            sql = "select tagfield,dictionaryseq from val_item_tbl where tagcategory=? order by dictionaryseq"
            self._curs.execute( sql, (self._table,) )
            sql = 'create table "%s" (' % (self._table)
            while True :
                row = self._curs.fetchone()
                if row == None : break
#                print row
                sql += '"%s" text,' % (row[0])
            sql = sql[:-1]
            sql += ")"
            self._curs.execute( sql )

        self._tags.append( col )


    def value( self, line, val, delim ) :
        if not self._in_loop : return False

        if self._col == 0 :
            self._sql = 'insert into "%s" ("' % (self._table)
            self._sql += '","'.join( i for i in self._tags )
            self._sql += '") values ('
            self._sql += ",".join( "?" for i in self._tags )
            self._sql += ")"
#            print self._sql


        self._col += 1
        if self._col == (len( self._tags ) + 1) :
#            print self._row
#            print self._sql.replace( "?", "%s" ) % tuple( self._row )
            self._curs.execute( self._sql, tuple( self._row ) )
            self._row = []
            self._col = 1

        self._row.append( val )

#        print "Value", val, "in line", line

    def endLoop( self, line ) :
#        print "end loop in line", line
        self._curs.close()
        self._conn.commit()
        return True

    def endData( self, line, name ) :
#        print "end data block", name, "in line", line
        pass




#
#
#
def main() :
    l = STARLexer( sys.stdin )
    h = handler()
    p = parser2( l, h, h )
    p.parse()
#
#
#
if __name__ == "__main__" :
    main()
#
#
