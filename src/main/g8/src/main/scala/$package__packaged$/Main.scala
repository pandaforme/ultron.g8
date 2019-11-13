package $package$

import scala.jdk.CollectionConverters._
import scala.util.Try

import cats.effect.ExitCode
import $package$.model.config.Application
import $package$.module.db.{LiveUserRepository, UserRepository}
import $package$.module.logger.{LiveLogger, Logger => MyLogger}
import $package$.route.UserRoute
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.auto._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import tapir.docs.openapi._
import tapir.openapi.circe.yaml._
import tapir.swagger.http4s.SwaggerHttp4s
import zio._
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object Main extends App {
  type AppEnvironment = Clock with UserRepository with MyLogger

  private val userRoute = new UserRoute[AppEnvironment]
  private val yaml = userRoute.getEndPoints.toOpenAPI("User", "1.0").toYaml
  private val httpApp =
    Router("/" -> userRoute.getRoutes, "/docs" -> new SwaggerHttp4s(yaml).routes[RIO[AppEnvironment, *]]).orNotFound
  private val finalHttpApp = Logger.httpApp[ZIO[AppEnvironment, Throwable, *]](true, true)(httpApp)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val result = for {
      applicationConfig <- ZIO.fromTry(Try(Application.getConfig))
      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[ZIO[AppEnvironment, Throwable, *]]
          .bindHttp(applicationConfig.server.port, applicationConfig.server.host.getHostAddress)
          .withHttpApp(finalHttpApp)
          .serve
          .compile[ZIO[AppEnvironment, Throwable, *], ZIO[AppEnvironment, Throwable, *], ExitCode]
          .drain
      }
      program <- server.provideSome[ZEnv] { base =>
        new Clock with LiveUserRepository with LiveLogger {
          val clock: Clock.Service[Any] = base.clock
          val config: Config = ConfigFactory.parseMap(
            Map(
              "dataSourceClassName" -> applicationConfig.database.className.value,
              "dataSource.url" -> applicationConfig.database.url.value,
              "dataSource.user" -> applicationConfig.database.user.value,
              "dataSource.password" -> applicationConfig.database.password.value
            ).asJava)
        }
      }
    } yield program

    result
      .foldM(failure = err => putStrLn(s"Execution failed with: \$err") *> ZIO.succeed(1), success = _ => ZIO.succeed(0))
  }
}
