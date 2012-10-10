package se.sics.kompics.scala.performancetest.kompics

//import java.security.MessageDigest
import se.sics.kompics.address.Address

object Utils {

//	val sha = MessageDigest.getInstance("SHA");
//
//	private def hash(some: String): BigInt = {
//		val bytes = some.getBytes();
//		val hashBytes = sha.digest(bytes);
//		return BigInt(hashBytes);
//	}

	def rank(ref: Address): Int = {
		return ref.getId();
	}

	implicit def pair2TimeRankPair(x: Pair[Int, Int]): TimeRankPair = new TimeRankPair(x._1, x._2);

	class TimeRankPair(ts: Int, wr: Int) {
		def largerThan(other: Pair[Int, Int]): Boolean = {
			if (ts > other._1) return true;
			if (ts == other._1) {
				if (wr > other._2) return true;
			}
			return false;
		}
	}
}

