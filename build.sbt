import sbt.Project.projectToRef

lazy val clients = Seq(client)
lazy val scalaV = "2.11.7"

lazy val server = (project in file("server")).settings(
  name := "MARS-admin-website",
  version := "1.0.0",
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.4.0",
    "org.webjars" % "jquery" % "2.2.0",
    "org.webjars" % "jquery-validation" % "1.14.0-1",
    "org.webjars" % "highcharts" % "4.2.3",
    "org.webjars.bower" % "bootstrap-material-datetimepicker" % "2.5.3",
    "org.typelevel" %% "cats" % "0.4.1",
    "org.scalaj" %% "scalaj-http" % "2.1.0",
    "com.github.nscala-time" %% "nscala-time" % "2.6.0"
  ),

  // docker settings; run "sbt docker" to generate a docker image and Dockerfile
  // https://github.com/marcuslonnberg/sbt-docker
  dockerfile in docker := {
    val appDir: File = stage.value
    val targetDir = "/app"
    val port = 9000

    new Dockerfile {
      from("thangiee/mars-base:v1")
      expose(port)

      env("MARS_BACKEND_URL", "http://backend:8080")
      env("MARS_PLAY_SECRET", "CHANGEME")

      entryPoint(s"$targetDir/bin/${executableScriptName.value}", "-J-server")
      copy(appDir, targetDir)
    }
  },

  imageNames in docker := Seq(
    ImageName(s"thangiee/${name.value.toLowerCase()}:latest"),
    ImageName(
      namespace = Some("thangiee"),
      repository = name.value.toLowerCase(),
      tag = Some("v" + version.value)
    )
  )
).enablePlugins(PlayScala, sbtdocker.DockerPlugin, JavaAppPackaging).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalacOptions += "-Xexperimental",
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats" % "0.4.1",
    "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
    "com.lihaoyi" %%% "scalatags" % "0.5.4",
    "com.lihaoyi" %%% "upickle" % "0.3.8",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4"
  ),
  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "0.14.3" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars.bower" % "react" % "0.14.3" / "react-dom.js" minified  "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM")
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
