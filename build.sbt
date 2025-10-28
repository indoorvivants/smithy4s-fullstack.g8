import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.ModuleSplitStyle
import smithy4s.codegen.Smithy4sCodegenPlugin

val Versions = new {
  val http4s = "0.23.30"

  val Scala = "3.7.3"

  val scribe = "3.17.0"

  val smithy4sFetch = "0.0.4"

  val TestContainers = "0.43.0"

  val Weaver = "0.10.1"

  val Laminar = "17.2.1"

  val waypoint = "9.0.0"

  val circe = "0.14.5"

  val skunk = "1.0.0-M11"

  val macroTaskExecutor = "1.1.1"

  val dumbo = "0.6.0"

  val declineDerive = "0.3.3"
}

val Config = new {
  val BasePackage = "hellosmithy4s"
}

inThisBuild(
  Seq(
    semanticdbEnabled := true // enable SemanticDB
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(backend.projectRefs*)
  .aggregate(shared.projectRefs*)
  .aggregate(frontend.projectRefs*)
  .aggregate(tests.projectRefs*)

lazy val backend = projectMatrix
  .in(file("modules/backend"))
  .dependsOn(shared)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % Versions.http4s,
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion.value,
      "com.outr"          %%% "scribe"          % Versions.scribe,
      "com.outr"          %%% "scribe-cats"     % Versions.scribe,
      "com.outr"          %%% "scribe-slf4j"    % Versions.scribe,
      "org.tpolecat"      %%% "skunk-core"      % Versions.skunk,
      "dev.rolang"        %%% "dumbo"           % Versions.dumbo,
      "com.indoorvivants" %%% "decline-derive"  % Versions.declineDerive,
      "org.typelevel"      %% "otel4s-oteljava" % "0.13.1", // <1>
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.55.0" % Runtime, // <2>
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.55.0" % Runtime // <3>

    ),
    Compile / doc / sources := Seq.empty,
    javaOptions += "-Dotel.java.global-autoconfigure.enabled=true", // <4>
    javaOptions += "-Dotel.service.name=hello-smithy4s",            // <5>
    // javaOptions += "-Dotel.exporter.otlp.endpoint=http://localhost:4317", // <6>
    reStart / baseDirectory := (ThisBuild / baseDirectory).value,
    run / baseDirectory     := (ThisBuild / baseDirectory).value,
    (Compile / compile) := ((Compile / compile) dependsOn (Compile / copyResources)).value,
    scalacOptions += "-Wunused:all"
  )

lazy val shared = projectMatrix
  .in(file("modules/shared"))
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .jsPlatform(Seq(Versions.Scala))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion.value
    )
  )

lazy val frontend = projectMatrix
  .in(file("modules/frontend"))
  .jsPlatform(Seq(Versions.Scala))
  .defaultAxes((defaults :+ VirtualAxis.js)*)
  .dependsOn(shared)
  .enablePlugins(ForgeViteWebappPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo"    %%% "waypoint"       % Versions.waypoint,
      "com.raquo"    %%% "laminar"        % Versions.Laminar,
      "io.circe"     %%% "circe-core"     % Versions.circe,
      "io.circe"     %%% "circe-parser"   % Versions.circe,
      "tech.neander" %%% "smithy4s-fetch" % Versions.smithy4sFetch,
      "org.scala-js" %%% "scala-js-macrotask-executor" % Versions.macroTaskExecutor
    ),
    scalacOptions += "-Wunused:all"
  )

lazy val tests = projectMatrix
  .in(file("modules/tests"))
  .dependsOn(backend)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    libraryDependencies ++= Seq(
      // test dependencies
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.TestContainers % Test,
      "org.typelevel" %%% "weaver-cats"         % Versions.Weaver % Test,
      "org.http4s"    %%% "http4s-ember-server" % Versions.http4s % Test,
      "org.http4s"    %%% "http4s-ember-client" % Versions.http4s % Test,
      "dev.rolang"    %%% "dumbo"               % Versions.dumbo
    ),
    Test / fork             := true,
    Compile / doc / sources := Seq.empty,
    scalacOptions += "-Wunused:all"
  )

lazy val defaults =
  Seq(VirtualAxis.scalaABIVersion(Versions.Scala), VirtualAxis.jvm)

ThisBuild / concurrentRestrictions ++= {
  if (sys.env.contains("CI")) {
    Seq(
      Tags.limitAll(4)
    )
  } else Seq.empty
}

ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))

addCommandAlias(
  "stubTests",
  s"tests/testOnly *.stub.*"
)
addCommandAlias(
  "integrationTests",
  s"tests/testOnly *.integrationtest.*"
)
addCommandAlias(
  "fix",
  s"scalafmtAll; scalafmtSbt; scalafixAll"
)
addCommandAlias(
  "ci",
  s"scalafmtCheck; scalafixAll --check; test"
)

ThisBuild / concurrentRestrictions ++= {
  if (sys.env.contains("CI")) {
    Seq(
      Tags.limitAll(4)
    )
  } else Seq.empty
}

ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))

import sbtwelcome.*

logo :=
  s"""
     | ##### ###### #    # #####  #        ##   ##### ######
     |   #   #      ##  ## #    # #       #  #    #   #
     |   #   #####  # ## # #    # #      #    #   #   #####
     |   #   #      #    # #####  #      ######   #   #
     |   #   #      #    # #      #      #    #   #   #
     |   #   ###### #    # #      ###### #    #   #   ######
     |
     |Version: ${version.value}
     |
     |${scala.Console.YELLOW}Scala ${(backend.jvm(
      true
    ) / scalaVersion).value}${scala.Console.RESET}
     |
     |""".stripMargin

logoColor := scala.Console.MAGENTA

usefulTasks := Seq(
  UsefulTask("fx", "fix", "Run Scalafmt and Scalafix"),
  UsefulTask("st", "stubTests", "Stub tests - fast, only in memory"),
  UsefulTask(
    "it",
    "integrationTests",
    "Integration tests - run against Docker container, exercising both database and HTTP logic"
  )
)
