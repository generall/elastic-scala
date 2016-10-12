package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.text.Text
import scala.collection.JavaConversions._
import scala.reflect.io.File


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
  var lastUid: Option[String] = None

  def makeRequest(lim: Int, fromUId: Option[String]) = scrollId match {
    case None => {
      println(fromUId.orElse(lastUid))
      val res = client.execute {
        fromUId.orElse(lastUid) match {
          case Some(uid) => search in index -> "mention" query {
            bool(
              filter(
                rangeQuery("_uid") from uid
              )
            )
          } sort ( field sort "_uid" ) scroll "10m" size lim
          case None => search in index -> "mention" sort ( field sort "_uid" ) scroll "10m" size lim
        }
      }.await
      scrollId = res.scrollIdOpt
      res
    }
    case Some(thisScrollId) =>
      client.execute {
        search scroll thisScrollId keepAlive "10m"
      }.await
  }

  def processRequest(lim: Int = 100, fromUId: Option[String] = None): RichSearchResponse = {
    if (currentLimit == 0)
      currentLimit = lim
    else
      assert(currentLimit == lim)
    val res: RichSearchResponse = makeRequest(lim, fromUId)
    lastUid = Some(res.getHits.head.sortValues().head.asInstanceOf[Text].string())
    currentOffset += res.getHits.size
    res
  }

  def readScroll(lim: Int = 100, fromUId: Option[String] = None) = {
    val res = processRequest(lim, fromUId)
    val mentions = res.as[Mention].toList
    pool = mentions ++ pool
    mentions
  }

  def skip(lim: Int = 100) = {
    processRequest(lim)
  }

  def skipMany(times: Int, lim: Int = 100) = {
    (1 to times).foreach(_ => skip(lim))
  }

  def dispatch(): Option[Mention] = {
    this.synchronized {
      if (pool.isEmpty) {
        readScroll()
        File("./dispatcher_offset.txt").appendAll(s"$currentOffset $lastUid\n")
      }
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
