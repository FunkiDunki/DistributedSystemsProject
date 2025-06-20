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

    val sample_size = 100
    val k = 3
    val init_pts = sc.textFile(f"../Data/features_std_sample_${sample_size}.csv")
      .map(line => {
        line.split(",")
      })
      .filter(
        {tokens => tokens.head != ""}
      )
      .map(tokens => {
        (
          tokens.head,
          Point(List(tokens(4).toDouble, tokens(5).toDouble))
        )
      })
      .persist() //persisting because points never move
    //example point
    println(init_pts.collect().head)

    //val init_pts = sc.textFile("src/main/resources/datapoints")

    var cent_positions = sc.textFile(s"../Data/features_std_k${k}.csv")
      .map(line => {
        line.split(",")
      })
      .filter(
        {tokens => tokens.head != ""}
      )
      .map(tokens => {
        (
          tokens.head,
          Point(List(tokens(4).toDouble, tokens(5).toDouble))
        )
      })
      .persist() //persisting because points never move

    var cnt_pnts = init_pts.cartesian(cent_positions)
      .map(
        {case ((pid, pnt), (cid, cnt)) => (pid, (cid, DistanceSqr(pnt, cnt)))}
      )
      .reduceByKey({
        case ((cnt1, d1), (cnt2, d2)) => (if (d1 < d2) (cnt1, d1) else (cnt2, d2))
      })
      .map({
        case (pnt, (cnt, _)) => (cnt, pnt)//f"${pnt}%s, ${cnt}%s"
      })

    for (x <- (1 to 10)) {


      cent_positions = cnt_pnts.map({case (c, p) => (p, c)})
        .join(init_pts)
        .map({case (pid, (c, pos)) => (c, (1, pos))}) // 1 is for counting in next step:
        .reduceByKey(
          {case ((count1, p1), (count2, p2)) =>
            (
              count1+count2,
              Point(p1.pos.zip(p2.pos).map(
                {case (a, b) => a+b}
              ))
            )}
        )
        .mapValues(
          {case (count, summed) => Point(summed.pos.map(_/count))}
        )


      cnt_pnts = init_pts.cartesian(cent_positions)
        .map(
          {case ((pid, pnt), (cid, cnt)) => (pid, (cid, DistanceSqr(pnt, cnt)))}
        )
        .reduceByKey({
          case ((cnt1, d1), (cnt2, d2)) => (if (d1 < d2) (cnt1, d1) else (cnt2, d2))
        })
        .map({
          case (pnt, (cnt, _)) => (cnt, pnt)//f"${pnt}%s, ${cnt}%s"
        })

    }
    cnt_pnts.sortByKey(ascending = true, 1).saveAsTextFile(f"output/labels_k${k}_sample${sample_size}")
    cent_positions.sortByKey(ascending = true, 1).saveAsTextFile(f"output/centroids_k${k}_sample${sample_size}")
  }
}

