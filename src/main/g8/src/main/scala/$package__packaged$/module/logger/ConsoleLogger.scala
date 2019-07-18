package $package$.module.logger

import $package$.model.Error
import zio._

object ConsoleLogger extends Logger.Service {
  def error(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal(println(message))

  def warn(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal(println(message))

  def info(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal(println(message))

  def debug(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal(println(message))

  def trace(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal(println(message))

  def error(t: Throwable)(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def warn(t: Throwable)(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def info(t: Throwable)(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def debug(t: Throwable)(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def trace(t: Throwable)(message: => String): ZIO[Any, Error, Unit] = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }
}
