#!/usr/bin/env bash
## Needs two parameters: path to a question JSON file, task id and path to the ECKG generate by NWR
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
#LIB="$ROOT"/target
LIB="/Code/vu/newsreader/nwr-semeval2018-5/target"
SUBMISSION=$1
INPUT=$2
ECKG=$3
PERIOD=$4
#SUBMISSION="/Users/piek/Desktop/SemEval2018/test_data/submission"
#QUESTIONPATH="/Users/piek/Desktop/SemEval2018/test_data/input"
#ECKG="/Users/piek/Desktop/SemEval2018/test_data/eckg"
#PERIOD="weekend"

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5CountingVersion4 --question $INPUT/s1/questions.json --eckg-files $ECKG --subtask "s1" --cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel --period $4

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5CountingVersion4 --question $INPUT/s2/questions.json --eckg-files $ECKG --subtask "s2" --cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel --period $4

java -Xmx2000m -cp "$LIB/nwr-semeval2018-5-1.0-SNAPSHOT-jar-with-dependencies.jar" task5.Task5CountingVersion4 --question $INPUT/s3/questions.json --eckg-files $ECKG --subtask "s3" --cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel --period $4

cp $INPUT/s3/docs.conll.result $SUBMISSION/s1/docs.conll
cp $INPUT/s3/docs.conll.result $SUBMISSION/s2/docs.conll
cp $INPUT/s3/docs.conll.result $SUBMISSION/s3/docs.conll

cp $INPUT/s1/answers.json $SUBMISSION/s1/
cp $INPUT/s2/answers.json $SUBMISSION/s2/
cp $INPUT/s3/answers.json $SUBMISSION/s3/

cd $SUBMISSION
zip -r submission.zip *
