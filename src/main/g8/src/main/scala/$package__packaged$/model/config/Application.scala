package $package$.model.config

import java.net.InetAddress

import ciris.api.Id
import ciris.refined._
import ciris.{env, loadConfig, ConfigResult}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.net.UserPortNumber

final case class Application(
  server: Server,
  database: Database
)

object Application {

  private val config: ConfigResult[Id, Application] = loadConfig(
    env[InetAddress]("server.host"),
    env[UserPortNumber]("server.port"),
    env[Refined[String, NonEmpty]]("dataSource.className"),
    env[Refined[String, NonEmpty]]("dataSource.url"),
    env[Refined[String, NonEmpty]]("dataSource.user"),
    env[Refined[String, NonEmpty]]("dataSource.password")
  ) { (url, port, className, dataSourceUrl, user, password) =>
    Application(Server(url, port), Database(className, dataSourceUrl, user, password))
  }

  val getConfig: Application = config.orThrow()
}
