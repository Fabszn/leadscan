import org.flywaydb.play.PlayInitializer
import play.api.ApplicationLoader.Context
import play.api.db.{BoneCPComponents, DBComponents, Database}
import play.api.libs.mailer.MailerComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator, _}
import play.filters.cors.CORSComponents
import play.libs.mailer.MailerClient
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

  //val filter = new Filters()

  override lazy val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)

  val database: Database = dbApi.database("leadTrackerDb")
  val remote = new MyDevoxxRemoteClient(wsClient)
  val ls = new LeadServiceImpl(database)
  val ns = new NotificationServiceImpl(database,mailerClient, remote)
  val ss = new SponsorServiceImpl(database)
  val sts = new StatsServiceImpl(database)
  val as = new AuthServiceImpl(remote)
  val ps = new PersonServiceImpl(database,ns, remote)




  val flyway = new PlayInitializer(configuration, environment, webCommands)


  lazy val router = new Routes(
    httpErrorHandler,
    new controllers.PersonController(ps),
    new controllers.LeadController(ls, ns, ps),
    new controllers.NotificationController(ns),
    new controllers.Status(ns),
    new controllers.AdminController(ps, ss, sts, remote),
    new controllers.SecurityController(as),
    new controllers.ImportController(ps, remote),
    new controllers.Assets(httpErrorHandler),
    new controllers.SponsorsController(ss)

  )
}