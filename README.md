# duplication finder

Scans a given directory and checks for duplicated directories and files using the size of the files and checksums
calculated from their contents.

* The hash value of a file is the hash calculated from the actual content of the file.
* The hash value of a directory is the hash calculated by taking the list of all directories and files inside it,
  ordering them to achieve permutation invariance and then hashing the resulting sorted list.

Files and directories are considered to be duplicates only if they match in their size and hash value.

The program produces json files containing:

* The index build from the given directory to scan.
* A file containing all found duplicated directories.
* A file containing all found duplicated files.