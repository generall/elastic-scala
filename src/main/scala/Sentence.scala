

case class Chunk(text: String, href: String = null){

}

/**
  * Created by generall on 22.08.16.
  */
class Sentence() {

  case class Link(text: String, href: String) {  }

  var chunks: List[Chunk] = List()

  def sentence = chunks.map(_.text).mkString(" | ")

  def this(mention: Mention) = {
    this()
    chunks = Chunk(mention.left) :: Chunk(mention.middle, mention.href) :: Chunk(mention.right) :: Nil
  }
}
