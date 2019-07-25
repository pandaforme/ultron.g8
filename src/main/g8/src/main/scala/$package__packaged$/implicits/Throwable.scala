package $package$.implicits

import java.io.{PrintWriter, StringWriter}

object Throwable {
  implicit class ThrowableExtension(t: Throwable) {

    def getStacktrace: String = {
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
  }
}
