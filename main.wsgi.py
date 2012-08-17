#!/usr/bin/python -u
#


import os
import sys
import warnings
import werkzeug.routing
import werkzeug.wrappers
import werkzeug.exceptions

import sqlite3

import ConfigParser

sys.path.append( "/bmrb/lib/python" )
from sans.lexer import STARLexer
from sans.handlers import ErrorHandler, ContentHandler

import LoopParser
import TableEditor
import send_file
import show_table
import edit_form
import print_star


CONFIG = "tabedit.conf"

class TableEdit( object ) :

    _map = None
    _conn = None
    _props = None
    _dbfile = None
    _table = None

    def __init__( self, conffile = None ) :
        if conffile == None : raise UnboundLocalError( "Missing config file" )
        if len( conffile.strip() ) < 1 : raise UnboundLocalError( "Missing config file" )
        if not os.path.exists( conffile ) : raise UnboundLocalError( "Config file not found: %s" % (conffile) )
        self._props = ConfigParser.SafeConfigParser()
        self._props.read( conffile )

        self._map = werkzeug.routing.Map( [
            werkzeug.routing.Rule( "/", endpoint = "new" ),
            werkzeug.routing.Rule( "/upload", endpoint = "upload" ),
            werkzeug.routing.Rule( "/edit", endpoint = "edit" ),
            werkzeug.routing.Rule( "/update", endpoint = "update" ),
            werkzeug.routing.Rule( "/print", endpoint = "print" ),
            werkzeug.routing.Rule( "/help", endpoint = "help" ),
            werkzeug.routing.Rule( "/help/", endpoint = "help" )
        ] )

#
    def dispatch_request( self, request ) :
        adapter = self._map.bind_to_environ( request.environ )
        try :
            endpoint, values = adapter.match()
            return getattr( self, "on_" + endpoint )( request, **values )
        except werkzeug.exceptions.HTTPException, e :
            return e

#
    def wsgi_app( self, environ, start_response ) :
        request = werkzeug.wrappers.Request( environ )
        response = self.dispatch_request( request )
        return response( environ, start_response )

#
    def __call__( self, environ, start_response ) :
        return self.wsgi_app( environ, start_response )

#
# url handlers
#
    def on_new( self, request ) :
        """show file upload form"""
        s = send_file.SendFile();
        s.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/index.html" )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
        return response

    def on_upload( self, request ) :
        """read uploaded file into sqlite3 table and display it"""
        dictdsn = self._props.get( "main", "dictionary_dsn" )
        if not os.path.exists( dictdsn ) : raise UnboundLocalError( "Dictionary missing" )
        with warnings.catch_warnings() :
            warnings.simplefilter( "ignore", RuntimeWarning ) # yes I know about tempnam thankyouverymuch
            self._dbfile = os.tempnam( self._props.get( "main", "data_files" ) )
        self._conn = sqlite3.connect( self._dbfile )
        curs = self._conn.cursor()
        curs.execute( "attach database '%s' as dict" % (dictdsn) )
        curs.close()

        l = STARLexer( request.files["filename"].stream )
        h = LoopParser.handler( conn = self._conn )
        p = LoopParser.parser2( l, h, h )
        p.parse()
        if h.hasErrors() :
            self._conn.close()
            os.unlink( self._dbfile )
            return werkzeug.wrappers.Response( h.errors, status = 200, content_type = "text/plain" )

        self._table = h.table
        s = show_table.ShowTable( connection = self._conn, table = self._table )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
        return response

    def on_edit( self, request ) :
        """show edit form"""
        s = edit_form.EditForm( connection = self._conn, table = self._table, column = request.args["column"] )
        return werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )

    def on_update( self, request ) :
        """run editing function(s) and show table"""

        print request.args

        e = TableEditor.edit( connection = self._conn, table = self._table, column = request.args["column"] )
        if "const_val" in request.args.keys() :
            ovr = False
            if ("const_ovr" in request.args.keys()) and (request.args["const_ovr"] == "on") : ovr = True
            val = request.args["const_val"].strip()
            if len( val ) > 0 : e.insert_value( value = val, overwrite = ovr )

        if "col_copy" in request.args.keys() :
            val = request.args["col_copy"].strip()
            if len( val ) > 0 : e.copy_column( to_column = val )

        s = show_table.ShowTable( connection = self._conn, table = self._table )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
        return response

    def on_print( self, request ) :
        """unparse sqlite3 table to NMR-STAR and display as text/plain"""
        self._conn.close()
        s = print_star.PrintStar( filename = self._dbfile, table = self._table )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/plain" )
        return response

#
    def on_help( self, request ) :
        """show help file"""
        s = send_file.SendFile();
        s.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/help.html" )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
        return response
#        return redirect( "http://www.bmrb.wisc.edu" )

#
#
#
if __name__ == '__main__':

    from werkzeug.serving import run_simple
    from werkzeug.debug import DebuggedApplication
    app = TableEdit( CONFIG )
#    run_simple( '127.0.0.1', 5000, app, use_debugger = True, use_reloader = True )
    run_simple( '144.92.167.179', 5000, app, use_debugger = True, use_reloader = True )
