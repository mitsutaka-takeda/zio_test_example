import CustomEnvironmentSpecUtil.DatabaseAccess
import zio.{Managed, RIO, Task, UIO, ZIO}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, _}

object CustomEnvironmentSpecUtil {

  trait DatabaseAccess {
    val databaseAccess: DatabaseAccess.Service
  }

  object DatabaseAccess {

    trait Service {
      def query: Task[Int]
    }

    val make: Managed[Nothing, DatabaseAccess] = Managed.make(UIO(
      new DatabaseAccess {
        override val databaseAccess: Service = new Service {
          override def query: Task[Int] = UIO(42)
        }
      }
    ))(_ => UIO.unit)
  }

  // create a Environment
  val databaseAccessEnvironment: Managed[Nothing, DatabaseAccess with TestEnvironment] = for {
    testEnvironment <- TestEnvironment.Value
    databaseAccessService <- DatabaseAccess.make
  } yield new TestEnvironment(
    blocking = testEnvironment.blocking,
    clock = testEnvironment.clock,
    console = testEnvironment.console,
    live = testEnvironment.live,
    random = testEnvironment.random,
    sized = testEnvironment.sized,
    system = testEnvironment.system
  ) with DatabaseAccess {
    override val databaseAccess: DatabaseAccess.Service = databaseAccessService.databaseAccess
  }
}

object CustomEnvironmentSpec extends DefaultRunnableSpec(
  testM("testing a logic with DatabaseAccess") {
    val logicWithDatabaseAccess: RIO[DatabaseAccess, Boolean] = for {
      q <- ZIO.accessM[DatabaseAccess] {
        _.databaseAccess.query
      }
    } yield q == 42

    assertM(logicWithDatabaseAccess, isTrue)
  }.provideManaged(CustomEnvironmentSpecUtil.databaseAccessEnvironment)
)
