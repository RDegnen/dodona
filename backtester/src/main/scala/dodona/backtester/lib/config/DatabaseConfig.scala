package dodona.backtester.lib.config

import slick.jdbc.JdbcBackend.Database

object DatabaseConfig {
  val sqlite = Database.forConfig("DodonaBacktester.db.sqlite")
  val h2 = Database.forConfig("DodonaBacktester.db.h2")
}