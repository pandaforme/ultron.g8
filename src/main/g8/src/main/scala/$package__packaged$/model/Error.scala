package $package$.model

sealed abstract class Error
case class UnexpectedError(cause: Throwable) extends Error
case class ParseJsonError(cause: Exception) extends Error
case class DBError(cause: Exception) extends Error
