package $package$.route

import cats.syntax.semigroupk._
import $package$.implicits.Throwable._
import $package$.model.database.User
import $package$.model.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import $package$.model.{DBError, Error, NotFoundError, UnexpectedError}
import $package$.module.db._
import $package$.module.logger.{Logger, _}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import tapir._
import tapir.json.circe._
import tapir.model.StatusCodes
import tapir.server.http4s._
import zio.interop.catz._
import zio.{TaskR, ZIO}

class UserRoute[R <: UserRepository with Logger] extends Http4sDsl[TaskR[R, ?]] {
  private val getUserEndPoint = endpoint.get
    .in("user" / path[Long]("user id"))
    .errorOut(
      oneOf(
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse]),
        statusMapping(StatusCodes.NotFound, jsonBody[NotFoundResponse])
      ))
    .out(jsonBody[User])

  private val insertUserEndPoint = endpoint.post
    .in("user")
    .in(jsonBody[User])
    .errorOut(
      oneOf[ErrorResponse](
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse])
      ))
    .out(
      oneOf(
        statusMapping(StatusCodes.Created, jsonBody[User])
      )
    )

  private val deleteUserEndPoint = endpoint.delete
    .in("user" / path[Long]("user id"))
    .errorOut(
      oneOf(
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse]),
        statusMapping(StatusCodes.NotFound, jsonBody[NotFoundResponse])
      ))
    .out(emptyOutput)

  val getRoutes: HttpRoutes[TaskR[R, ?]] = {
    getUserEndPoint.toRoutes { userId =>
      {
        val result = for {
          _ <- debug(s"id: \$userId")
          user <- get(userId)
          _ <- debug(s"user: \$user")
        } yield {
          user
        }

        handleError(result)
      }
    } <+> insertUserEndPoint.toRoutes { user =>
      handleError(create(user))
    } <+> deleteUserEndPoint.toRoutes { id =>
      {
        val result = for {
          _ <- debug(s"id: \$id")
          user <- get(id)
          _ <- delete(user.id)
        } yield {}

        handleError(result)
      }
    }
  }

  val getEndPoints = {
    List(getUserEndPoint, insertUserEndPoint, deleteUserEndPoint)
  }

  private def handleError[A](result: ZIO[R, Error, A]): ZIO[R, Throwable, Either[ErrorResponse, A]] = {
    result.foldM(
      {
        case DBError(e) =>
          error(e)(s"Database error: \$e").either.map(_ =>
            Left(InternalServerErrorResponse("Database BOOM", e.getMessage, e.getStacktrace)))

        case UnexpectedError(t) =>
          error(t)(s"Unexpected error: \$t").either.map(_ =>
            Left(InternalServerErrorResponse("Database BOOM", t.getMessage, t.getStacktrace)))

        case NotFoundError(m) =>
          warn(s"Not Found").either.map(_ => Left(NotFoundResponse(s"Something BOOM, \$m")))
      },
      a => ZIO.succeed(Right(a))
    )
  }

}
