#!/bin/bash

# trying to fix the out of mem error here
for i in `"ls" dist/lib`; do CLASSPATH="$CLASSPATH:dist/lib/$i"; done
CLASSPATH="dist/j-maanova-1.1.1.jar$CLASSPATH"
echo $CLASSPATH
$JAVA_HOME/bin/java -Xmx2g -enableassertions -Djava.util.logging.config.class=org.jax.util.ResourceBasedLoggerConfiguration -cp $CLASSPATH org.jax.maanova.MaanovaLauncher
