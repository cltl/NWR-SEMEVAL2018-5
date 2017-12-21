#!/usr/bin/env bash
## Needs two parameters: path to a question JSON file, task id and path to the ECKG generate by NWR
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target
SUBMISSION=$1

QUESTIONPATH="/Users/piek/Desktop/SemEval2018/trial_data_final/input"
ECKG="/Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/eckg"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Counting --question $QUESTIONPATH/s1/questions.json --eckg-files $ECKG --subtask "s1"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Counting --question $QUESTIONPATH/s2/questions.json --eckg-files $ECKG --subtask "s2"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5Counting --question $QUESTIONPATH/s3/questions.json --eckg-files $ECKG --subtask "s3"

cp $QUESTIONPATH/s3/docs.conll.result $SUBMISSION/s1/docs.conll
cp $QUESTIONPATH/s3/docs.conll.result $SUBMISSION/s2/docs.conll
cp $QUESTIONPATH/s3/docs.conll.result $SUBMISSION/s3/docs.conll

cp $QUESTIONPATH/s1/answers.json $SUBMISSION/s1/
cp $QUESTIONPATH/s2/answers.json $SUBMISSION/s2/
cp $QUESTIONPATH/s3/answers.json $SUBMISSION/s3/

cd $SUBMISSION
zip -r submission.zip *