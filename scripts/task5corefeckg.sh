#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
TRIGPATH="/Users/piek/Desktop/SemEval2018/trial_data_final/NAFDONE"
CONLLPATH="/Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/CONLL"
THRESHOLD="2"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5EventCoref -trig-files TRIGPATH --conll-files $CONLLPATH --event-file trial_vocabulary