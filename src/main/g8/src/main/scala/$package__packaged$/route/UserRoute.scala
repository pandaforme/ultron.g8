package $package$.route

import $package$.implicits.Circe._
import io.circe.Json
import io.circe.generic.auto._
import $package$.model.database.User
import $package$.model.{DBError, Error, ParseJsonError, UnexpectedError}
import $package$.module.db._
import $package$.module.logger.{Logger, _}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.{TaskR, ZIO}

class UserRoute[R <: UserRepository with Logger] {

  private val dsl: Http4sDsl[TaskR[R, ?]] = Http4sDsl[TaskR[R, ?]]
  import dsl._

  def route: HttpRoutes[TaskR[R, ?]] = {
    HttpRoutes.of[TaskR[R, ?]] {
      case GET -> Root / LongVar(id) => {
        val result = for {
          _ <- debug(s"id: \$id")
          user <- get(id)
          _ <- debug(s"user: \$user")
        } yield {
          user
        }

        handleError[Option[User]](result, { user =>
          user.fold(NotFound())(Ok(_))
        })
      }

      case request @ POST -> Root => {
        val result = for {
          user <- request.as[User].mapError(t => ParseJsonError(new Exception(t)))
          _ <- create(user)
        } yield {
          user
        }

        handleError[User](result, { user =>
          Created(user)
        })
      }

      case DELETE -> Root / LongVar(id) => {
        val result = for {
          _ <- debug(s"id: \$id")
          user <- get(id)
          _ <- debug(s"user: \$user")
          r <- user match {
            case Some(s) => delete(s.id).map(Some(_))
            case None => ZIO.unit.map(_ => None)
          }
        } yield { r }

        handleError[Option[Unit]](result, { user =>
          user.fold(NotFound())(Ok(_))
        })
      }
    }
  }

  private def handleError[A](
    result: ZIO[R, Error, A],
    f: A => TaskR[R, Response[TaskR[R, ?]]]): ZIO[R, Throwable, Response[TaskR[R, ?]]] = {
    result.foldM(
      {
        case DBError(e) =>
          error(e)(s"Database error: \$e").mapError(_ => new Throwable("")) *>
            InternalServerError(Json.obj("Database BOOM!!!" -> Json.fromString(e.getMessage)))

        case ParseJsonError(e) =>
          warn(e)(s"JSON error: \$e").mapError(_ => new Throwable("")) *>
            BadRequest(Json.obj("JSON BOOM!!!" -> Json.fromString(e.getMessage)))

        case UnexpectedError(t) =>
          error(t)(s"Unexpected error: \$t").mapError(_ => new Throwable("")) *>
            InternalServerError(Json.obj("BOOM!!!" -> Json.fromString(t.getMessage)))
      },
      f
    )
  }

}
