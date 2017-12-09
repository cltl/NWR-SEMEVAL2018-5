
#!/usr/bin/env bash

# Reads a CoNLL file and extracts NAF files with raw text and token layer.

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
CONLLPATH=$1


######
java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" ConllNafConversion --file $CONLLPATH

