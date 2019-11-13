package $package$.module.db

import $package$.model.database.User
import $package$.model.{DBFailure, ExpectedFailure}
import com.typesafe.config.Config
import io.getquill.{H2JdbcContext, SnakeCase}
import zio.ZIO

trait LiveUserRepository extends UserRepository {
  val config: Config
  lazy val ctx: H2JdbcContext[SnakeCase.type] = new H2JdbcContext(SnakeCase, config)
  import ctx._

  override val repository: UserRepository.Service = new UserRepository.Service {

    def get(id: Long): ZIO[Any, ExpectedFailure, Option[User]] = {
      for {
        list <- ZIO.effect(ctx.run(query[User].filter(_.id == lift(id)))).mapError(t => DBFailure(t))
        user <- list match {
          case Nil => ZIO.none
          case s :: _ => ZIO.some(s)
        }
      } yield {
        user
      }
    }

    def create(user: User): ZIO[Any, ExpectedFailure, Unit] = {
      zio.IO
        .effect(ctx.run(query[User].insert(lift(user))))
        .mapError(t => DBFailure(t))
        .unit
    }

    def delete(id: Long): ZIO[Any, ExpectedFailure, Unit] = {
      zio.IO
        .effect(ctx.run(query[User].filter(_.id == lift(id)).delete))
        .mapError(t => DBFailure(t))
        .unit
    }
  }
}