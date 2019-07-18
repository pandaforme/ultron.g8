package $package$

import cats.effect.Sync
import cats.implicits._
import org.http4s.{EntityDecoder, Method, Request, Response, Status, Uri}
import org.scalatest.{Assertion, Matchers}

trait HTTPSpec extends Matchers {
  protected def request[F[_]](method: Method, uri: String): Request[F] =
    Request(method = method, uri = Uri.unsafeFromString(uri))

  protected def check[F[_]: Sync, A](
    response: F[Response[F]],
    expectedStatus: Status,
    expectedBody: Option[A]
  )(
    implicit
    ev: EntityDecoder[F, A]): F[Unit] =
    for {
      r <- response
      _ <- expectedBody.fold[F[Assertion]] { r.body.compile.toVector.map(s => assert(s.isEmpty)) } { expected =>
        r.as[A].map(x => assert(x === expected, s"Body was \$x instead of \$expected."))
      }
      _ <- Sync[F].delay(assert(r.status == expectedStatus, s"Status was \${r.status} instead of \$expectedStatus."))
    } yield ()

}
