package $package$.module.db

import $package$.model.Error
import $package$.model.database.User
import zio.ZIO

trait UserRepository {

  val repository: UserRepository.Service
}

object UserRepository {

  trait Service {

    def get(id: Long): ZIO[Any, Error, User]

    def create(user: User): ZIO[Any, Error, User]

    def delete(id: Long): ZIO[Any, Error, Unit]
  }
}
