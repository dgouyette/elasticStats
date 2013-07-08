package controllers

import play.api.mvc._
import models._
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet
import play.api.libs.json._
import java.util.concurrent.TimeUnit
import java.util.Locale
import java.text.SimpleDateFormat
import java.sql.Date
import org.elasticsearch.search.facet.statistical.StatisticalFacet
import org.elasticsearch.search.facet.range.RangeFacet
import models.StatsFacet
import models.Paiement
import models.Histo
import models.AnneeFacet
import models.MoisFacet
import models.RangeStatusCode


object Application extends Controller {

  implicit val datePaiementWrites = Json.writes[AnneeFacet]
  implicit val statsWrites = Json.writes[StatsFacet]
  implicit val monthstWrites = Json.writes[MoisFacet]
  implicit val rangeStatusCodeWrites = Json.writes[RangeStatusCode]
  implicit val serieWrites = Json.writes[Serie]
  implicit val seriePieWrites = Json.writes[SeriePie]
  implicit val titleWrites = Json.writes[Title]
  implicit val subtitleWrites = Json.writes[Subtitle]
  implicit val xaxisWrites = Json.writes[Xaxis]
  implicit val highChartWrites = Json.writes[AreaChart]
  implicit val pieChartWrites = Json.writes[PieChart]
  implicit val histoWrites = Json.writes[Histo]


  val settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).build()
  val client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300))


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }



  def annees = Action {
    Ok(views.html.annees("Liste"))
  }

  def statuts = Action {
    Ok(views.html.statuts("Liste"))
  }





  def stats = Action {
    val statFacets: StatisticalFacet = paiementsJson().getFacets.facetsAsMap().get("statisticalFacet").asInstanceOf[StatisticalFacet]
    Ok(Json.toJson(StatsFacet(statFacets.getCount, statFacets.getMin, statFacets.getMax, statFacets.getMean, statFacets.getTotal)))
  }


  def statutsJson = Action{

    val rangeFacets: RangeFacet = paiementsJson().getFacets.facetsAsMap().get("rangeFacet").asInstanceOf[RangeFacet]

    val statusPaiement = for (i <- 0 to rangeFacets.getEntries.size() - 1)
    yield {
      rangeFacets.getEntries.get(i).getCount
    }

    //val hightChart =   PieChart(Title("Statuts des paiements"), Subtitle(""),Xaxis(List("Ok", "Ko")), List(SeriePie("", statusPaiement.toList )))

    val list =  List(("Ok", 45), ("Ko", 66))

    Ok(views.html.statutsJson("Statuts des paiements", "", statusPaiement.toList)).as(JSON)
  }





  def anneesJson = Action {
    val years = paiementsJson().getFacets.facetsAsMap().get("years").asInstanceOf[DateHistogramFacet]

    val annees = for (i <- 0 to years.getEntries.size() - 1)
    yield {
      (new Date(years.getEntries.get(i).getTime).getYear + 1900).toString
    }


    val count = for (i <- 0 to years.getEntries.size() - 1)
    yield {
      years.getEntries.get(i).getCount
    }

    val hightChart =   AreaChart(Title("Réservations par année"), Subtitle(""),Xaxis(annees.toList), List(Serie("", count.toList)))
    Ok(Json.toJson(hightChart))
  }

  def years = Action {
    val years = paiementsJson().getFacets.facetsAsMap().get("years").asInstanceOf[DateHistogramFacet]



    val annees = for (i <- 0 to years.getEntries.size() - 1)
    yield {

      val jsYear = JsNumber(new Date(years.getEntries.get(i).getTime).getYear + 1900)

      val jsCount = JsNumber(years.getEntries.get(i).getCount)
      JsArray().append(jsYear).append(jsCount)
    }

    val metadata = JsArray().append(JsString("Années")).append(JsString("Count"))




    Ok(Json.toJson(annees))
  }


  def months = Action {
    val months = paiementsJson().getFacets.facetsAsMap().get("months").asInstanceOf[DateHistogramFacet]
    val monthsPaiements = for (i <- 0 to months.getEntries.size() - 1)
    yield {
      val entry: DateHistogramFacet.Entry = months.getEntries.get(i)
      MoisFacet(new SimpleDateFormat("MMMMMMM", Locale.FRANCE).format(new Date(entry.getTime)), new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date(entry.getTime)), entry.getCount)
    }
    Ok(Json.toJson(monthsPaiements))
  }


  def extractFacet = {
    Ok("")
  }


  def paiementsJson() = {


    val statisticalFacet = FacetBuilders.statisticalFacet("statisticalFacet")
      .field("montant")

    val rangeFacet = FacetBuilders.rangeFacet("rangeFacet")
      .field("responseCode")
      .addRange(0, 1)
      .addRange(1, 96)
      /**.addRange(5, 6)
      .addRange(17, 18)
      .addRange(75, 76)
      .addRange(90, 91)**/

    val histoFacet = FacetBuilders.histogramFacet("histoFacet")
      .field("montant")
      .field("responseCode")
      .interval(1, TimeUnit.DAYS)




    val responseDateHistogramme = client.prepareSearch()
      .setQuery(QueryBuilders.matchAllQuery())
      .addFacet(FacetBuilders.dateHistogramFacet("years").field("datePaiement").interval("year"))
      .addFacet(FacetBuilders.dateHistogramFacet("months").field("datePaiement").interval("month"))
      .addFacet(statisticalFacet)
      .addFacet(histoFacet)
      .addFacet(rangeFacet)
      .execute().actionGet()

    responseDateHistogramme


    /** val yearsFacets: DateHistogramFacet = responseDateHistogramme.getFacets.facetsAsMap().get("years").asInstanceOf[DateHistogramFacet]
    val monthsFacets: DateHistogramFacet = responseDateHistogramme.getFacets.facetsAsMap().get("months").asInstanceOf[DateHistogramFacet]
    val statFacets: StatisticalFacet = responseDateHistogramme.getFacets.facetsAsMap().get("statisticalFacet").asInstanceOf[StatisticalFacet]
    val rangeFacets: RangeFacet = responseDateHistogramme.getFacets.facetsAsMap().get("rangeFacet").asInstanceOf[RangeFacet]
    val histoFacets: HistogramFacet = responseDateHistogramme.getFacets.facetsAsMap().get("histoFacet").asInstanceOf[HistogramFacet]
      * */


    /** println("count = " + statFacets.getCount)
    println("max = " + statFacets.getMax)
    println("min = " + statFacets.getMin)
    println("mean = " + statFacets.getMean)
    println("total = " + statFacets.getTotal)
      * */

    /** val histo  = for (i <- 0 to histoFacets.getEntries.size() - 1)
      yield {
      val entry : HistogramFacet.Entry = histoFacets.getEntries.get(i)
       Histo(entry.getKey, entry.getCount)
    }

    val statusPaiement = for (i <- 0 to rangeFacets.getEntries.size() - 1)
    yield {
      val entry: RangeFacet.Entry = rangeFacets.getEntries.get(i)
      RangeStatusCode(entry.getFrom, entry.getCount)
    }  **/


    /** val yearsPaiements = for (i <- 0 to yearsFacets.getEntries.size() - 1)
    yield {
      val entry: DateHistogramFacet.Entry = yearsFacets.getEntries.get(i)
      AnneeFacet(new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date(entry.getTime)), entry.getCount)
    }

    val monthsPaiements = for (i <- 0 to monthsFacets.getEntries.size() - 1)
    yield {
      val entry: DateHistogramFacet.Entry = monthsFacets.getEntries.get(i)
      MoisFacet(new SimpleDateFormat("MMMMMM", Locale.FRANCE).format(new Date(entry.getTime)), new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date(entry.getTime)), entry.getCount)
    } **/


  }


  def indexPaiement() = Action {

    val paiements: List[Paiement] = PaiementDao.findAll

    //val node = nodeBuilder().client(true).node()



    paiements.map {
      p =>
        client.prepareIndex("paiements", "paiement").setSource(jsonBuilder()
          .startObject()
          .field("datePaiement", p.datePaiement)
          .field("montant", (p.montant / 100))
          .field("responseCode", p.responseCode)
          .field("typeCarte", p.typeCarte)
          .endObject()
        ).execute().actionGet()
    }
    val size: Int = paiements.size


    Ok(size.toString)
  }

}


