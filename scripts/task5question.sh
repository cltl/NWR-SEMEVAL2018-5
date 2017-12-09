#!/usr/bin/env bash

## Needs two paramters: path to a question JSON file and path to the ECKG generate by NWR
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
QUESTIONPATH=$1
ECKG=$5

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Question --question $QUESTIONPATH -eckg $ECKG
