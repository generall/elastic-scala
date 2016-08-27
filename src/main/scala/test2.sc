def smartAssigning(information: Array[Array[String]]): String = {
  information.filter(x => x(1) == "1").sortBy( arr => {
    val name = arr(0)
    val status = arr(1)
    val projects = arr(2)
    val tasks = arr(3)
    (tasks, projects)
  } ).apply(0).apply(0)
}

