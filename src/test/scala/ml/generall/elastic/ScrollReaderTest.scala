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


  test("testUId1") {

    val searcher1 = new ScrollReader()
    val searcher2 = new ScrollReader()

    val res1 = searcher1.readScroll(10)
    val res2 = searcher2.readScroll(10, Some("mention#AVanPAb-ad22Fy4HN761"))

    res1.foreach(x => println(x.href))
    println(" --- ")
    res2.foreach(x => println(x.href))
  }

  test("testUId2") {

    val searcher1 = new ScrollReader()
    val searcher2 = new ScrollReader()

    searcher2.lastUid = Some("mention#AVanPAb-ad22Fy4HN761")

    val res1 = searcher1.readScroll(10)
    val res2 = searcher2.readScroll(10)

    res1.foreach(x => println(x.href))
    println(" --- ")
    res2.foreach(x => println(x.href))
  }

  test("testPages") {
    val searcher = new ScrollReader()
    (0 to 20) foreach {_ =>
      val res1 = searcher.readScroll(2)
      res1.foreach(x => println( (x.href, searcher.lastUid) ))
      println(" --- ")
    }

  }

}
