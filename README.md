# NWR-SEMEVAL2018-T5
NewsReader participation to task 5 of SemEval2018

Our goal is to build a wrapper around the NewsReader system to participate in the task.
The goal of the task is to identify events that fit the constraints of a question and to provide 3 types of answers:

1. the number of incidents that fit the constraints
2. the documents that report on these incidents
3. the mentions of the events that report on these incidents and their coreference relations

We will participate to all 3 tasks in a principled manner. Our approach is as follows:

1. Select all documents that come with a question (each question contains a list of document identifiers)
2. Convert the documents to the NewsReader NAF format
3. Process the documents with the NewsReader event detection pipeline
4. Convert the results to RDF-SEM
5. Load the results in a KnowledgeStore (KS)
6. Map the query constraints to SPARQL request for the KS
7. Query the KS with Sparql and process the results
8. Convert the results to the answer format required for the task

Different version of the system will:

1. improve the NLP components for event detection
2. improve the reasoning over the output of the SPARQL requests


Overall strategy version 1

Preprocessing
- Read conll and generate NAF files with tokens
- NWR pipeline for final NAF and naf2sem for RDF TRiG

Question processing:
- Read all RDF TRiG files
- Select events of the correct type
- merge events with sharing object
Read question file



