#!/usr/bin/perl -w
#
#

use strict;
use English;
use File::Basename;

if( scalar( @ARGV ) < 1 ) {
    print "Usage: $0 <input file>\n";
    exit( 1 );
}

my $file;
my $dir;
my $ext;
( $file, $dir, $ext ) = fileparse( $ARGV[0], qr/\..*/ );

my $line;
my $parse_flag = 0;
my @fields = ();
my %residues;
my @atoms = ();
my $i;
my $found = 0;

while( $line = <> ) {
    if( $line =~ /^\s*$/ ) { next; }
    if( $line =~ /_Atom_chem_shift.ID/ ) { $parse_flag = 1; }
    if( $parse_flag == 0 ) { next; }
    else {
	if( $line =~ /stop_/ ) { last; }
	elsif( $line =~ /_Atom_chem_shift/ ) { next; }
	else {
	    chomp( $line );
	    $line =~ s/^\s+//;
	    $line =~ s/\s+$//;
	    @fields = split( /\s+/, $line );
#print "$fields[4] $fields[6] $fields[7] $fields[10] $fields[11] $fields[12]\n";
	    $residues{$fields[4]} = $fields[6];
	    $found = 0;
	    for( $i = 0; $i < scalar( @atoms ); $i++ ) {
		if( ($atoms[$i]{"seq"} == $fields[4]) && ($atoms[$i]{"name"} eq $fields[7]) ) {
#print "Compare " . $atoms[$i]{"seq"} . ", " . $atoms[$i]{"name"} . ", " . $atoms[$i]{"val"} . " with $fields[4], $fields[7], $fields[10]\n";
#print "scores: " . $atoms[$i]{"score"} . " vs $fields[12]\n";
		    if( $fields[12] > $atoms[$i]{"score"} ) {
			$atoms[$i]{"score"} = $fields[12];
			$atoms[$i]{"val"} = $fields[10];
			$atoms[$i]{"err"} = $fields[11];
		    }
		    $found = 1;
		    last;
		}
	    }
	    if( $found == 0 ) {
		push( @atoms, { "seq" => $fields[4], "name" => $fields[7], "val" => $fields[10], "err" => $fields[11], "score" => $fields[12] } );
	    }
	}
    }
}

open( SEQ, ">$file.seq" ) or die "Cannot create $file.seq: $OS_ERROR\n";
foreach $line( sort( { $a <=> $b } keys( %residues ) ) ) { print SEQ $residues{$line} . "    " . $line . "\n"; }
close( SEQ );

open( PROT, ">$file.prot" )  or die "Cannot create $file.prot: $OS_ERROR\n";
for( $i = 0; $i < scalar( @atoms ); $i++ ) {
    printf PROT "%6d    %8.3f    %8.3f    %6s    %d\n", $i, $atoms[$i]{"val"}, $atoms[$i]{"err"}, $atoms[$i]{"name"}, $atoms[$i]{"seq"};
}
close( PROT );

exit( 0 );
