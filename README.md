<<<<<<< HEAD
Name: Dawid Biel UIN: 665470421
HW 2 Cloud Computing This is a program for parallel distributed processing of a large corpus of text.

Installation: Java JDK 8 or Higher, Apache Hadoop, sbt, IntelliJ IDEA, SCALA

Steps: Clone repository

git clone git@github.com:djbiel2/CS441HW2.git cd CS441HW2

Make sure all the dependencies are installed


curl -L https://www.scala-sbt.org/sbt-rpm.repo | sudo tee /etc/yum.repos.d/sbt.repo
sudo yum install sbt -y
sbt sbtVersion

Setup the Hadoop cluster on AWS EMR

Edit the application.conf to your specific configurations. -trainer is for the word to vector model -main is the paths -sliding_window Parameters to the sliding window

Running the program
stb compile
sbt "runMain Main"


spark-submit --class Main --master yarn your_path_jar_file.jar


to test use
sbt test


=======
my njame is Daiwd biel and I am doing this projeict
>>>>>>> 1d547d8 (Create README.md)
