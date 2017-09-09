import akka.actor.{Actor, ActorLogging, Props, Status}

object WC {

  import com.ibm.watson.developer_cloud.personality_insights.v2.PersonalityInsights
  import com.ibm.watson.developer_cloud.personality_insights.v2.model.{Profile, Trait}
  import scala.collection.JavaConverters._
  val service = new PersonalityInsights()

  service.setUsernameAndPassword("72f0d57e-fc39-400e-9f62-24c2b7a2dfb8", "Or5wyVYYO2fW")

  def getProfile(text: String): Profile = service.getProfile(text).execute()

  def childrenOf(t: Trait): List[Trait] = t.getChildren.asScala.toList

  def summary(profile: Profile): String = {
    val profileChildren = childrenOf(profile.getTree)

    profileChildren.map { profileChild =>
      val childId = profileChild.getId
      val maxChild = childrenOf(profileChild).maxBy(_.getPercentage)
      val maxGrandChildren = childrenOf(maxChild).sortBy(_.getPercentage).take(3)
      val grandChildren = maxGrandChildren.map { grandChild =>
        f"${grandChild.getId}, ${grandChild.getPercentage}%1.1f"
      }.mkString(", ")

      s"$childId : $grandChildren"
    }.mkString(";\n")
  }

  def profile = getProfile _ andThen summary _
}

class WatsonService extends Actor with ActorLogging {
  import WatsonService._
  import TwitterService.{Payload}

  override def preStart(): Unit = log.info("Actor WatsonService started")
  override def postStop(): Unit = log.info("Actor WatsonService stopped")

  override def receive: Receive = {
    case Payload(payload: String)  =>
      log.info("Watson Service get Payload")
      val gambaProfile = WC.getProfile(payload)
      log.info("Watson Service resolved Profile")

      val gambaSummary = WC.summary(gambaProfile)

      sender ! Profile(gambaSummary)
      context.stop(self)
  }
}

object WatsonService {
  def props = Props(new WatsonService)
  final case class Profile(profile: String)
}

