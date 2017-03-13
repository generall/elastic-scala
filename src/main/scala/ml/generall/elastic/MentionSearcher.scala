package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticsearchClientUri, HitReader, TcpClient}
import com.sksamuel.elastic4s.searches.RichSearchResponse
import ml.generall.isDebug

import scala.collection.JavaConversions._

/**
  * Created by generall on 22.08.16.
  */

class MentionSearcher(
                       val host: String,
                       val port: Int,
                       val index: String = "wiki",
                       val thresholdCount: Int = 1,
                       val mentionLimit: Int = 25,
                       val hrefLimit: Int = 50
                     ) extends MentionSearcherAbs {

  var client: TcpClient = TcpClient.transport(ElasticsearchClientUri(host, port))

  def customSearch[T](query: TcpClient => RichSearchResponse)(implicit hitas: HitReader[T], manifest: Manifest[T]): List[(T, Float)] = {
    val resp = query(client)
    resp.hits.map(x => (x.to[T], x.score)).toList
  }

}
