lazy val root = (project in file("."))
  .settings(
    name := "HW1CloudComputing",
    libraryDependencies ++= Seq(
      // Dependencies for deeplearning4j and nd4j
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-modelimport" % "1.0.0-M2.1",

      "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1",

      // Dependency for jtokkit
      "com.knuddels" % "jtokkit" % "1.1.0",

      // Logging - Use Logback as the sole SLF4J binding
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.slf4j" % "slf4j-api" % "2.0.12",

      // Configuration library
      "com.typesafe" % "config" % "1.4.3",

      // Dependencies for Hadoop
      "org.apache.hadoop" % "hadoop-common" % "3.4.0",
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.4.0",
      "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.4.0",
     //dependencies for http
      "com.typesafe.akka" %% "akka-http" % "10.5.3",
      "com.typesafe.akka" %% "akka-stream" % "2.8.6",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",


      // Dependencies for Apache Spark with SLF4J exclusions
      "org.apache.spark" %% "spark-core" % "3.5.0" exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.spark" %% "spark-sql" % "3.5.0" exclude("org.slf4j", "slf4j-log4j12"),

      // Dependency for scalatest
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
