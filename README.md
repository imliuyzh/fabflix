<img src="demo.gif">

# Fabflix
Please visit the [wiki](https://github.com/imliuyzh/fabflix/wiki) for more information about this project.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. Alternatively, you can always use the link above to run the application.

### Running the Application
The only supported environment is Ubuntu v20.04+, Tomcat v9.0+, MySQL v8.0+, Java v11+, and IntelliJ IDEA.

#### Install Java and Maven
1. `sudo apt update`
2. `sudo apt install openjdk-11-jdk maven`

#### Install MySQL
1. ```sudo apt update```
2. ```sudo apt install mysql-server```
3. ```sudo mysql_secure_installation```
4. Press "Y"
5. Set the password and keep pressing "Y"
6. `mysql -u root -p`
7. `CREATE USER 'testuser'@'localhost' IDENTIFIED WITH mysql_native_password BY '122Baws@ICS';`
8. `GRANT ALL PRIVILEGES ON * . * TO 'testuser'@'localhost';`
9. `quit`

##### Create the `moviedb` Database
1. Fetch data files
   + `cd` into `src/main/sql`
   + `wget https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs122b-2019-winter-project1/movie-data.sql`
   + `cd` into `src/main/java/com/flixster/xml/parser`
   + `wget http://infolab.stanford.edu/pub/movies/mains243.xml`
   + `wget http://infolab.stanford.edu/pub/movies/actors63.xml`
   + `wget http://infolab.stanford.edu/pub/movies/casts124.xml`
   + `cd` back into project root directory
2. `mysql -u testuser -p`
3. `\. src/main/sql/createtable.sql`
4. `\. src/main/sql/movie-data.sql`
5. `\. src/main/sql/addvalues.sql`
6. `quit`
7. `mvn clean`
8. `mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.flixster.password.UpdateSecurePassword"`
9. `mvn clean`
10. `mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.flixster.xml.parser.MainsParser"`
11. `mvn clean`
12. `mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.flixster.xml.parser.ActorsParser"`
13. `mvn clean`
14. `mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.flixster.xml.parser.CastsParser"`
15. `mysql -u testuser -p`
16. `\. src/main/sql/stored-procedure.sql`
17. `\. src/main/sql/createindexes.sql`
18. `quit`
19. Set up FLAMINGO Toolkit to enable efficient fuzzy search
    + `wget http://flamingo.ics.uci.edu/toolkit/toolkit_2021-05-18.tgz`
    + Unpack the file
    + `sudo apt install gcc make mysql-server libmysqlclient-dev`
    + `cd` into the unpacked folder
    + `make`
    + `sudo cp libed*.so /usr/lib/mysql/plugin/`
    + `sudo service mysql restart`
    + `mysql -u root -p`
    + `DROP FUNCTION IF EXISTS ed;`
    + `CREATE FUNCTION ed RETURNS INTEGER SONAME 'libed.so';`
    + `DROP FUNCTION IF EXISTS edrec;`
    + `CREATE FUNCTION edrec RETURNS INTEGER SONAME 'libedrec.so';`
    + `DROP FUNCTION IF EXISTS edth;`
    + `CREATE FUNCTION edth RETURNS INTEGER SONAME 'libedth.so';`

#### Install Tomcat
1. ```sudo apt update```
2. ```sudo apt install tomcat9 tomcat9-admin```
3. Wait for a second and you should see Tomcat is on port 8080: `ss -ltn`
4. ```sudo ufw allow from any to any port 8080 proto tcp```
5. Go to http://127.0.0.1:8080 and you should see the "It works!" page
6. `sudo vim /etc/tomcat9/tomcat-users.xml`
7. Add this block before `</tomcat-users>` and remember to change the password
    ```
    <role rolename="admin-gui"/>
    <role rolename="manager-gui"/>
    <user username="tomcat" password="your_password" roles="admin-gui,manager-gui"/>
    ```
8. `sudo systemctl restart tomcat9`
9. Wait and put in the username and password in http://127.0.0.1:8080/manager/html
   + You should see the Web Application Manager afterward

#### Setup IDE on the development machine
1. Clone Fabflix into your device: ```git clone https://github.com/UCI-Chenli-teaching/cs122b-spring21-team-20.git```
2. Create `Keys.java` in `src/main/java` and put in your reCAPTCHA secret key and TMDb API key
   + Follow [this](https://morweb.org/support-post/set-up-google-recaptcha) to set up reCAPTCHA
     + Remember to add "localhost" into the domains
   + You need to register a TMDb account and follow [this](https://www.themoviedb.org/documentation/api) to get an API key from TMDb
    ```
    public class Keys 
    {
        public static final String RECAPTHCA_SECRET_KEY = "";
        public static final String POSTER_SECRET_KEY = "";
    }
    ```
3. Follow the instructions [here](https://github.com/imliuyzh/fabflix/wiki/Project-1:-Setup-AWS,-MySQL,-JDBC,-Tomcat,-Start-Fabflix#setup-ide-on-the-development-machine) (Ignore "Create Project in IntelliJ")

## Built with
### Front end
+ Bootstrap
+ jQuery
  + Ajax Autocomplete for jQuery
+ Line Awesome

### Back end
+ Apache Tomcat
  + Java Servlet
+ Gson
+ Jasypt
+ MySQL
  + FLAMINGO Toolkit
+ OkHttp
+ The Movie Database (TMDb)

### Others
+ Adobe Photoshop
+ Amazon Elastic Compute Cloud (EC2)
+ GIMP
+ Git
+ Google Cloud Platform
  + reCAPTCHA
+ IntelliJ IDEA
+ Maven
+ Ubuntu

## Contributors
Item | Contributor
------------ | -------------
Autocomplete | imliuyzh
Checkout Back End | AnonymousAnteater, imliuyzh
Checkout Front End | imliuyzh, AnonymousAnteater
Dashboard | imliuyzh
HTTPS | AnonymousAnteater, imliuyzh
Full-Text & Fuzzy Search | imliuyzh, AnonymousAnteater
Login Back End | imliuyzh, AnonymousAnteater
Login Front End | imliuyzh
Login Filter |imliuyzh
Movie Page Back End |imliuyzh, AnonymousAnteater
Movie Page Front End | imliuyzh, AnonymousAnteater
Password Encryption | AnonymousAnteater
Portrait | imliuyzh, AnonymousAnteater
Poster | AnonymousAnteater, imliuyzh
reCAPTCHA | imliuyzh
Search Front End | imliuyzh
Search Back End |imliuyzh, AnonymousAnteater
SQL Scripts | AnonymousAnteater, imliuyzh
Star Page Front end | imliuyzh
Star Page Back End |imliuyzh, AnonymousAnteater
XML Parsing | AnonymousAnteater, imliuyzh
