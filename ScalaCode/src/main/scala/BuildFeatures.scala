import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{lpad, col}
import org.apache.spark.sql.types.DoubleType

object BuildFeatures {
    def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Build Features for KMeans")
      .master("local[*]")
      .getOrCreate()

    //Spark 2.4.8, we need to import this way
    import spark.implicits._

    val targetNaics = "------" //change this value if specific naics requested

    val businessDF = spark.read
        .option("header", "true")
        .csv("../Data/zipcodeBusinessPatterns.txt")
        .withColumn("zip", lpad(col("zip"), 5, "0"))
        .withColumn("est", col("est").cast(DoubleType))
        .filter(col("naics") === targetNaics)

    val popIncDF = spark.read
        .option("header", value = true)
        .csv("../Data/zip_population_income_2022.csv")
        .withColumnRenamed("ZCTA20", "zip")
        .withColumn("TOTPOP", col("TOTPOP").cast(DoubleType))
        .withColumn("MEDFAMINC", col("MEDFAMINC").cast(DoubleType))

    println("Business rows: " + businessDF.count())
    println("Census rows: " + popIncDF.count())

    val joinedDF = businessDF
        .join(popIncDF, Seq("zip"))
        .filter(col("TOTPOP") > 0)

    val featuresDF = joinedDF
      .withColumn("est_per_capita", col("est") / col("TOTPOP"))
      .select("zip", "stabbr", "cty_name", "est_per_capita", "MEDFAMINC")
        val cleanDF = featuresDF.na.drop(Array("MEDFAMINC", "est_per_capita"))

        println("Number of rows in DataFrame: " + cleanDF.count())
        println("Schema of DataFrame:")
        cleanDF.printSchema()

try {

    val csvData = cleanDF.collect()
      .map(_.mkString(","))
      .mkString("\n")

    val header = cleanDF.columns.mkString(",")
    val fullData = header + "\n" + csvData

    import java.io.{File, PrintWriter}
    val writer = new PrintWriter(new File("src/main/resources/features.csv"))
    writer.write(fullData)
    writer.close()

    println("File successfully written to features.csv")


} catch {
    case e: Exception =>
    println("Error writing to file:" + e.getMessage)
    e.printStackTrace()
}
        spark.stop()
    }
}