#
# makefile for STARch
#

#include $(BMRBMAKEHEADER)
#
# these are defined in $(BMRBMAKEHEADER)
# comment them out if you have one
#
PERL_PATH=\/usr\/bin\/perl
JAVA_PATH=\/usr\/bin\/java
BMRBBASE=/bmrb
GLOBALCLASSES=$(BMRBBASE)/javaclasses
DOCUMENTROOT=$(BMRBBASE)/htdocs
#
# you may need to edit these
#
GLOBALCLASSES_SED=\/bmrb\/javaclasses
#
# Osaka mirror:
#
#GLOBALCLASSES_SED=\/export\/home\/bmrb\/javaclasses
#

SRCDIR=EDU/bmrb/starch
JAVAINSTDIR=$(GLOBALCLASSES)/$(SRCDIR)
#CGIINSTDIR=/share/dmaziuk/internal_html
CGIINSTDIR=$(DOCUMENTROOT)/starch
JAVADOCINSTDIR=$(CGIINSTDIR)/doc/javadoc
FTPDIR=$(BMRBBASE)/ftp/pub/software/bmrb

JAVAC=javac -target 1.4

all: starch cgi

classes: 
	cd $(SRCDIR) && $(JAVAC) Atom.java Converter.java \
	Data.java ErrorList.java \
	Format1.java Format2.java Format3.java Format4.java \
	Garret.java Main.java Messages.java Nomenmap.java \
	Pipp.java Ppm.java Residue.java Star2.java Starch.java \
	Utils.java Xeasy.java XeasyN.java \
	LoopTable.java *Writer.java *Reader.java


starch: classes

classes-all:
	cd EDU/bmrb/starch && $(JAVAC) *.java

install-starch:
	- mkdir -p $(JAVAINSTDIR)
	cp EDU/bmrb/starch/*.class $(JAVAINSTDIR)
	- mkdir -p $(FTPDIR)
	- cp starch.jar $(FTPDIR)

cgi: starch.pl
	cat starch.pl | \
	sed 's/PERLPATH_REPLACEME/$(PERL_PATH)/g' | \
	sed 's/CLASSPATH_REPLACEME/$(GLOBALCLASSES_SED)/g' | \
	sed 's/JAVAPATH_REPLACEME/$(JAVA_PATH)/g' > starch.cgi
	chmod 755 starch.cgi

install-cgi:
	- mkdir -p $(CGIINSTDIR)/tmp
	chmod 777 $(CGIINSTDIR)/tmp
	cp starch.cgi $(CGIINSTDIR)
	cp index.html $(CGIINSTDIR)
	chmod a+x $(CGIINSTDIR)/index.html
	- mkdir $(CGIINSTDIR)/doc
	cp doc/*.html $(CGIINSTDIR)/doc
	cp doc/starch.css $(CGIINSTDIR)/doc

oldcgi: starch.oldperl.pl
	cat starch.oldperl.pl | \
	sed 's/PERLPATH_REPLACEME/$(PERL_PATH)/g' > starch.cgi
	chmod 755 starch.cgi

install-oldcgi: oldcgi
	- mkdir -p $(CGIINSTDIR)/tmp
	chmod 777 $(CGIINSTDIR)/tmp
	cp starch.cgi $(CGIINSTDIR)
	cp cgi-lib2.pl $(CGIINSTDIR)
	cp index.html $(CGIINSTDIR)
	- mkdir $(CGIINSTDIR)/doc
	cp doc/*.html $(CGIINSTDIR)/doc
	cp doc/starch.css $(CGIINSTDIR)/doc

install: install-starch install-cgi

jar:
	jar -cvf starch.jar EDU/bmrb/starch/*.java EDU/bmrb/starch/*.class \
	testfiles/* doc/* *.*

javadoc:
#	javadoc -d doc/javadoc -version -author -doctitle "STARch documentation" EDU.bmrb.starch
	javadoc -d doc/javadoc -version -author EDU.bmrb.starch

javadoc-install:
	- mkdir -p $(JAVADOCINSTDIR)
	cp -R doc/javadoc/* $(JAVADOCINSTDIR)

clean:
	rm EDU/bmrb/starch/*\~
	rm doc/*\~
	rm *\~
	rm starch.cgi

