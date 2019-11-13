ZIO Test

ZIO TestはZIOをベースにしたテストライブラリです。この記事ではZIOテストの書き方を見ていきます。

この記事のソースコードは[こちら](https://github.com/mitsutaka-takeda/zio_test_example)です。

# テスト・コードの書き方・基本編

テストを書くためには`zio.test.DefaultRunnableSpec`クラスを継承します。

`zio.test.DefaultRunnableSpec`はコンストラクタで`ZSpec`を受け取ります。`ZSpec`はテスト仕様を表現する型です。`ZSpec`の値を生成するには`zio.test.test`関数を使用します。`zio.test.test`関数はラベルとテスト・コードを引数にとります。テスト・コード中でテスト対象のロジックを実行し結果をチェックします。

```scala
import zio.test.{DefaultRunnableSpec, _}

object SingleSpec extends DefaultRunnableSpec(
  test("test single") {
    ??? // テスト・コード。テスト対象のロジックを実行して結果をassertする
  }
)
```

複数のテストケースをひとまとめにするには`zio.test.suite`関数を使用します。`zio.test.suite`関数は表示用のラベルとテストのリストを受け取ります。`zio.test.suite`では第2引数のカッコが丸かっこ`(`であることに注意しましょう。波かっこ`{`を使用するとBlock式になってリストが渡せなくなります。

```scala
import zio.test.{DefaultRunnableSpec, _}

object MultipleTestSpec extends DefaultRunnableSpec(
  suite("multiple test cases")(
    test("test case 1")(???),
    test("test case 2")(???)
  )
)
```

## 副作用のないコードのテスト

ZIO Testではテスト対象のコードが副作用のないコードか、副作用のあるコード（戻り値の型が`ZIO`モナド）かでコードの書き方が少し変わります。前述の`zio.test.test`関数は副作用のないテストコードを生成するための関数です。

`zio.test.test`のテストコード内では副作用のないコードの実行結果を`zio.test.assert`関数でチェックします。`zio.test.assert`関数は第1引数にチェックしたい対象の値を第2引数に値に対する表明`zio.test.Assertion`を取ります。

```scala
import zio.test._
import zio.test.Assertion._

object SpecWithoutEffect extends DefaultRunnableSpec(
  test("42 is the answer to ultimate question of life") {
    val answerToUltimateQuestion = 42
    assert(answerToUltimateQuestion, equalTo(42))
  }
)
```

## 副作用のあるコードのテスト

副作用のあるコードをテストするには`zio.test.test`と`zio.test.assert`の代わりに`zio.test.testM`と`zio.test.assertM`(*1) を使用します。

```scala
import zio.UIO
import zio.test._
import zio.test.Assertion._

object SpecWithSideEffect extends DefaultRunnableSpec(
  testM("waiting 7.5 million years for the answer"){
    val answerToUltimateQuestion: UIO[Int] = UIO(42)
    assertM(answerToUltimateQuestion, equalTo(42))
  }
)
```

成功時の値ではなく失敗に関するテストを書くためにはZIOモナドの`run`メソッドでエラー情報にアクセスし
`zio.test.Assertion.fails`、`zio.test.Assertion.dies`関数で表明を行います。(*2)

```scala
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
```

# テスト・コードの書き方・実践編

ここまでZIO Testを使用したテストの基本的な書き方（副作用のない場合＆ある場合）を見てきました。これ以降では実際のテストを書くときに役立ちそうな情報をいくつか紹介します。

## 表明`Assertion`について

値に対する表明は40種類以上用意されています。ここでは数種類の表明をコードで紹介します。

```scala
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
```

ここで上げた表明以外にも、コレクション型・Option型に対する表明など便利なものが多数用意されています。

## テスト結果(`assert`) & 表明(`Assertion`)の合成

テスト結果(`assert`)や値に対する表明`Assertion`は合成できます。

テスト結果は、否定(negation)、論理和(logical disjunction)、論理積（logical conjunction）、論理包含(implication)で合成可能です。

```scala
import zio.ZIO
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

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
```

表明は否定(negation)、論理和(logical disjunction)、論理積（logical conjunction）で合成可能です。

```scala
import zio.ZIO
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

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
```

## テスト時の実行環境について

ZIOモナドの実行に実行環境が必要です。ZIO Testではテストコードは`zio.test.environment.TestEnvironment`環境で実行されます。`TestEnvironment`は通常のZIOの実行環境(Clockなど)がテスト版の実装`TestClock`に置き換えられています。そのため実行環境に依存したコードをテストする場合は注意が必要です。

以下、`Clock`に依存したテストケースです。テスト用`TestClock`は自身で時刻を進めないため、このテストは終了しません。

```scala
import zio.ZIO
import zio.clock.Clock
import zio.duration._
import zio.test.Assertion._
import zio.test._

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
```

このテストを終了させるためには`TestClock`の時刻を手動で調整する、または、通常のZIOの実行環境を使用する必要があります。手動で時刻を調整するためには`TestClock.adjust`メソッドを、通常のZIOの実行環境を利用するには`zio.test.environment.Live.live`関数を使用します。

```scala
import zio.ZIO
import zio.clock.Clock
import zio.duration._
import zio.test.Assertion._
import zio.test._

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
```

## アプリケーション固有の環境

ZIOの実行環境のほかにアプリケーション固有の環境でテストを実行できます。

以下DBアクセスが必要なアプリケーションのテストの模擬コードです。アプリケーション固有の`DatabaseAccess`にZIO Testのテスト環境`TestEnvironment`をmixinした環境をテストへ提供します。こうすることでテストコードが`DatabaseAccess`を使用できます。

```scala
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
```

## Test Aspectによるテスト制御

特定のテストをテスト対象から外す、テストをタイムアウトさせるなど、Test Aspectを利用するとテストの挙動を制御できます。

```scala
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
```

# 最後に

この記事ではZIO Testを利用したテストについて以下のことを紹介しました。

- 副作用のないコードのテスト
- 副作用のあるコードのテスト
- 表明`Assertion`について
- テスト結果と表明の合成
- テスト時の実行環境について
- アプリケーション固有の環境
- Test Aspectによるテスト制御

ZIO TestはZIOで作られたアプリケーションをテストするための最良の選択肢の1つです。ぜひ皆さんも試してください。

# 参考URL

- [ZIO Streams using IoT Sensor Emulation](https://timpigden.github.io/_pages/zio-streams/intro.html)
- [ZIO@discord](https://discord.gg/2ccFBr4)

# Footnote

*1: `zio.test.assertM`はZIOモナドの値に表明をmapするための糖衣構文で`zio.test.assert`でも副作用のあるコードに対して表明可能です。
*2: `fails`と`dies`の違いは [ZIOのエラー・モデルとエラー処理](https://qiita.com/MitsutakaTakeda/items/1510876f74704c3fe388)を参照してください。

```scala
object SpecWithSideEffectForFootNote extends DefaultRunnableSpec(
  testM("waiting 7.5 million years for the answer") {
    val answerToUltimateQuestion: UIO[Int] = UIO(42)
    answerToUltimateQuestion.map(v => assert(v, equalTo(42)))
  }
)
```
