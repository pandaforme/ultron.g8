scalaVersion              := "2.12.8"
name                      := "starterkit"
organization              := "com.organization"
scalafmtOnCompile         := true
fork in Test              := true
parallelExecution in Test := true

lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val http4s = "0.20.6"
  val zio = "1.0.0-RC10-1"
  val zioInteropCats = "1.3.1.0-RC3"
  val circe = "0.11.1"
  val scalaTest = "3.0.8"
  val randomDataGenerator = "2.7"
  val ciris = "0.12.1"
  val logback = "1.2.3"
  val h2database = "1.4.199"
  val quill = "3.3.0"
  val tapir = "0.9.0"
}
addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector)
addCompilerPlugin("org.scalamacros" %% "paradise"     % Versions.scalamacros cross CrossVersion.full)

// Scala libraries
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"                                     % Versions.zio,
  "dev.zio" %% "zio-interop-cats"                        % Versions.zioInteropCats,
  "org.http4s" %% "http4s-core"                          % Versions.http4s,
  "org.http4s" %% "http4s-dsl"                           % Versions.http4s,
  "org.http4s" %% "http4s-blaze-server"                  % Versions.http4s,
  "org.http4s" %% "http4s-circe"                         % Versions.http4s,
  "io.circe" %% "circe-generic"                          % Versions.circe,
  "io.getquill" %% "quill-jdbc"                          % Versions.quill,
  "is.cir" %% "ciris-cats"                               % Versions.ciris,
  "is.cir" %% "ciris-cats-effect"                        % Versions.ciris,
  "is.cir" %% "ciris-core"                               % Versions.ciris,
  "is.cir" %% "ciris-enumeratum"                         % Versions.ciris,
  "is.cir" %% "ciris-generic"                            % Versions.ciris,
  "is.cir" %% "ciris-refined"                             % Versions.ciris,
  "com.softwaremill.tapir" %% "tapir-core"               % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-http4s-server"      % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s"  % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-openapi-docs"       % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-json-circe"         % Versions.tapir,
  "org.scalatest" %% "scalatest"                         % Versions.scalaTest % "test",
  "com.danielasfregola" %% "random-data-generator"       % Versions.randomDataGenerator % "test"
)

// Java libraries
libraryDependencies ++= Seq(
  "com.h2database" % "h2"              % Versions.h2database,
  "ch.qos.logback" % "logback-classic" % Versions.logback
)
