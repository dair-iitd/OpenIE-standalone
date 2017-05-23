# OpenIE-5.0

This project contains the principal Open Information Extraction (Open IE) system from the University of Washington (UW). An Open IE system runs over sentences and creates extractions that represent relations in text. For example, consider the following sentence.

The U.S. president Barack Obama gave his speech on Tuesday to thousands of people.

There are many binary relations in this sentence that can be expressed as a triple (A, B, C) where A and B are arguments, and C is the relation between those arguments. Since Open IE is not aligned with an ontology, the relation is a phrase of text. Here is a possible list of the binary relations in the above sentence:

(Barack Obama, is the president of, the U.S.)
(Barack Obama, gave, his speech)
(Barack Obama, gave his speech, on Tuesday)
(Barack Obama, gave his speech, to thousands of people)

The first extraction in the above list is a "noun-mediated extraction", because the extraction has a relation phrase is described by the noun "president". The other extractions are very similar. In fact, they can be represented more informatively as an n-ary extraction. An n-ary extraction can have 0 or more secondary arguments. Here is a possible list of the n-ary relations in the sentence:

(Barack Obama, is the president of, the U.S.)
(Barack Obama, gave, [his speech, on Tuesday, to thousands of people])

Extractions can include more than just the arguments and relation as well. For example, we might be interested in whether the extraction is a negative assertion or a positive assertion, or if it is conditional in some way. Consider the following sentence:

Some people say Barack Obama was born in Kenya.

We would not want to extract that (Barack Obama, was born, in Kenya) alone because this is not true. However, if we have the condition as well, we can have a correct extraction.

Some people say:(Barack Obama, was born in, Kenya)
