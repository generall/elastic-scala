package ml.generall.elastic

import java.util

import com.sksamuel.elastic4s.{Hit, HitReader}

import scala.collection.JavaConversions._


case class Mention(
                    left: String,
                    middle: String,
                    right: String,
                    href: String,
                    var weight: Double,
                    source: String = ""
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

  implicit object MentionHitAs extends HitReader[Mention] {

    override def read(hit: Hit): Either[Throwable,Mention] = {
      //hit.sourceAsMap("name").toString, hit.sourceAsMap("location").toString
      val context = mapAsScalaMap(hit.sourceAsMap("context").asInstanceOf[util.HashMap[String, String]])
      Right(Mention(
        context("left"),
        context("middle"),
        context("right"),
        hit.sourceAsMap("concept").toString,
        // TODO: add set of score in methods
        0.0, //Deprecated hit.sourceAsMap("_source").asInstanceOf[Float],
        hit.sourceAsMap("source").asInstanceOf[String]
      ))
    }
  }

  implicit object TupleHitAs extends HitReader[String] {
    override def read(hit: Hit): Either[Throwable, String] = Right(hit.sourceAsMap("concept").toString)
  }


}
