package $package$.module.logger

import zio.ZIO
trait Logger {
  val logger: Logger.Service
}

object Logger {

  trait Service {
    def error(message: => String): ZIO[Any, Nothing, Unit]

    def warn(message: => String): ZIO[Any, Nothing, Unit]

    def info(message: => String): ZIO[Any, Nothing, Unit]

    def debug(message: => String): ZIO[Any, Nothing, Unit]

    def trace(message: => String): ZIO[Any, Nothing, Unit]

    def error(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit]

    def warn(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit]

    def info(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit]

    def debug(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit]

    def trace(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit]
  }
}