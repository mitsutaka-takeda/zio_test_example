import zio.test.{DefaultRunnableSpec, _}

object SingleSpec extends DefaultRunnableSpec(
  test("test single") {
    ??? // ロジックを実行して結果をassertする
  }
)

object MultipleTestSpec extends DefaultRunnableSpec(
  suite("multiple test cases")(
    test("test case 1")(???),
    test("test case 2")(???)
  )
)
