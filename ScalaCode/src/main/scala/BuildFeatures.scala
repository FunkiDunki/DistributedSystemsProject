import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.types.DoubleType

object BuildFeatures {
    def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Build Features for KMeans")
      .master("local[*]") // Use all available cores
      .getOrCreate()

    import spark.implicits._

    val targetNaics = "------" //change this value if specific naics requested

    val businessDF = spark.read
        .option("header", "true")
        .csv("..Data/zipcodeBusinessPatterns.txt")
        .withColumn("zip", F.lpad($"zip", 5, "0"))
        .withColumn("est", $"est".cast(DoubleType))
        .filter($"naics" === targetNaics)

    val popIncDF = spark.read
        .option("header", true)
        .csv("../Data/zip_population_income_2022.csv")
        .withColumnRenamed("ZCTA20", "zip")
        .withColumn("TOTPOP", $"TOTPOP".cast(DoubleType))
        .withColumn("MEDFAMINC", $"MEDFAMINC".cast(DoubleType))

    //join on zip and divide by pop
    val joinedDF = businessDF
        .join(censusDF, Seq("zip"))
        .filter($"TOTPOP" > 0)

    val featuresDF = joinedDF
      .withColumn("est_per_capita", $"est" / $"TOTPOP")
      .select("zip", "stabbr", "cty_name", "est_per_capita", "MEDFAMINC")

    featuresDF.show(10, truncate = false)

    spark.stop()
    
    }
}