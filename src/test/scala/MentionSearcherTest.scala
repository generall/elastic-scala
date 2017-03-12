import ml.generall.elastic.{Mention, MentionSearcher}
import org.scalatest.FunSuite
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri, Hit, HitReader}


/**
  * Created by generall on 22.08.16.
  */
class MentionSearcherTest extends FunSuite {

  test("testFindMentions") {
    val searcher = new MentionSearcher("localhost", 9300)
    val res = searcher.findMentions("Titanic")
    res.stats.foreach(println)
  }

  test("testFindHref") {
    val searcher = new MentionSearcher("localhost", 9300)
    searcher.findHref("http://en.wikipedia.org/wiki/Hot_air_balloon").foreach(x => println(x.sentence))
  }

  test("testFindHrefContext") {
    val searcher = new MentionSearcher("192.168.1.44", 9300)
    searcher.findHrefWithContext("http://en.wikipedia.org/wiki/RMS_Titanic", "", "iceberg").foreach(x => println(x.sentence))
  }

  test("testInnerFieldRequest") {
    val searcher = new MentionSearcher("192.168.1.44", 9300)
    val res = searcher.innerFieldRequest("Titanic").foreach(x => println(x.sentence))
  }

  test("testCustomSearch") {
    val searcher = new MentionSearcher("localhost", 9300)

    case class MySentence(
                           sent: String
                         ) {}

    implicit object MentionHitAs extends HitReader[MySentence] {
      override def read(hit: Hit): Either[Throwable, MySentence] = {
        Right(MySentence(
          hit.sourceAsMap("sent").toString
        ))
      }
    }

    searcher.customSearch(client =>
      client.execute {
        search("sknn-data") query "batman" limit 10
      }.await
    ).foreach(println)


  }

}
