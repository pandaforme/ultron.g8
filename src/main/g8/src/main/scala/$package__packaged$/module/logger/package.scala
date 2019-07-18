package $package$.module

import $package$.model.Error
import zio.ZIO

package object logger {
  def error(message: => String): ZIO[Logger, Error, Unit] = ZIO.accessM[Logger](_.logger.error(message))

  def warn(message: => String): ZIO[Logger, Error, Unit] = ZIO.accessM[Logger](_.logger.warn(message))

  def info(message: => String): ZIO[Logger, Error, Unit] = ZIO.accessM[Logger](_.logger.info(message))

  def debug(message: => String): ZIO[Logger, Error, Unit] = ZIO.accessM[Logger](_.logger.debug(message))

  def trace(message: => String): ZIO[Logger, Error, Unit] = ZIO.accessM[Logger](_.logger.trace(message))

  def error(t: Throwable)(message: => String): ZIO[Logger, Error, Unit] =
    ZIO.accessM[Logger](_.logger.error(t)(message))

  def warn(t: Throwable)(message: => String): ZIO[Logger, Error, Unit] =
    ZIO.accessM[Logger](_.logger.warn(t)(message))

  def info(t: Throwable)(message: => String): ZIO[Logger, Error, Unit] =
    ZIO.accessM[Logger](_.logger.info(t)(message))

  def debug(t: Throwable)(message: => String): ZIO[Logger, Error, Unit] =
    ZIO.accessM[Logger](_.logger.debug(t)(message))

  def trace(t: Throwable)(message: => String): ZIO[Logger, Error, Unit] =
    ZIO.accessM[Logger](_.logger.trace(t)(message))
}
