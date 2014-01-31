GermplasmStudyBrowser
============

Overview
----------
The GermplasmStudyBrowser tools provide a user-friendly interface for browsing study and cross-study information.
The Study Browser allows user to browse and export details of existing program and public studies.
The Head to Head Comparison tool compares performance of germplasm pairs for selected traits and environments.
The Query for Adapted Germplasm helps find germplasms that are most suitable for specific environmental conditions.

These tools make use of our Middleware API to connect to public and program databases.

Prerequisites
---------------
Build and install IBPMiddleware and IBPCommons using one of the following methods:
  1.  Using the command line, go the IBPMiddleware/IBPCommons home directory, run the command: <pre>mvn clean install</pre>
  2.  From within Eclipse, right-click on the project, IBPCommons for instance, select Run As --> Maven build..., then input the target <pre>clean install</pre>
  
Note: Please see build instructions for the IBPMiddleware for more information.  

To Build
----------
To build the GermplasmStudyBrowser project using the command line, issue the following commands in the GermplasmStudyBrowser directory:
  1.  To create a clean build and run the test code: mvn clean package

To Run Tests
--------------
To run junit tests using the command line, issue the following commands in the GermplasmStudyBrowser directory:
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
  	* In the GermplasmStudyBrowser directory, go to pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.
  	
  	Running via command line
  	* From the command line, go to the GermplasmStudyBrowser folder
  	* Execute the ff: <pre>mvn tomcat7:run</pre>

To Access Product
-------------------
Below are the URLs to access the GermplasmStudyBrowser tools:
  1.  Study Browser - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/GermplasmStudyBrowser/main/study/
  2.  Head to Head Comparison http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/GermplasmStudyBrowser/main/Head_to_head_comparison/
  3.  Query for Adapted Germplasm - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/GermplasmStudyBrowser/main/Query_For_Adapted_Germplasm/

Other Helpful Resources
-------------------------
To setup remote debugging:
  1.  In Windows: set <pre>MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n</pre>
  2.  Go to IDE, setup remote debugging application, choose socket attach and input the correct port number.
  3.  Happy debugging