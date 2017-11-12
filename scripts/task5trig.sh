#!/usr/bin/env bash

#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
QUESTIONPATH=$1
TRIGPATH=$2
CONLLPATH=$3
THRESHOLD=S4

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Trig --question $QUESTIONPATH -trig-files TRIGPATH --conll-files $CONLLPATH --triple-threshold $THRESHOLD