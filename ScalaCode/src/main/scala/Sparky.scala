import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}


case class Point(pos: List[Double])

object Sparky{
  def main(args: Array[String]): Unit = {
    // --- SETUP ---
    System.setProperty("hadoop.home.dir", "c:/winutils/")
    System.setProperty("spark.driver.host", "localhost")
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf().setAppName("ScalaSetup")
      .setMaster("local[4]")
    val sc = new SparkContext(conf)


    // --- ASSIGNMENT CODE ---

    def DistanceSqr(p1: Point, p2: Point): Double = {
      //calculate the squared distance between the two points
      return p1.pos.zip(p2.pos).map({
        case (x1, x2) => math.pow(x1 - x2, 2)
      }).sum
    }

    val init_pts = sc.textFile("src/main/resources/datapoints")
      .map(line => {
        val tokens = line.split(", ")
        (
          tokens.head,
          Point(tokens.tail.map(_.toDouble).toList)
        )
      }).persist() //persisting because points never move

    val init_cnts = sc.textFile("src/main/resources/centroids")
      .map(line => {
        val tokens = line.split(", ")
        (
          tokens.head.trim,
          Point(tokens.tail.map(_.toDouble).toList)
        )
      }) // no persist because centroids do move

    val cnt_pnts = init_pts.cartesian(init_cnts)
      .map(
        {case ((pid, pnt), (cid, cnt)) => (pid, (cid, DistanceSqr(pnt, cnt)))}
      )
      .reduceByKey({
        case ((cnt1, d1), (cnt2, d2)) => (if (d1 < d2) (cnt1, d1) else (cnt2, d2))
      })
      .map({
        case (pnt, (cnt, _)) => (cnt, pnt)//f"${pnt}%s, ${cnt}%s"
      })

    cnt_pnts.sortByKey(ascending = true, 1).saveAsTextFile("output/labels")
  }
}
