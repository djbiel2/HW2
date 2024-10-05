lazy val root = (project in file("."))
  .settings(
    name := "HW1CloudComputing",
    libraryDependencies ++= Seq(
      // dependencies for deeplearning4j and nd4j
      "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-modelimport" % "1.0.0-M2.1",
      "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1",

      // Dependency for jtokkit (corrected name in comment)
      "com.knuddels" % "jtokkit" % "1.1.0",

      // Logging and Configuration
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.slf4j" % "slf4j-api" % "2.0.12",
      "com.typesafe" % "config" % "1.4.3",

      // Dependencies for Hadoop
      "org.apache.hadoop" % "hadoop-common" % "3.4.0",
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.4.0",
      "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.4.0",


      //Dependency for scalatest
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
