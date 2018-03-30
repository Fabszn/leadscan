import org.flywaydb.play.PlayInitializer
import play.api.ApplicationLoader.Context
import play.api.db.{BoneCPComponents, DBComponents, Database}
import play.api.libs.mailer.MailerComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator, _}
import play.filters.cors.CORSComponents
import router.Routes
import services._


/**
  * Created by fsznajderman on 10/01/2017.
  */


class LeadTrackerApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach(_.configure(context.environment))
    new Components(context).application
  }
}


class Components(context: Context)
  extends BuiltInComponentsFromContext(context)
    with DBComponents
    with BoneCPComponents
    with AhcWSComponents
    with CORSComponents
    with MailerComponents {


  override lazy val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)


  val database: Database = dbApi.database("leadTrackerDb")

  implicit val es = new EventServiceImpl(database)
  val remote = new LocalRemoteClient(database, es)
  val ls = new LeadServiceImpl(database)
  val ns = new NotificationServiceImpl(database, mailerClient, remote)
  val ps = new PersonServiceImpl(database, ns, remote, es)
  val ss = new SponsorServiceImpl(database, es, wsClient,ps)
  val sts = new StatsServiceImpl(database)
  val as = new AuthServiceImpl(database, remote)

  val sys = new SyncServiceImpl(remote, es, database)


  val flyway = new PlayInitializer(configuration, environment, webCommands)


  lazy val router = new Routes(
    httpErrorHandler,
    new controllers.PersonController(ps),
    new controllers.LeadController(ls, ns, ps),
    new controllers.NotificationController(ns),
    new controllers.Status(ns),
    new controllers.AdminController(ps, ss, sts, ns, sys, remote,es),
    new controllers.SecurityController(as),
    new controllers.ImportController(ps, ss, es, remote, ns),
    new controllers.Assets(httpErrorHandler),
    new controllers.ReportsController(ss, ps, remote, es, ns, sts, ls),
    new controllers.SponsorsController(ss)

  )
}