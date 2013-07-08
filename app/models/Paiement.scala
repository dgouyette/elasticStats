package models

import java.sql.Date


import scala.slick.session.Database.threadLocalSession
import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import play.api.Play.current

case class Paiement(id: Option[Long], datePaiement: Date, montant: Double, responseCode: Int, typeCarte: String)

case class AnneeFacet(annee: Int, count: Long)

case class MoisFacet(mois: String, annee: String, count: Long)

case class StatsFacet(count: Long, min: Double, max: Double, mean: Double, total: Double)

case class RangeStatusCode(statusCode: Double, count: Long)

case class Histo(key: Long, count: Long)

case class AreaChart(title: Title, subtitle: Subtitle,xAxis : Xaxis, series: List[Serie])

case class PieChart(title: Title, subtitle: Subtitle,xAxis : Xaxis, series: List[SeriePie])

case class Title(text: String)

case class Subtitle(text: String)

case class Xaxis(categories : List[String])

case class Serie(name: String, data: List[Long])
case class SeriePie(typeq : String,name: String, data: List[Long])


object PaiementDao extends Table[Paiement]("paiement") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def datePaiement = column[Date]("datePaiement")

  def montant = column[Double]("montant")

  def responseCode = column[Int]("responseCode")

  def typeCarte = column[String]("typeCarte")

  def * = id.? ~ datePaiement ~ montant ~ responseCode ~ typeCarte <>(Paiement, Paiement.unapply _)

  lazy val database = Database.forDataSource(DB.getDataSource())


  def findAll() = database withSession {
    (for (c <- PaiementDao.sortBy(_.datePaiement)) yield c).list.reverse
  }


}