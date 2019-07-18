package $package$

import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import $package$.implicits.Circe._
import io.circe.generic.auto._
import $package$.model.database.User
import $package$.module.db.{InMemoryUserRepository, UserRepository}
import $package$.module.logger.{ConsoleLogger, Logger}
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalatest.FlatSpec
import $package$.route.UserRoute
import zio.interop.catz._
import zio.{DefaultRuntime, Ref, ZIO}

class UserRouteSpec extends FlatSpec with HTTPSpec with DefaultRuntime with RandomDataGenerator {
  private val user: User = random[User]

  type Env = UserRepository with Logger
  private val userRoute: UserRoute[Env] = new UserRoute[Env]
  private val app = userRoute.route.orNotFound
  private val getEnv: ZIO[Any, Nothing, UserRepository with Logger] =
    for {
      store <- Ref.make(Map[Long, User]())
      repo = InMemoryUserRepository(store)
      env = new UserRepository with Logger {
        val repository: InMemoryUserRepository = repo
        val logger = ConsoleLogger
      }
    } yield env

  "UserRoute" should "create a user" in {
    val payload: Request[ZIO[Env, Throwable, ?]] = request(Method.POST, "/").withEntity(user)

    runWithEnv(check(app.run(payload), Status.Created, Some(user)))
  }

  it should "get a user" in {
    val postPayload: Request[ZIO[Env, Throwable, ?]] = request(Method.POST, "/").withEntity(user)
    val getPayload: Request[ZIO[Env, Throwable, ?]] = request(Method.GET, s"\${user.id}")

    runWithEnv(check(app.run(postPayload) *> app.run(getPayload), Status.Ok, Some(user)))
  }

  it should "delete a user" in {
    val postPayload: Request[ZIO[Env, Throwable, ?]] = request(Method.POST, "/").withEntity(user)
    val getPayload: Request[ZIO[Env, Throwable, ?]] = request(Method.GET, s"\${user.id}")
    val deletePayload: Request[ZIO[Env, Throwable, ?]] = request(Method.DELETE, s"\${user.id}")

    runWithEnv(
      check(
        app.run(postPayload) *> app.run(deletePayload) *> app.run(getPayload),
        Status.NotFound,
        Option.empty[Array[Byte]]))
  }

  private def runWithEnv[E, A](task: ZIO[Env, E, A]): A = {
    val result: ZIO[Any, E, A] = for {
      env <- getEnv
      r <- task.provide(env)
    } yield {
      r
    }

    unsafeRun(result)
  }
}
