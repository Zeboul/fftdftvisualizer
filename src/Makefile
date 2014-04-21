# This was useful until I realized your could just do "javac Main.java" and
# the compiler automatically compiles dependencies
RM=rm -fv
JAVAC=javac
JFLAGS=-Xlint:unchecked
CLASSES=Main.class

all: $(DEP_CLASSES) $(CLASSES)

%.class: %.java
	$(JAVAC) $(JFLAGS) $<

clean:
	-$(RM) *.class WavFile/*.class
