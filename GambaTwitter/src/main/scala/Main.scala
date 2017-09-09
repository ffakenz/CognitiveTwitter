import akka.actor.{ActorSystem, Props, Actor, ActorLogging}
import scala.io.StdIn
object Main extends App {
  import TwitterService._
  import WatsonService._
  import MainActor._


  val system = ActorSystem("GambaSystem")

  try {
    val main = system.actorOf(Props[MainActor], "main")
    main ! Go
    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  } finally {
    system.terminate()
  }



  object MainActor {
    final case object Go
  }
  class MainActor extends Actor with ActorLogging {
    import MainActor._
    override def preStart(): Unit = {
      log.info("Actor Main started")
    }
    override def postStop(): Unit = log.info("Actor Main stopped")

    override def receive: Receive = {
      case Go =>
        context.actorOf(TwitterService.props("FTestagrossa"), "twitter-service")
      case p: Payload =>
        val watsonService = system.actorOf(Props[WatsonService], "watson-service")
        watsonService ! p // TODO: forward
        log.info("waiting for Watson")
        context.become(WaitingWatson)
      case Issue(cause) => log.info(s"Shit: $cause")
    }

    def WaitingWatson : Receive = {
      case Profile(summary) =>
        log.info(summary)
        context.stop(self)
    }

  }




}
