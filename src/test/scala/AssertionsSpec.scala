import zio.ZIO
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

object AssertionsSpec extends DefaultRunnableSpec(
  suite("Assertions")(
    test("testing equality") {
      assert(42, equalTo(42))
    },
    test("testing approximation") {
      assert(42.0, approximatelyEquals(42.1, 0.1))
    },
    test("string starts with") {
      assert("abc", startsWith("ab"))
    },
    test("string ends with") {
      assert("abc", endsWith("bc"))
    },
    test("string comparison with case insensitive manner") {
      assert("ABC", equalsIgnoreCase("abc"))
    },
    test("test to throw an exception") {
      assert(throw new Exception("exception"), throws(anything))
    },
    testM("testing type of failure") {
      object MyError extends Exception("MyError")
      assertM(ZIO.fail(MyError).run, fails(isSubtype[Exception](anything)))
    }
  )
)

object CombinationOfAssertionSpec extends DefaultRunnableSpec(
  suite("Combination of Assertion")(
    test("negating an assertion") {
      assert(42, not(equalTo(43)))
    },
    test("logical conjunction of 2 assertions") {
      assert(
        "ZIO Test is a zero dependency testing library",
        startsWith("ZIO") && endsWith("library")
      )
    },
    test("logical disjunction of 2 assertions") {
      assert(42, equalTo(42) || equalTo(43))
    }
  )
)

object CombinationOfTestResultSpec extends DefaultRunnableSpec(
  suite("Combination of TestResult")(
    test("negating a test result") {
      !assert(42, equalTo("forty two"))
    },
    test("logical conjunction of 2 test results") {
      assert(42, equalTo(42)) && assert("forty two", equalTo("forty two"))
    },
    test("logical disjunction of 2 test results") {
      val x = 42
      assert(x, equalTo(42)) || assert(x, equalTo(43))
    },
    test("logical implication of test results") {
      val x = 42

      def isEven(z: Int) = z % 2 == 0

      assert(x, equalTo(42)) ==> assert(isEven(x), isTrue)
    },
    test("logical implication of test results take2") {
      val x = 41

      def isEven(z: Int) = z % 2 == 0

      assert(x, equalTo(42)) ==> assert(isEven(x), isTrue)
    }
  )
)
