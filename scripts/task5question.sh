#!/usr/bin/env bash

## Needs two parameters: path to a question JSON file, task id and path to the ECKG generate by NWR
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
QUESTIONPATH=$1
TASK=$2
ECKG=$3


java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Question --question --task $TASK $QUESTIONPATH -eckg $ECKG
