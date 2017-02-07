import com.google.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter

/**
  * Created by fsznajderman on 07/02/2017.
  */
class Filters @Inject() (corsFilter: CORSFilter)
  extends DefaultHttpFilters(corsFilter){
  println("test")
}
