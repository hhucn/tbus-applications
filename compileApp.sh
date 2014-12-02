#!/bin/bash

# Application source file is our only parameter
if [[ $# -ne 1 || "${1}" = "" ]]
then
	echo "${0} - Compiles the given Java code into a jar"
	echo -e "\tand includes VSimRTI-libraries into the classpath"
	echo "(c) 2014 Raphael Bialon <Raphael.Bialon@hhu.de>"
	echo "Usage: ${0} File.java"
	echo -e "\tFile.java - the given Java code"
fi

function error() {
	echo "Something went wrong, aborting..."
	exit 1
}

progName=$(basename "${1}" .java)
#packageDirs=$(grep -m1 "package" "${progName}.java" | awk '{print $2}' | sed 's/\./\//g' | sed 's/\/[a-zA-Z0-9]*;//g')
packageDirs=$(grep -m1 "package" "${progName}.java" | awk '{print $2}' | sed 's/\./\//g' | sed 's/;//')
topPackageDir=$(echo ${packageDirs} | sed 's/\/.*//')

# Remove old jar file because of class path errors
if [ -f "${progName}.jar" ]
then
	rm "${progName}.jar" || error
fi

echo -n "Building class path... "
if [ ! -f "classpath.txt" ]
then
	classPath=$(for f in $(for i in $(locate "*.jar"); do grep -Hlsi dcaiti/vsimrti/fed/app ${i}; done); do echo -n ${f}:; done)
	echo "${classPath}" > classpath.txt
fi
classPath=$(cat classpath.txt)
echo "Finished."

echo $packageDirs

echo -n "Compiling... "
javac -cp "${classPath}" "${progName}.java" || error
echo "Finished."

echo -n "Creating jar... "
mkdir -p "./${packageDirs}" || error
mv "${progName}.class" "./${packageDirs}/" || error
jar -cf "${progName}.jar" "${topPackageDir}" || error
echo "Finished"

echo -n "Cleaning up... "
rm -r "./${topPackageDir}" || error
echo "Finished."
