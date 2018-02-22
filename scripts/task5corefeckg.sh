#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
#LIB="$ROOT"/lib
LIB="/Code/vu/newsreader/nwr-semeval2018-5/target"
TRIGPATH=$1
CONLLPATH=$2
ECKG=$3
THRESHOLD=$4
PERIOD=$5

#./task5crossevent-coref.sh ../trial_data_final/NAFDONE.ALL ../trial_data_final/input/s3/docs.conll 1

#TRIGPATH="/Users/piek/Desktop/SemEval2018/trial_data_final/NAFDONE"
#CONLLPATH="/Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/CONLL"
#THRESHOLD="2"

java -Xmx2500m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5EventCorefVersion4 --trig-files $TRIGPATH --conll-file $CONLLPATH --eckg $ECKG --event-file /Users/piek/Desktop/SemEval2018/scripts/trial_vocabulary --cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel --threshold $THRESHOLD --period $PERIOD