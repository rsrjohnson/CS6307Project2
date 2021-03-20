// Databricks notebook source
// MAGIC %scala
// MAGIC // import the text to DF
// MAGIC val movies =sc.textFile("/FileStore/tables/plot_summaries.txt")
// MAGIC   .map(_.split("	"))
// MAGIC   .map(c => (c(0),c(1)))
// MAGIC   .toDF("movieID","description")
// MAGIC val terms = spark.read.option("header","false").csv("/FileStore/tables/user_terms.txt")
// MAGIC movies.count()  // the total number of movieID is 42306=N, used for computer tf-idf weight

// COMMAND ----------

import org.apache.spark.sql.functions.explode
import org.apache.spark.ml.feature.Tokenizer

val tokenizer = new Tokenizer().setInputCol("description").setOutputCol("Words")
val wordsData = tokenizer.transform(movies)


// COMMAND ----------

val flattened = wordsData.withColumn("token",explode($"Words")) // explode the df by token
import org.apache.spark.sql.functions.regexp_replace
import org.apache.spark.sql.functions.length

val dropstop = flattened.select(flattened.col("*")).where(length(flattened.col("token")) >5 ) // remove the stop words by length <= 4

val dropstopnew = dropstop.withColumn("newtoken", regexp_replace(dropstop("token"), """[\p{Punct}&&[^.]]""", "")) // remove the punctuation

// COMMAND ----------

// calculate the TF
val TF = dropstopnew.groupBy("movieID", "newtoken").count().as("tf").toDF("movieID","newtoken","tf")

// COMMAND ----------

TF.orderBy($"tf".desc).show()

// COMMAND ----------

// calculate the DF
val dropdup = dropstopnew.distinct()  // keep only one distinct movieID-token 
val DF = dropdup.groupBy("newtoken").count().as("df").toDF("newtoken","df")

// COMMAND ----------

import org.apache.spark.sql.functions.countDistinct
import sqlContext.implicits._
import org.apache.spark.sql.functions._
// calculate the DF
val DF = dropstopnew.select("movieID","newtoken")
        .groupBy("newtoken")
        .agg(countDistinct("movieID")).as("df").toDF("newtoken","df")
DF.show()

// COMMAND ----------

DF.show()

// COMMAND ----------

TF.show()

// COMMAND ----------

DF.show()

// COMMAND ----------

// calculate idf
import scala.math._
import org.apache.spark.sql.functions.{col, udf}
val calidf = (df: Long) => {
  log10(42306)/log10(df)
}
spark.udf.register("calidf", calidf)
val calcIdfUdf = udf { df: Long => calidf(df) }
val newdf = DF.withColumn("idf", calcIdfUdf(col("df")))


// COMMAND ----------

newdf.show()

// COMMAND ----------

// join the TF and newdf, calculate the tf-idf

val tf_idf = TF
      .join(newdf, Seq("newtoken"), "left")
      .withColumn("tf_idf", col("tf") * col("idf"))

// COMMAND ----------

tf_idf.show()


// COMMAND ----------

tf_idf.orderBy($"tf_idf".desc).show()

// COMMAND ----------

spark.sql("select tf_idf.movieId, dsn.description from tf_idf join on dropstopnew dsn where tf_idf.movieID = dsn.movieID and tf_idf.token ="horror" limit(10) .show()


select * from (select movieID from (select Top 10 movieID from tf_idf where token ="horror" group By movieID token order By tf_idf desc)tf_idf where token ="horror" and ) where tf_idf.movieID = dsn.movieID

// COMMAND ----------

spark sql("select Top 10 * from tf_idf where newtoken = "horror" group By movieID newtoken order By tf_idf desc").show()

// COMMAND ----------

spark sql("select * from tf_idf where token ='horror'").show()

// COMMAND ----------

import scala.math.abs

// COMMAND ----------

log(100)

// COMMAND ----------



// COMMAND ----------

log100

// COMMAND ----------

log10(100)

// COMMAND ----------

