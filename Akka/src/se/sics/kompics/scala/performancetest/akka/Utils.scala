package se.sics.kompics.scala.performancetest.akka

import java.security.MessageDigest
import akka.actor.ActorRef

object Utils {

	val sha = MessageDigest.getInstance("SHA");

	private def hash(some: String): BigInt = {
		val bytes = some.getBytes();
		val hashBytes = sha.digest(bytes);
		return BigInt(hashBytes);
	}

	def rank(ref: ActorRef): BigInt = {
		return hash(ref.path.toString());
	}

	implicit def pair2TimeRankPair(x: Pair[Int, BigInt]): TimeRankPair = new TimeRankPair(x._1, x._2);

	class TimeRankPair(ts: Int, wr: BigInt) {
		def largerThan(other: Pair[Int, BigInt]): Boolean = {
			if (ts > other._1) return true;
			if (ts == other._1) {
				if (wr > other._2) return true;
			}
			return false;
		}
	}
}

