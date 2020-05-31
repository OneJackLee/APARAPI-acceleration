# APARAPI-acceleration

Eduard is a cross-platform-supported software designed to generate shaded relief images. \
Aparapi is a JAVA API which can perform General-purpose computing on Graphics Processing Units (GPGPU). \
The project is intended to use APARAPI to accelerate the processes of generating the raster filter


### Eduard
The original software which produce the shaded relief images. It used Java thread algorithm.

### APARAPI
The technology which used to accelerate the process.

## Download the project
There are multiple ways to download the project.
* use any JAVA-supported IDE to clone the project
* open the command prompt or terminal and execute the command `git clone link_of_this_repo` (required git installed)
* directly download this project from gitHub.

## Build project
To compile the executable JAR file, 
1. open the command prompt or terminal
2. navigate to the `APARAPI-acceleration` parent folder
3. execute the command `mvn package`
## Execute software
The executable JAR file will be located at `target` folder (in the `APARAPI-acceleration` parent folder).\
Double click the JAR `APARAPI-acceleration-1.0-SNAPSHOT-jar-with-dependencies.jar` to execute the software.
#### _**Running**_
After executing the software, 
1. A pop out window will shown up to prompt the input raster file `*.asc`.
2. It will process the raster file and generate the raster file
3. A pop out window will shown up to prompt the output location of the filtered raster file `*.asc`.
4. A pop out window will shown up to prompt the output location of the filtered image file `*.png`.
5. A pop out window will shown up to prompt the output location of the performance benchmark file `*.txt`.
