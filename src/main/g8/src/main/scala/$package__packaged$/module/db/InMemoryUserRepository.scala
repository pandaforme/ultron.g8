package $package$.module.db

import $package$.model.Error
import $package$.model.database.User
import zio.{Ref, ZIO}

final case class InMemoryUserRepository(ref: Ref[Map[Long, User]]) extends UserRepository.Service {
  def get(id: Long): ZIO[Any, Error, Option[User]] = ref.get.map(_.get(id))

  def create(user: User): ZIO[Any, Error, User] = ref.update(map => map.+(user.id -> user)).map(_ => user)

  def delete(id: Long): ZIO[Any, Error, Unit] = ref.update(map => map.-(id)).unit
}
