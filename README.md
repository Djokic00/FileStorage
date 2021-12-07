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

- <b>list</b> - list of all available operations
- <b>ns (path)</b> - creates a new storage (path is absolute path)
- <b>path (path to storage)</b> - absolute path to storage
- <b>mkdir (folderName) [number of folders]</b> - makes one or more new directories
- <b>mkdir -res (folderName) (restriction) [number of folders]</b> - makes one or more directories with limited number of folders/files in it
- <b>touch (fileName)</b> - makes new file
- <b>cd (fileName)</b> - change current directory to fileName
- <b>cd..</b> - goes one step back from current directory
- <b>pwd</b> - print the name of the current working directory
- <b>rm (fileName)</b> - removes file or directory
- <b>logout</b> - disconnect the user (to connect again type path <path to storage>)
- <b>sort (asc or desc) [date or size]</b> - sort files in current directory, if optional argument is empty it sort files alphabetically
- <b>ls [filter]</b> - list files in current directory ( filter can be .txt or .jpeg or anything after dot (.) )
- <b>exit</b> - exits from application

 ## Config.json & Users.json
  ![config.json](Pictures/config.png)
  ![users.json](Pictures/users.png)

## TODO:

- [ ]  Fix move method in google drive (to go up in hierarchy)
- [ ]  Fix privilege for creating directory and file in google drive

## Code Contributors
