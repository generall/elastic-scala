import java.util

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri, HitAs, RichSearchHit}

import scala.collection.JavaConversions._

/**
  * Created by generall on 22.08.16.
  */


case class Mention(left: String, middle: String, right: String, href: String, weight: Double) {

}

case class ConceptVariant(
                           concept: String,
                           count: Int,
                           avgScore: Double,
                           maxScore: Double,
                           minScore: Double
                         ) {}

class MentionSearchResult(_vars: Iterable[ConceptVariant]){
  val variants = _vars


  val stats = {
    val avgs = _vars.map(_.avgScore)
    val avgsNorm = ProbTools.normalize(avgs)
    val avgsSotfMax = ProbTools.softMax(avgs)
    _vars.map(_.concept).zip( avgsNorm.zip(avgsSotfMax) ).toMap
  }



}

class MentionSearcher(host: String, port: Int) {

  implicit object MentionHitAs extends HitAs[Mention] {
    override def as(hit: RichSearchHit): Mention = {
      //hit.sourceAsMap("name").toString, hit.sourceAsMap("location").toString
      val context = mapAsScalaMap(hit.sourceAsMap("context").asInstanceOf[util.HashMap[String, String]])
      Mention(
        context("left"),
        context("middle"),
        context("right"),
        hit.sourceAsMap("concept").toString,
        hit.getScore
      )
    }
  }

  implicit object TupleHitAs extends HitAs[(String, Float)] {
    override def as(hit: RichSearchHit): (String, Float) = {
      (hit.sourceAsMap("concept").toString, hit.getScore)
    }
  }

  var client = ElasticClient.transport(ElasticsearchClientUri("localhost", 9300))


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
      search in "wiki" -> "mention" query {
        matchQuery("anchor_text", mentionText)
      }
    }.await

    val resScores = resp.as[(String, Float)]
      .groupBy(_._1)
      .map({ case (concept, pairs) => calcStat(concept, pairs) })

    new MentionSearchResult(resScores)
  }

  def findHref(hrefToFind: String): List[Sentence] = {
    val resp = client.execute {
      search in "wiki" -> "mention" query {
        matchQuery("concept", hrefToFind)
      }
    }.await
    resp.as[Mention].map(new Sentence(_)).toList
  }

}
