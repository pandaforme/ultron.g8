package $package$.route

import cats.syntax.semigroupk._
import $package$.implicits.Throwable._
import $package$.model.database.User
import $package$.model.response.{
  BadRequestResponse,
  ErrorResponse,
  InternalServerErrorResponse,
  NotFoundResponse
}
import $package$.model.{DBFailure, ExpectedFailure, NotFoundFailure}
import $package$.module.db._
import $package$.module.logger.{Logger, _}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import tapir.DecodeResult.Error
import tapir._
import tapir.json.circe._
import tapir.model.StatusCodes
import tapir.server.http4s._
import tapir.server.{DecodeFailureHandling, ServerDefaults}
import zio.interop.catz._
import zio.{RIO, ZIO}

class UserRoute[R <: UserRepository with Logger] extends Http4sDsl[RIO[R, *]] {
  private implicit val customServerOptions: Http4sServerOptions[RIO[R, *]] = Http4sServerOptions
    .default[RIO[R, *]]
    .copy(
      decodeFailureHandler = (request, input, failure) => {
        failure match {
          case Error(_, error) =>
            DecodeFailureHandling.response(jsonBody[BadRequestResponse])(BadRequestResponse(error.toString))
          case _ => ServerDefaults.decodeFailureHandler(request, input, failure)
        }
      }
    )

  private val getUserEndPoint = endpoint.get
    .in("user" / path[Long]("user id"))
    .errorOut(
      oneOf(
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse]),
        statusMapping(StatusCodes.NotFound, jsonBody[NotFoundResponse])
      ))
    .out(jsonBody[User])

  private val createUserEndPoint = endpoint.post
    .in("user")
    .in(jsonBody[User])
    .errorOut(
      oneOf[ErrorResponse](
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse])
      ))
    .out(statusCode(StatusCodes.Created))

  private val deleteUserEndPoint = endpoint.delete
    .in("user" / path[Long]("user id"))
    .errorOut(
      oneOf(
        statusMapping(StatusCodes.InternalServerError, jsonBody[InternalServerErrorResponse]),
        statusMapping(StatusCodes.NotFound, jsonBody[NotFoundResponse])
      ))
    .out(emptyOutput)

  val getRoutes: HttpRoutes[RIO[R, *]] = {
    getUserEndPoint.toRoutes { userId =>
      handleError(getUser(userId))
    } <+> createUserEndPoint.toRoutes { user =>
      handleError(create(user))
    } <+> deleteUserEndPoint.toRoutes { id =>
      val result = for {
        _ <- debug(s"id: \$id")
        user <- getUser(id)
        _ <- delete(user.id)
      } yield {}

      handleError(result)
    }
  }

  val getEndPoints = {
    List(getUserEndPoint, createUserEndPoint, deleteUserEndPoint)
  }

  private def getUser(userId: Long): ZIO[R, ExpectedFailure, User] = {
    for {
      _ <- debug(s"id: \$userId")
      user <- get(userId)
      u <- user match {
        case None => ZIO.fail(NotFoundFailure(s"Can not find a user by \$userId"))
        case Some(s) => ZIO.succeed(s)
      }
    } yield {
      u
    }
  }

  private def handleError[A](result: ZIO[R, ExpectedFailure, A]): ZIO[R, Throwable, Either[ErrorResponse, A]] = {
    result
      .fold(
        {
          case DBFailure(t) => Left(InternalServerErrorResponse("Database BOOM !!!", t.getMessage, t.getStacktrace))
          case NotFoundFailure(message) => Left(NotFoundResponse(message))
        },
        Right(_)
      )
      .foldCause(
        c => Left(InternalServerErrorResponse("Unexpected errors", "", c.squash.getStacktrace)),
        identity
      )
  }

}
