# FileStorage

## About The Project

File storage is just a folder that has config.json (information about size and restrictions in that storage) and users.json which acts like database for storage. Program is implemented to work with local machine or Google drive depending on how it was packaged (built). Additionally, to use Google drive you have to change token for accessing drive. 

## Build process

Program can be compiled and run from IDE or directly from command-line/terminal. To run it from command-line/terminal follow these steps:


## Usage

Program is used through command line interface. First step is to create a storage using ns command and absolute path which will be the location of the storage. After that, storage size, restriction, username and password, for the root user, will be required. Once the storage is created, all of the down commands are available for use.

Notation: 

- parentheses (required)
- square brackets [optional]


### Command line operations:

- list - list of all available operations
- ns (path) - creates a new storage (path is absolute path)
- path (path to storage) - absolute path to storage
- mkdir (folderName) [number of folders] - makes one or more new directories
- mkdir -res (folderName) (restriction) [number of folders] - makes one or more directories with limited number of folders and files in it
- touch (fileName) - makes new file
- cd (fileName) - change current directory to fileName
- cd.. - goes one step back from current directory
- pwd - print the name of the current working directory
- rm (fileName) - removes file or directory
- logout - disconnect the user (to connect again type path <path to storage>)
- sort (asc or desc) [] - sort files in current directory
- ls [filter] - list files in current directory ( filter can be .txt or .jpeg or anything after dot (.) )
- exit - exits from application

## Config and User example
  ![config.json](Pictures/config.png)
  ![users.json](Pictures/users.png)

## TODO:

- [ ]  Fix move method in google drive (to go up in hierarchy)
- [ ]  Fix privilege for creating directory and file in google drive

## Code Contributors
