A ZIO + http4s + Circe + Quill + Tapir giter8 template

# How to install
```sh
brew update && brew install giter8
g8 pandaforme/ultron.g8
```

# How to add a new API
1. Create a package in `module`
for example: `xyz`

2. Create an interface in `module.xyz`
```scala
trait XYZ {

  val service: XYZ.Service
}

object XYZ {

  trait Service {

    def doXYZ(): ZIO[Any, Error, Unit]
  }
}
```

3. Create a package object in `module.xyz`
```scala
package object xyz {

  def doXYZ(id: Long): ZIO[XYZ, Error, Unit] =
    ZIO.accessM(_.service.doXYZ())
}
```

4. Create an instance for test/live in `module.xyz`
```scala
trait LiveXYZ extends XYZ {
    override val service: XYZ.Service = new XYZ.Service {
        def doXYZ(): ZIO[Any, Error, Unit] = ???
    }
}
```

5. Create your own route in `route` and pass your interface into enviroment type
```scala
class XyzRoute[R <: XYZ] extends Http4sDsl[TaskR[R, ?]] {
  private val xyzEndPoint = endpoint.get
    .in("xyz" / path[Long]("user id"))
    .errorOut(emptyOutput)
    .out(emptyOutput)    

  val getRoutes: HttpRoutes[TaskR[R, ?]] = ???
  val getEndPoints = List(xyzEndPoint)   
}
```
6. Write unit test

7. Add your interfaces to `AppEnvironment`, routes to `httpApp` and provide Live instances in `Main.scala`
```scala
object Main extends App {
  type AppEnvironment = Clock with Console with UserRepository with MyLogger with XYZ
  private val userRoute = new UserRoute[AppEnvironment]
  private val xyzRoute = new XyzRoute[AppEnvironment]
  private val yaml = userRoute.getEndPoints.toOpenAPI("User", "1.0").toYaml

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] = {
    val result = for {
      application <- ZIO.fromTry(Try(Application.getConfig))

      httpApp = Router(
          "/" -> userRoute.getRoutes,
          "/" -> xyzRoute.getRoutes, 
          "/docs" -> new SwaggerHttp4s(yaml).routes[TaskR[AppEnvironment, ?]]).orNotFound
      finalHttpApp = Logger.httpApp[ZIO[AppEnvironment, Throwable, ?]](true, true)(httpApp)

      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[ZIO[AppEnvironment, Throwable, ?]]
          .bindHttp(application.server.port, application.server.host.getHostAddress)
          .withHttpApp(finalHttpApp)
          .serve
          .compile[ZIO[AppEnvironment, Throwable, ?], ZIO[AppEnvironment, Throwable, ?], ExitCode]
          .drain
      }
      program <- server.provideSome[Environment] { base =>
        new Clock with Console with LiveUserRepository with LiveLogger with LiveXyz{
          val clock: Clock.Service[Any] = base.clock
          val console: Console.Service[Any] = base.console
          val config: Config = ConfigFactory.parseMap(
            Map(
              "dataSourceClassName" -> application.database.className.value,
              "dataSource.url" -> application.database.url.value,
              "dataSource.user" -> application.database.user.value,
              "dataSource.password" -> application.database.password.value
            ).asJava)
        }
      }
    } yield program

    result
      .foldM(failure = err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), success = _ => ZIO.succeed(0))
  }
}
```

# API Endpoints
```
Swagger: http://localhost:5566/docs
User API: http://localhost:5566/user
```


# Referneces
* [ZIO with http4s and doobie](https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089)
* [mschuwalow/zio-todo-backend](https://github.com/mschuwalow/zio-todo-backend/)
* [loicdescotte/pureWebappSample](https://github.com/loicdescotte/pureWebappSample)
