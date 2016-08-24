

def f1() = {
  val l = List(1, 2, 3, 4, 5, 6, 7, 8, 9)
  l.view.map(x => {
    println("Map1: " ++ x.toString)
    x + 1
  }).map(x => {
    println("Map2: " ++ x.toString)
    x + 1
  }).isInstanceOf[Iterable[Int]]

  implicit def d2opt[T](t: T) = Option(t)
  def foo(x: Option[Double] = None) = {
    println(x.getOrElse(9))
  }

  foo()
  val i: Int = 1
  i.isInstanceOf[Double]
  i.isInstanceOf[Int]

  l.sum
}

val  l = List((1,2), (2,4), (2,5))

l.groupBy(_._1)


val m1 = Map("fname" -> "Al", "lname" -> "Alexander")

m1.map({case (key, value) => (key, value)})