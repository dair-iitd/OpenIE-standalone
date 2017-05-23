Open IE 5.0
===================


This project contains the principal Open Information Extraction (Open IE) system from the University of Washington (UW) and Indian Institute of Technology, Delhi. An Open IE system runs over sentences and creates extractions that represent relations in text. For example, consider the following sentence.

> The U.S. president Barack Obama gave his speech on Tuesday to thousands of people.

There are many binary relations in this sentence that can be expressed as a triple (A, B, C) where A and B are arguments, and C is the relation between those arguments. Since Open IE is not aligned with an ontology, the relation is a phrase of text. Here is a possible list of the binary relations in the above sentence:

>- (Barack Obama, is the president of, the U.S.)
>- (Barack Obama, gave, his speech)
>- (Barack Obama, gave his speech, on Tuesday)
>- (Barack Obama, gave his speech, to thousands of people)
