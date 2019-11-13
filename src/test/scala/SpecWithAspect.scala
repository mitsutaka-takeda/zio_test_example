import zio.test.Assertion._
import zio.test._
import zio.duration._

object SpecWithAspect extends DefaultRunnableSpec(
  suite("Spec with aspect")(
    test("ignore test case which always fails due to bug"){
      assert(42, equalTo(43))
    }@@TestAspect.ignore,
    testM("timeout test if it takes too long"){
      zio.clock.sleep(60.minutes).map(r => assert(r, isUnit))
    }@@TestAspect.timeout(3.second)
  )
)
