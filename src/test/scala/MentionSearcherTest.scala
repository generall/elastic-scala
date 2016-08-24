import org.scalatest.FunSuite

/**
  * Created by generall on 22.08.16.
  */
class MentionSearcherTest extends FunSuite {

  test("testFindMentions") {
    val searcher = new MentionSearcher("localhost", 9300)
    val res = searcher.findMentions("Titanic")
    res.variants.foreach(println)
    res.stats.foreach(println)
  }

  test("testFindHref"){
    val searcher = new MentionSearcher("localhost", 9300)
    searcher.findHref("http://en.wikipedia.org/wiki/Hot_air_balloon").foreach(x => println(x.sentence))
  }

}
