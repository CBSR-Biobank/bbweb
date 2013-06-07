import infrastructure._

object test {
  val x = new ValueObjectList[Int]                //> x  : infrastructure.ValueObjectList[Int] = infrastructure.ValueObjectList@2ef
                                                  //| b56b1
  x.add(1)
  x.add(2)
  x.add(3)
  x.getList foreach (x => println(x))             //> 3
                                                  //| 2
                                                  //| 1
  x.remove(2)
  x.getList foreach (x => println(x))             //> 3
                                                  //| 1
}