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


class MentionSearchResult(_vars: Iterable[ConceptVariant])(filterPredicate: (ConceptVariant => Boolean)) {
  val stats: Iterable[ConceptVariant] = {
    val avgs = _vars.map(_.avgScore)
    val avgsNorm = ProbTools.normalize(avgs)
    val avgsSotfMax = ProbTools.softMax(avgs)
    _vars.zip(avgsNorm.zip(avgsSotfMax)).map({ case (variant, (norm, softMax)) => {
      variant.avgSoftMax = softMax
      variant.avgNorm = norm
      variant
    }
    }).filter(filterPredicate)
  }
}

class MentionSearcher(
                       val host: String,
                       val port: Int,
                       val index: String = "wiki",
                       val thresholdCount: Int = 1,
                       val mentionLimit: Int = 25,
                       val hrefLimit: Int = 50
                     ) extends MentionSearcherAbs {

  var client: TcpClient = TcpClient.transport(ElasticsearchClientUri(host, port))


  def filterResult(x: ConceptVariant): Boolean = {
    if (x.count <= thresholdCount) {
      if (isDebug()) println(s"Filter concept: ${x.concept}")
      false
    } else true
  }

  def calcStat(concept: String, list: Iterable[(String, Float)]): ConceptVariant = {
    val size = list.size
    ConceptVariant(
      concept = concept,
      count = size,
      avgScore = list.foldLeft(0.0)(_ + _._2) / size,
      maxScore = list.maxBy(_._2)._2,
      minScore = list.minBy(_._2)._2
    )
  }

  def findMentions(mentionText: String): MentionSearchResult = {
    val resp = client.execute {
      search(index / "mention") query {
        matchQuery("anchor_text", mentionText)
      } limit mentionLimit
    }.await

    val resScores = resp.to[String].zip(resp.hits.map(_.score))
      .groupBy(_._1)
      .map({ case (concept, pairs) => calcStat(concept, pairs) })

    new MentionSearchResult(resScores)(filterResult)
  }

  def findHref(hrefToFind: String): List[Sentence] = {
    val resp = client.execute {
      search(index / "mention") query {
        matchQuery("concept", hrefToFind)
      } limit hrefLimit
    }.await
    resp.to[Mention].map(new Sentence(_)).toList
  }

  def findHrefWithContext(hrefToFind: String, leftContext: String, rightContext: String): List[Sentence] = {
    val resp = client.execute {
      search(index / "mention") query {
        boolQuery().should(
            matchQuery("context.left", leftContext),
            matchQuery("context.right", rightContext)
          ) filter termQuery("concept", hrefToFind)
      } limit hrefLimit
    }.await
    resp.to[Mention].map(new Sentence(_)).toList
  }

  def innerFieldRequest(str: String): List[Sentence] = {
    val resp = client.execute {
      search(index / "mention") query {
        matchQuery("context.right", str)
      }
    }.await
    resp.to[Mention].map(new Sentence(_)).toList
  }

  def customSearch[T](query: TcpClient => RichSearchResponse)(implicit hitas: HitReader[T], manifest: Manifest[T]): List[(T, Float)] = {
    val resp = query(client)
    resp.hits.map(x => (x.to[T], x.score)).toList
  }

}
