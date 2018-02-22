# NWR-SEMEVAL2018-T5
NewsReader participation to task 5 of SemEval2018
Version 4
Contact: piek.vossen@vu.nl

Our goal is to build a wrapper around the NewsReader system to participate in the task.
The goal of the task is to identify events that fit the constraints of a question and to provide 3 types of answers:

1. the number of incidents that fit the constraints
2. the documents that report on these incidents
3. the mentions of the events that report on these incidents and their coreference relations

We will participate to all 3 tasks in a principled manner. Our approach is as follows:

1. Preprocessing using the NewsReader system:
- Read CoNLL and generate NAF files with tokens according to the CoNLL file
- NWR pipeline for final NAF and naf2sem for RDF TRiG

The output of this step is provided in the data folder:

data\newsreader_eckg_raw.tar.gz

2. Resolving event coreference giving incident - document clusters
- Read all RDF TRiG files and store the Event-Centric-Knowledge-Graphs (ECKGs) in memory
- Select events of the correct type (shooting, burning and dismissals) and group them by the assumed event date.
- Establish the incident - document relations by comparing the ECKGs for the complete document within the same type of event and the same date.
- Create ECKGs by aggregating  properties across documents that are associated with the same incident and that share sufficient properties for event coreference
- Store the task adapted ECKGs on disk grouped by type of event and the event date
- Annotate the tokens in the CoNLL file with event identifiers, estbalishing event coreference across mentions and across documents according to the adapted ECKGs.

The adapted ECKGs for the test data can be found in:

data\newsreader_submission_eckg_4_dom_day_loc__m1.tar.gz

Use the script "task5corefeckg.sh" to derive the task adapted ECKGs from the raw NewsReader ECKGs.
The script requires 5 parameters: 

TRIGPATH=$1 (the path to the folder with the raw NewsReader ECKG files in TRiG-RDF format, wherever you unpack data\newsreader_eckg_raw.tar.gz)
CONLLPATH=$2 (the path to the Task 5 CoNLL file as input for the event coreference annotation)
ECKG=$3 (the folder to store the adapted ECKGs)
THRESHOLD=$4 (threshold for the number of locations and participants that need to match across documents to be associated with the same incident)
PERIOD=$5 (the granularity of the period of events considered for identity: document-creation-time, the event day, event weekend, event week)

Some hard-coded parameters are the related cities (cities.rel) and states (states.rel) according to DBPedia and a lexicon of event mentions taken from the trial data.

3. Answering the subtask queries reasoning and counting on the basis of the aggregated task adapted ECKGs

- Read all the task adapted ECKGs created by step 2 and store them in memory
- For each task, read the question.json file, derive the question constraints for each question and match the constraints with the ECKG data in memory.
- We create the answer data on the basis of the semantic match of the query with the ECKGs and counting the number of incidents and extracting the associated document.
- We apply a specific reasoning for subtask 3 to count the number of killed and injured.

Use the script "task5question.sh" to answer the specific task questions. It takes the following input parameters:

SUBMISSION=$1 (the folder to store the result of the each task)
INPUT=$2 (the folder with the tasks)
ECKG=$3 (the folder with the task-adapted ECKGs)
PERIOD=$4 (the granularity of the period of events to be considered)

Some hard-coded parameters are: the related cities (cities.rel) and states (states.rel) and the specific subtask: s1, s2, and s3.





