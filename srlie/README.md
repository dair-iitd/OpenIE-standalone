** **DEPRECATED!** ** Please see https://github.com/dair-iitd/OpenIE-standalone, which has combined multiple projects into a single project and maintains the latest version of Open IE (Open IE 5). It is based on another repository https://github.com/allenai/openie-standalone,  which has an older version of Open IE.

SRLIE
=====
SRLIE is a component of Open IE 4.x that automatically identifies n-ary extractions from English sentences.
SRLIE is designed for Web-scale information extraction, where target relations are not specified in advance.

SRLIE constructs richer extractions than OLLIE.  It builds extractions from Semantic Role Labelling (SRL)
SRL frames are not extractions themselves because they contain many frame arguments that are not considered
extraction arguments in Open IE.  SRL also does not identify argument boundaries or expand relation verbs
into relation phrases.

## Process

SRLIE is composed of a layer of functions on the input sentence.

1.  First the sentences is processed by a dependency parser.
2.  Next the dependency graph is run through a SRL system to produce SRL Frames.
3.  Then the frames are sent through SRLIE to produce n-ary extractions.  This involves filtering some SRL frames, determing argument boundaries, and constructing a relation phrase.
 1.  Optionally the n-ary extractions can be sent through a conversion to produce triples.
 2.  Optionally the n-ary extractions can be sent through a conversion to produce nested n-ary extractions.

## Concurrency

When operating at web scale, parallelism is essential.  While the base SRLIE extractor is immutable and
thread safe, the underlying SRL system provided by ClearNLP is not threadsafe. 
Version 1.0.3 onwards it is thread-safe. Works well with java-7-openjdk.

## Citing SRLIE

Janara Christensen, Mausam, Stephen Soderland, Oren Etzioni. "An Analysis of Open Information Extraction based on Semantic Role Labeling". International Conference on Knowledge Capture (KCAP). Banff, Alberta, Canada. June 2011.
