package services

import dao.LeadDAO
import dao.LeadDAO.Item
import play.api.db.Database


/**
  * Created by fsznajderman on 22/02/2017.
  */

case class StatsData(leadsDateTime: Seq[Item], sponsorStat: Seq[(Int, String)])

trait StatsService {

  def getData: StatsData

}


class StatsServiceImpl(db: Database) extends StatsService {
  override def getData: StatsData = {
    db.withConnection { implicit c =>
      StatsData(LeadDAO.leadByhour, LeadDAO.leadBySponsor)
    }


  }
}
