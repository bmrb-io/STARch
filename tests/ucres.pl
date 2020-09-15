#!/usr/bin/perl -w

while( $line = <> ) {
    chomp( $line );
    @fields = split( /\s+/, $line );
    for( $i = 1; $i < scalar( @fields ); $i++ ) { 
	if( ($i == 5) || ($i == 12) ) { $fields[$i] =~ tr/a-z/A-Z/; print "$fields[$i]\t"; }
	else { print "$fields[$i]\t"; }
    }
    print "\n";
}