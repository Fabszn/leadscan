import play.api.ApplicationLoader.Context
import play.api.db.{BoneCPComponents, DBComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator, _}
import router.Routes


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


  val database = dbApi.database("leadTracker_DB")

  lazy val router = new Routes(
    httpErrorHandler,
    new controllers.Index(database)
  )
}