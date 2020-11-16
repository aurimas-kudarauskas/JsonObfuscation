# JsonObfuscation
Simple program which replaces strings in json files with utf codes

Project was build with NetBeans IDE 12.1 and Java jdk-15.0.1
Project compiled with maven

Project has the following dependencies:
  apache commons-lang3-3.11
  apache commons-text-1.9
Both dependences included in build directory under lib subdirectory

In windows application shal be run with the following command:

java -jar JsonObfuscator-1.jar fileToConvert

This application meant to obfuscate any string in provided json file.

Example of application usage: JsonObfuscator InputJsonFile

Additional options for application:

    JsonObfuscator InputJsonFile -o outputFileName - specifie filename where obfuscated json should be saved
    JsonObfuscator InputJsonFile -m mapFileName - specifie filename where map of obfuscated strings should be saved. Default "mapping.txt"
    JsonObfuscator -h - print this help text
