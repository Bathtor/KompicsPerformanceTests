package se.sics.kompics.performancetest.launcher

import akka.actor.ActorSystem

object EmptySystem extends App {
	val system = ActorSystem("testSystem");
}
