import ml.generall.elastic.{Mention, MentionSearcher}
import org.scalatest.FunSuite
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri, Hit, HitReader}


/**
  * Created by generall on 22.08.16.
  */
class MentionSearcherTest extends FunSuite {

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
