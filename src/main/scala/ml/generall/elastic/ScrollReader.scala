package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri, HitAs, RichSearchHit}
import com.sksamuel.elastic4s.ElasticDsl._
import scala.collection.JavaConversions._


/**
  * Created by generall on 02.10.16.
  */
class ScrollReader(
                    host: String = "localhost",
                    port: Int = 9300,
                    index: String = "wiki"
                  ) extends MentionSearcherAbs{


  var client = ElasticClient.transport(ElasticsearchClientUri(host, port))

  var scrollId: Option[String] = None

  var pool: List[Mention] = Nil

  def readScroll(lim: Int = 100) = {

    val res = scrollId match {
      case None => {
        val res = client.execute {
          search in index -> "mention" sort (
            field sort "_id"
            ) scroll "1m" limit lim
        }.await
        scrollId = res.scrollIdOpt
        res
      }
      case Some(thisScrollId) =>
        client.execute {
          search scroll thisScrollId
        }.await
    }

    val mentions = res.as[Mention].toList
    pool = mentions ++ pool
    mentions
  }
}
