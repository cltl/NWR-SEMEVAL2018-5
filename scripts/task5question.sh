#!/usr/bin/env bash
## Needs two parameters: path to a question JSON file, task id and path to the ECKG generate by NWR
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
SUBMISSION=$1

QUESTIONPATH1="/Users/piek/Desktop/SemEval2018/trial_data/input/s1"
QUESTIONPATH2="/Users/piek/Desktop/SemEval2018/trial_data/input/s2"
QUESTIONPATH3="/Users/piek/Desktop/SemEval2018/trial_data/input/s3"
ECKG="/Users/piek/Desktop/SemEval2018/trial_data/input/s3/eckg"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Question --question $QUESTIONPATH1/questions.json -eckg $ECKG --task "s1"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Question --question $QUESTIONPATH2/questions.json -eckg $ECKG --task "s2"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Question --question $QUESTIONPATH3/questions.json -eckg $ECKG --task "s3"

cp $QUESTIONPATH3/docs.conll.result $SUBMISSION/s1/docs.conll
cp $QUESTIONPATH3/docs.conll.result $SUBMISSION/s2/docs.conll
cp $QUESTIONPATH3/docs.conll.result $SUBMISSION/s3/docs.conll

cp $QUESTIONPATH1/answers.json $SUBMISSION/s1/
cp $QUESTIONPATH2/answers.json $SUBMISSION/s2/
cp $QUESTIONPATH3/answers.json $SUBMISSION/s3/

cd $SUBMISSION
zip -r submission.zip *
