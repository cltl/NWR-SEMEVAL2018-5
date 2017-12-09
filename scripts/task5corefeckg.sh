#!/usr/bin/env bash

#Reads RDF-TRiG files and generates the ECKG and CoNLL output file for event coreference
#Takes as parameters: path to the trig files, path to the CoNLL file and threshold for event coreference

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
TRIGPATH=$1
CONLLPATH=$2
THRESHOLD=$3


java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5EventCoref -trig-files TRIGPATH --conll-files $CONLLPATH --triple-threshold $THRESHOLD
