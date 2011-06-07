#!/bin/bash

# trying to fix the out of mem error here
for i in `"ls" dist/lib`; do CLASSPATH="$CLASSPATH:dist/lib/$i"; done
CLASSPATH="dist/j-maanova-1.1.1.jar$CLASSPATH"

J_MAANOVA_CMD="$JAVA_HOME/bin/java -Xmx2g -enableassertions \
-Djava.util.logging.config.class=org.jax.util.ResourceBasedLoggerConfiguration \
-Djava.library.path=/Users/kss/.j-maanova/1.1.1/jri-natives/Mac/r-2.13:.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java \
-cp $CLASSPATH org.jax.maanova.Maanova"

# print the command
echo "R_HOME=/Library/Frameworks/R.framework/Versions/2.13/Resources $J_MAANOVA_CMD"

# run the command
R_HOME=/Library/Frameworks/R.framework/Versions/2.13/Resources $J_MAANOVA_CMD
