import zio.test._
import zio.test.Assertion._

object SpecWithoutEffect extends DefaultRunnableSpec(
  test("42 is the answer to ultimate question of life") {
    val answerToUltimateQuestion = 42
    assert(answerToUltimateQuestion, equalTo(42))
  }
)
