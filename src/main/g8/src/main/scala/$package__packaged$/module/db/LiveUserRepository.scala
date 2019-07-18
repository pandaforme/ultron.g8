package $package$.module.db

import com.typesafe.config.Config
import io.getquill.{H2JdbcContext, SnakeCase}
import $package$.model.database.User
import $package$.model.{DBError, Error, UnexpectedError}
import zio.ZIO

trait LiveUserRepository extends UserRepository {
  val config: Config

  override val repository: UserRepository.Service = new UserRepository.Service {
    lazy val ctx = new H2JdbcContext(SnakeCase, config)
    import ctx._

    def get(id: Long): ZIO[Any, Error, Option[User]] = {
      zio.IO.effect(ctx.run(query[User].filter(_.id == lift(id))).headOption).mapError {
        case e: Exception => DBError(e)
        case t: Throwable => UnexpectedError(t)
      }
    }

    def create(user: User): ZIO[Any, Error, User] = {
      zio.IO.effect(ctx.run(query[User].insert(lift(user)))).mapError {
        case e: Exception => DBError(e)
        case t: Throwable => UnexpectedError(t)
      } *> get(user.id).map(_.get)
    }

    def delete(id: Long): ZIO[Any, Error, Unit] = {
      zio.IO
        .effect(ctx.run(query[User].filter(_.id == lift(id)).delete))
        .mapError {
          case e: Exception => DBError(e)
          case t: Throwable => UnexpectedError(t)
        }
        .map(_ => ())
    }
  }
}
