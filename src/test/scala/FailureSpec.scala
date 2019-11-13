import zio.{Exit, UIO, ZIO}
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

object FailureSpec extends DefaultRunnableSpec(
  suite("Testing effects")(
    testM("testing failure"){
      // runメソッドで失敗`Exception`情報にアクセスする。
      val result: UIO[Exit[Exception, Nothing]] = ZIO.fail(new Exception("failure")).run
      assertM(result, fails(anything))
    },
    testM("testing cause")(
      assertM(ZIO.die(new Exception("die")).run, dies(anything))
    )
  )
)
