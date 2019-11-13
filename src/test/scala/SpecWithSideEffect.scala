import zio.UIO
import zio.test._
import zio.test.Assertion._

object SpecWithSideEffect extends DefaultRunnableSpec(
  testM("waiting 7.5 million years for the answer") {
    val answerToUltimateQuestion: UIO[Int] = UIO(42)
    assertM(answerToUltimateQuestion, equalTo(42))
  }
)

object SpecWithSideEffectForFootNote extends DefaultRunnableSpec(
  testM("waiting 7.5 million years for the answer") {
    val answerToUltimateQuestion: UIO[Int] = UIO(42)
    answerToUltimateQuestion.map(v => assert(v, equalTo(42)))
  }
)
