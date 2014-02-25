#!/bin/sh
# EXIT STATUS:
#	 3 - Generic error
#	 4 - Source doesn't exist
#	 5 - Can't read source file or can't write on directory
#	 6 - Destination file already exists

function printErr () {
	# Exits with status, and prints it (to ease reading it with superuser)
	echo $1
	exit $1
}

# Check number of parameters
if test $# -ne 2
then
	printErr 3
fi

# Check source
if ! test -f "$1"
then
	printErr 4
fi

# Check permissions
if ! test -r "$1" -a -x "${1%/*}"
then
	printErr 5
fi

# Check destination
if test -e "$2"
then
	printErr 6
fi

# Rename file
mv "$1" "$2"

# Check result
if ! test -e "$2"
then
	printErr 3
fi

# Passed all test, the move has been completed successfully
printErr 0