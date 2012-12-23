#! /bin/sh
#leave the space above, maven resources plugin bug - MRESOURCES-110

THIS="$0"
DIR=`dirname "$THIS"`
XSM_HOME=`cd "$DIR/.."; pwd`
cd $XSM_HOME

JAVA_EXEC=$JAVA_HOME/bin/java
if [ !  -x $JAVA_EXEC ]; then

  JAVA_EXEC=`which java`
  if [ -z $JAVA_EXEC ] || [ !  -x $JAVA_EXEC ]; then
    echo "Error: Unable to find java - please set JAVA_HOME or add java to your PATH"
    exit
  fi

fi

JAVA_OPTS="-Xmx512m -Djava.awt.headless=true -Duser.timezone=UTC"

$JAVA_EXEC $JAVA_OPTS -jar bin/xsm-bin.jar $XSM_HOME/xsm.war $@
