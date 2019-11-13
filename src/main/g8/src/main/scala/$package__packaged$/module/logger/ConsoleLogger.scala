package $package$.module.logger

import $package$.implicits.Throwable._
import zio._
import zio.console.Console

trait ConsoleLogger extends Logger.Service with Console {

  def error(message: => String): ZIO[Any, Nothing, Unit] = console.putStr(message)

  def warn(message: => String): ZIO[Any, Nothing, Unit] = console.putStr(message)

  def info(message: => String): ZIO[Any, Nothing, Unit] = console.putStr(message)

  def debug(message: => String): ZIO[Any, Nothing, Unit] = console.putStr(message)

  def trace(message: => String): ZIO[Any, Nothing, Unit] = console.putStr(message)

  def error(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit] =
    console.putStr(s"message: \$message, exception: \${t.getStacktrace}")

  def warn(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit] =
    console.putStr(s"message: \$message, exception: \${t.getStacktrace}")

  def info(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit] =
    console.putStr(s"message: \$message, exception: \${t.getStacktrace}")

  def debug(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit] =
    console.putStr(s"message: \$message, exception: \${t.getStacktrace}")

  def trace(t: Throwable)(message: => String): ZIO[Any, Nothing, Unit] =
    console.putStr(s"message: \$message, exception: \${t.getStacktrace}")
}