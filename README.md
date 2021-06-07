<img src="demo.gif">

# Fabflix
Please visit the [wiki](https://github.com/imliuyzh/fabflix/wiki) for more information about this project.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. Alternatively, you can always use the link above to run the application.

### Running the Application
The only supported environment is Ubuntu v20.04+, Tomcat v9.0+, MySQL v8.0+, Java v11+, and IntelliJ IDEA.

#### Setup MySQL and Tomcat

#### Create a MySQL Database on the development machine

#### Setup IDE on the development machine

### Deploying to AWS
Go to AWS Console to sign up. You will need to enter a valid credit card. Don't worry; as long as you choose a free-tier instance and remove it after the end of the quarter, you will not be charged. When you are done, log in to the AWS console.
Launch a new Ubuntu 20.04 free-tier t2.micro EC2 instance. Notice that you need to generate and download a key to ssh to the machine, and it may take a few minutes for the instance to be initialized.
After the instance is running, you will see a public IP address assigned to it. Keep this IP: you are required to give us this IP to demo project 1.
When viewing the list of instances, you can click on the "connect" button, on the top to get instructions on how to use SSH to connect to the instance. By default, only the SSH port, 22, is open. In order to get other services (e.g., HTTP, HTTPS, and Tomcat) to be available to other machines, you will need to open the corresponding ports. To do so, when the instance is checked, select the security group, go to the "inbound" tab, and add more rules.
The name of the instance you will be launching is: `Ubuntu Server 20.04 LTS (HVM), SSD Volume Type`.

After this, please follow the steps in "Running the Application."

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
+ [@imliuyzh](https://github.com/imliuyzh)
+ [@anonymousanteater](https://github.com/anonymousanteater)
