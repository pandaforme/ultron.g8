package $package$.module.db

import $package$.model.database.User
import $package$.model.{DBError, Error, NotFoundError, UnexpectedError}
import com.typesafe.config.Config
import io.getquill.{H2JdbcContext, SnakeCase}
import zio.ZIO

trait LiveUserRepository extends UserRepository {
  val config: Config

  override val repository: UserRepository.Service = new UserRepository.Service {
    lazy val ctx = new H2JdbcContext(SnakeCase, config)
    import ctx._

    def get(id: Long): ZIO[Any, Error, User] = {
      (for {
        list <- zio.IO.effect(ctx.run(query[User].filter(_.id == lift(id))))
        user <- list match {
          case Nil => ZIO.fail(NotFoundError(s"Not found a user by id = \$id"))
          case s :: _ => ZIO.succeed(s)
        }
      } yield {
        user
      }).mapError {
        case e: Exception => DBError(e)
        case t: Throwable => UnexpectedError(t)
      }
    }

    def create(user: User): ZIO[Any, Error, User] = {
      zio.IO.effect(ctx.run(query[User].insert(lift(user)))).mapError {
        case e: Exception => DBError(e)
        case t: Throwable => UnexpectedError(t)
      } *> get(user.id)
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