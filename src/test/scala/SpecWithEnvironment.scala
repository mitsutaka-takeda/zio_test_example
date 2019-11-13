import zio.ZIO
import zio.clock.Clock
import zio.duration._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestClock

object BadSpecWithEnvironment extends DefaultRunnableSpec(
  suite("Spec with Environment")(
    testM("this test never finishes") {

      val logicUnderTest: ZIO[Clock, Nothing, Int] =  for {
        _ <- zio.clock.sleep(200.milliseconds)
      } yield 42

      assertM(logicUnderTest, equalTo(42))
    }
  )
)

object GoodSpecWithEnvironment extends DefaultRunnableSpec(
  suite("Spec with Environment")(
    testM("test with clock manually adjust time")(
      assertM(for {
        fiber <- zio.clock.sleep(200.milliseconds).fork
        _ <- TestClock.adjust(200.milliseconds)
        _ <- fiber.join
      } yield 42, equalTo(42))
    ),
    testM("use Live version of clock") {
      assertM(
        for {
          _ <- zio.test.environment.Live.live(zio.clock.sleep(200.milliseconds))
        } yield 42,
        equalTo(42)
      )
    }
  )
)
