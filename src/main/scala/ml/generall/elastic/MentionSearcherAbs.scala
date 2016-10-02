package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s.{RichSearchHit, HitAs}

import scala.collection.JavaConversions._



case class Mention(
                    val left: String,
                    val middle: String,
                    val right: String,
                    val href: String,
                    val weight: Double,
                    val source: String = ""
                  ) {

}

case class ConceptVariant(
                           concept: String,
                           count: Int = 1,
                           avgScore: Double = 1.0,
                           maxScore: Double = 1.0,
                           minScore: Double = 1.0,
                           var avgNorm: Double = 0.0,
                           var avgSoftMax: Double = 0.0
                         ) {}


/**
  * Created by generall on 02.10.16.
  */
trait MentionSearcherAbs {

  implicit object MentionHitAs extends HitAs[Mention] {
    override def as(hit: RichSearchHit): Mention = {
      //hit.sourceAsMap("name").toString, hit.sourceAsMap("location").toString
      val context = mapAsScalaMap(hit.sourceAsMap("context").asInstanceOf[util.HashMap[String, String]])
      Mention(
        context("left"),
        context("middle"),
        context("right"),
        hit.sourceAsMap("concept").toString,
        hit.getScore,
        hit.sourceAsMap("source").asInstanceOf[String]
      )
    }
  }

  implicit object TupleHitAs extends HitAs[(String, Float)] {
    override def as(hit: RichSearchHit): (String, Float) = {
      (hit.sourceAsMap("concept").toString, hit.getScore)
    }
  }


}
