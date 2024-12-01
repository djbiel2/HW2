
Name: Dawid Biel UIN: 665470421 HW 3 Cloud Computing This is a program for parallel distributed processing of a large corpus of text.

This ReadMe is for running the already trained model on AWS.
First create an instance and update the linux box

git clone git@github.com:djbiel2/CS441HW2.git
cd CS441HW2
curl -L https://www.scala-sbt.org/sbt-rpm.repo | sudo tee /etc/yum.repos.d/sbt.repo
sudo yum install sbt -y
sbt sbtVersion
sbt clean compile
sudo sbt "runMain My_LLM"

The server is now running and accepting connections. Open up your aws instance and check the IP, then go to its 8080 port to access the application

https://youtu.be/qOZQglp8IKo


