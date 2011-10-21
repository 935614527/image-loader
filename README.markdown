=========================================================
General information
=========================================================
ImageLoader component
- core contains a java maven based project
- demo mvn android based project to test imageLoader
- acceptance mvn android based project with robotium test
- monkey some example that take a snapshot using monkey runner

For client project that need to include the image loader you just need the jar
you can find the jar at : https://github.com/novoda/public-mvn-repo 


=========================================================
How to use the library
=========================================================

If you are using maven you need to define the repo and then the dependency
<repositories>
  <repository>
    <id>public-mvn-repo-releases</id>
    <url>https://github.com/novoda/public-mvn-repo/raw/master/releases</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.novoda.imageloader.core</groupId>
  <artifactId>imageloader-core</artifactId>
  <version>1.3.3</version>
</dependency>

=========================================================
Working with eclipse
=========================================================

better to use eclipse 3.7 and maven 3.0.3
import core as maven project
for acceptance and demo :
- run : mvn clean initialize
- create a new android project in eclipse
- add the necessary jars from the lib folder

Note : build support sdk 14 as is using the new android-maven-plugin:3.0.0-alpha13
