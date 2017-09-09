package helperFiles

case class Tweet()

import scala.collection.mutable.ArrayBuffer
//responsiveness: given an username will responde
import akka.actor.{ Actor, ActorRef, Props, ActorLogging }
class Workers extends Actor with ActorLogging {

  import Workers._
  import Worker._

  private var _tweets: Map[String, Seq[Tweet]] = Map.empty[String, Seq[Tweet]]
  private var _workers: ArrayBuffer[ActorRef] = ArrayBuffer.empty[ActorRef]
  private var _pending: Int = 0
  private var _start = System.currentTimeMillis()

  override def preStart(): Unit = {
    for( i <- 0 until 100) {
      val worker = context.actorOf(Worker.props, "Worker-{"+ _pending.toString + "}")
      _workers.append(worker)
    }
  }

  def receive : Receive = {
    case username: String => {
      //val worker = context.actorOf(Worker.props, s"Worker-{$_pending}"); _workers + worker
      _pending += 1
      val worker = _workers.remove(0)
      worker ! username // self
      _workers.append(worker)
    }
    case LastTweets(followerName, lastTweets) => {
      _tweets + (followerName -> lastTweets)
      _pending -= 1
      if(_pending == 0) {
        log.debug(s"Time taken: ${(System.currentTimeMillis - _start)/1.0e9}")
        context.stop(self) // context.system.shutdown()
      }
      // _workers - sender
    }
  }

}

object Workers { }

class Worker extends Actor {
  import Worker._

  var lastTweets : Seq[Tweet] = Seq.empty[Tweet]

  def receive : Receive = {
    case followerName: String => {
      // getLastTweetsFromUsername => lastTweets + (new Tweet)
      sender ! LastTweets(followerName, lastTweets)
    }
  }
}

object Worker {
  def props = Props(new Worker)
  case class LastTweets(followerName: String, lastTweets: Seq[Tweet])
}
