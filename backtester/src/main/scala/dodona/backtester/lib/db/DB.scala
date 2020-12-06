package dodona.backtester.lib.db

import scala.concurrent.Future

import slick.basic.DatabasePublisher
import slick.dbio.{DBIOAction, NoStream, Streaming}
import slick.jdbc.JdbcBackend.Database

class DB(database: Database) {
  def run[R](a: DBIOAction[R,NoStream,Nothing]): Future[R] =
    database.run(a)

  def stream[T](a: DBIOAction[_, Streaming[T], Nothing]): DatabasePublisher[T] =
    database.stream(a)
}