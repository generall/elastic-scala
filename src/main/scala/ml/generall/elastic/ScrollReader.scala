package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import scala.collection.JavaConversions._


/**
  * Created by generall on 02.10.16.
  */
class ScrollReader(
                    host: String = "localhost",
                    port: Int = 9300,
                    index: String = "wiki"
                  ) extends MentionSearcherAbs {


  var client = ElasticClient.transport(ElasticsearchClientUri(host, port))

  var scrollId: Option[String] = None

  var pool: List[Mention] = Nil

  var currentOffset = 0
  var currentLimit = 0

  def makeRequest(lim: Int) = scrollId match {
    case None => {
      val res = client.execute {
        search in index -> "mention" sort (
          field sort "_id"
          ) scroll "10m" limit lim
      }.await
      scrollId = res.scrollIdOpt
      res
    }
    case Some(thisScrollId) =>
      client.execute {
        search scroll thisScrollId keepAlive "10m"
      }.await
  }

  def processRequest(lim: Int = 100): RichSearchResponse = {
    if (currentLimit == 0)
      currentLimit = lim
    else
      assert(currentLimit == lim)
    val res = makeRequest(lim)
    currentOffset += res.getHits.size
    res
  }

  def readScroll(lim: Int = 100) = {
    val res = processRequest(lim)
    val mentions = res.as[Mention].toList
    pool = mentions ++ pool
    mentions
  }

  def skip(lim: Int = 100) = {
    processRequest(lim)
  }

  def dispatch(): Option[Mention] = {
    this.synchronized {
      if (pool.isEmpty) readScroll()
      pool match {
        case head :: tail => {
          pool = tail
          Some(head)
        }
        case Nil => None
      }
    }
  }
}
