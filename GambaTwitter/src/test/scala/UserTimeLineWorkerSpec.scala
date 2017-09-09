
/*import UserTimeLineWorker._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.duration._

import com.danielasfregola.twitter4s.entities.{Tweet}

class UserTimeLineWorkerSpec(_system: ActorSystem) extends TestKit(_system)
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("UserTimeLineWorkerTest"))

  override def afterAll() = shutdown(system)

  behavior of "Actor UserTimeLineWorker"
  ignore should "Return my last Tweets" in {
    // probe set up
    val probe = TestProbe()
    val client = probe.ref
    //validation
    val username = "ManuelBaraschi"
    system.actorOf(UserTimeLineWorker.props(username, client))
    val response = probe.expectMsgType[LastTweets]
    response.username should ===(username)
    //response.tweets foreach println
  }

  it should "Return last Tweets from 2 different ussers" in {
    // probe set up
    val probe = TestProbe()
    val client = probe.ref
    //validation
    val usernames = Seq("ManuelBaraschi", "ffakenz")

    val allTweets = scala.collection.mutable.ArrayBuffer.empty[Seq[Tweet]]

    usernames foreach { username =>
      system.actorOf(UserTimeLineWorker.props(username, client))
      val response = probe.expectMsgType[LastTweets](100 seconds)
      assert(usernames contains response.username)
      allTweets.append( response.tweets )
    }

    allTweets foreach { tweets =>
      tweets foreach { tweet =>
        val s : String = s"${tweet.user.get.}"
        println(s)
      }
    }
  }
}

*/