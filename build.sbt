name := "SearchEngine"

version := "1.0"

scalaVersion := "2.12.6"

lazy val dependencies = new {
  val akkaVer = "2.5.14"
  val akkaHttpVer = "10.1.4"
  val scalaTestVer = "3.0.5"
  val googleColVer = "1.0"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVer
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVer
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVer
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVer
  val googleCollections = "com.google.collections" % "google-collections" % googleColVer
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVer


  val akkaTest = "com.typesafe.akka" %% "akka-testkit" % akkaVer % Test
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVer % Test
  val akkaHttpTest = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVer % Test
}

lazy val commonDependencies = Seq(dependencies.akkaActor, dependencies.scalaTest)

lazy val commonSettings = Seq(
  organization := "com.conductor",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  parallelExecution in Test := false
)


lazy val client = (project in file("./client"))
  .settings(
    commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(dependencies.akkaHttp, dependencies.akkaStream)
  )

lazy val storage = (project in file("./storage"))
  .settings(
    commonSettings,
    libraryDependencies ++= commonDependencies ++
      Seq(dependencies.akkaTest, dependencies.googleCollections, dependencies.akkaCluster)
  )

lazy val rest = (project in file("./rest") dependsOn storage)
  .settings(
    commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(dependencies.akkaHttp, dependencies.akkaHttpJson, dependencies.akkaHttpTest)
  )

