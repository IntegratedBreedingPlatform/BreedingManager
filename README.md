BreedingManager
============

Overview
----------
The Breeding Manager tools provide a user-friendly interface for breeding activities such as importing germplasms, 
germplasm list management and making germplasm crosses. The List Manager allows the building, editing and browsing of germplasm lists.
The Crossing Manager allows the creation of germplasm crosses either manually or by uploading a file. 
The Germplasm Import tool allows creation of germplasm lists by importing existing or creating new germplasms.
The Nursery Template Wizard allows creation of templates for making crosses.
These tools make use of our Middleware API to connect to public and program databases.

Prerequisites
---------------
Build and install IBPMiddleware and IBPCommons using one of the following methods:
  1.  Using the command line, go the IBPMiddleware/IBPCommons home directory, run the command: <pre>mvn clean install</pre>
  2.  From within Eclipse, right-click on the project, IBPCommons for instance, select Run As --> Maven build..., then input the target <pre>clean install</pre>

Note: Please see build instructions for the IBPMiddleware for more information.

To Build
----------
 1.  To create a clean build and run the test code: 
   <pre>mvn clean package</pre>
 2. To build using a specific configuration, run the following:
  <pre>mvn clean package-DenvConfig=release</pre>
  In this example, it is expected that pipeline/config/release folder exists and the database configuration files (workbench.properties) are properly set.


To Run Tests
--------------
To run junit tests using the command line, issue the following commands in the BreedingManager directory:
  1.  To run all tests: <pre>mvn clean test</pre>
  2.  To run a specific test class: <pre>mvn clean test -Dtest=TestClassName</pre>
  3.  To run a specific test function: <pre>mvn clean test -Dtest=TestClassName#testFunctionName</pre>
 
To Deploy
-----------
  1.  Deploy the code after the build.
  2.  Or run via command line:
  
    Configuration
  	* Go to your maven installation, you can find your installation directory by typing in DOS "mvn -version", this should show you the MVN installation information
  	* Go to the Maven installation directory conf/settings.xml, modify as necessary and set the profiles.
  	* In the BreedingManager directory, go to pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.
  	
  	Running via command line
  	* From the command line, go to the BreedingManager folder
  	* Execute the ff: <pre>mvn tomcat7:run</pre>

To Access Product
-------------------
Below are the URLs to access the BreedingManager tools:
  1.  List Manager - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/BreedingManager/main/list-manager/
  2.  Crossing Manager - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/BreedingManager/main/crosses/
  3.  Germplasm Import - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/BreedingManager/main/germplasm-import/
  4.  Nursery Template Wizard - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/BreedingManager/main/nursery-template/
  
Other Helpful Resources
-------------------------
To setup remote debugging:
  1.  In Windows: set <pre>MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n</pre>
  2.  Go to IDE, setup remote debugging application, choose socket attach and input the correct port number.
  3.  Happy debugging