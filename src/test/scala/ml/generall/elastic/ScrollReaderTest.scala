package ml.generall.elastic

import org.scalatest.FunSuite

/**
  * Created by generall on 02.10.16.
  */
class ScrollReaderTest extends FunSuite {

  test("testReadScroll") {
    val searcher = new ScrollReader()


    val res1 = searcher.readScroll(10)
    val res2 = searcher.readScroll(10)

    res1.foreach(x => println(x.href))
    println(" --- ")
    res2.foreach(x => println(x.href))


  }

}
