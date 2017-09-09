import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.pipe
import akka.util.Timeout
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import scala.concurrent.Future
import scala.concurrent.duration._

object TC {

  import com.danielasfregola.twitter4s.entities.{ Tweet, User}
  import com.danielasfregola.twitter4s.TwitterRestClient
  import com.danielasfregola.twitter4s.entities.enums.ResultType
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  val RestClient : TwitterRestClient = TwitterRestClient()

  // helpers
  def followersForUser(screen_name: String): Future[Seq[User]] =
  // map: Future[RatedData[Users]] => Future[Seq[User]]
    RestClient.followersForUser(screen_name).map( _.data.users )

  def userTimelineForUser(user: User): Future[(User, Seq[Tweet])] =
  // map: Future[RatedData[Seq[Tweet]]] => Future[(User, Seq[Tweet])]
    RestClient.userTimelineForUser(user.screen_name).map( r => (user -> r.data) )

  def searchPeopleByTweets(query: String): Future[Seq[User]] =
  // map: Future[RatedData[StatusSearch]] => Future[Seq[User]]
    RestClient.searchTweet( query
      , count = 15 // : Int
      , include_entities = true // : Boolean
      , result_type = ResultType.Mixed // : ResultType
      , geocode = None     // : Option[GeoCode]
      , language = None    // : Option[Language]
      , locale = None      // : Option[String]
      , until = None       // : Option[LocalDate]
      , since_id = None    // : Option[Long]
      , max_id = None      // : Option[Long]
      , callback = None )  // : Option[String]
      .map { r =>
      for {
        tweet <- r.data.statuses
        user <- tweet.user
      } yield  user
    }
}

class TwitterService(username: String) extends Actor with ActorLogging  {
  import TwitterService._

  override def preStart(): Unit = {
    log.info("Actor TwitterService started")
    implicit val exec = context.dispatcher
    implicit val timeout = Timeout(30 seconds)

    lazy val algorithm: Future[Seq[(User, Seq[Tweet])]]  =
      for {
        followers <- TC.followersForUser(username)
        result <- Future.sequence(followers map TC.userTimelineForUser)
      } yield result

    algorithm map { x => Result(x) } pipeTo self
  }
  override def postStop(): Unit = log.info("Actor TwitterService stopped")

  override def receive: Receive = {
    case Result(result)  =>
      log.info("TwitterService Success")
      val allTweets = result.toList.map( _._2.mkString(".\n") ).mkString(".\n")
      log.info("Produce AllTweets and send Payload to Parent")
      context.parent ! Payload(allTweets)
      context.stop(self)
    case Status.Failure(cause) =>
      log.info("Fail")
      context.parent ! Issue(cause.getMessage)
      context.stop(self)
  }
}

object TwitterService {
  def props(username: String) = Props(new TwitterService(username))
  final case class Issue(cause: String)
  final case class Payload(allTweets: String)
  final case class Result(result: Seq[(User, Seq[Tweet])])
}

