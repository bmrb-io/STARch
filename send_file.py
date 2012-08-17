#!/usr/bin/python -u
#

class SendFile( object ) :
    """wraps file in an iterable to be used as response body"""
    _filename = None
    _fp = None

    def __init__( self, filename = None ) :
        self._filename = filename

    def _get_filename( self ) : 
        return self._filename
    def _set_filename( self, filename ) :
        self._filename = filename
    filename = property( _get_filename, _set_filename )

    def __iter__( self ) :
        return self

    def next( self ) :
        if self._fp == None :
            self._fp = open( self._filename )
        b = self._fp.read( 16384 )
        if not b :
            self._fp.close()
            raise StopIteration
        else :
            return b


#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
