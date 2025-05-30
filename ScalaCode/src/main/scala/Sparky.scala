import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

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
    val rdd = sc.parallelize(List(1, 2, 3, 4))

    rdd.collect()
      .foreach(println)

  }
}
