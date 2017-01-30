import controllers.NotificationController
import org.flywaydb.play.PlayInitializer
import play.api.ApplicationLoader.Context
import play.api.db.{BoneCPComponents, DBComponents, Database}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator, _}
import router.Routes
import services.{LeadServiceImpl, NotificationServiceImple, PersonServiceImpl}


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
    with AhcWSComponents {



  val database: Database = dbApi.database("leadTrackerDb")
  val ps = new PersonServiceImpl(database)
  val ls = new LeadServiceImpl(database)
  val ns = new NotificationServiceImple(database)

  val flyway = new PlayInitializer(configuration, environment, webCommands)


  lazy val router = new Routes(
    httpErrorHandler,
    new controllers.PersonController(ps),
    new controllers.LeadController(ls,ns),
    new controllers.NotificationController(ns),
    new controllers.Status()

  )
}