package se.sics.kompics.scala.performancetest.akka

import akka.actor.Actor
import java.io.File
import java.util.Date
import java.io.FileWriter

case class ProfilingMessage(ts: Long, id: String, time: Long);

class ProfilerSubscriber(prefix: String) extends Actor {
	val file: File = new File("log - "+ prefix+ " - " + (new Date()).getTime());
	val writer: FileWriter = new FileWriter(file);
	
	def receive = {
		case ProfilingMessage(ts, id, time) =>
			writer.write(ts + " " + id + " " + time + "\n");
			writer.flush();
	}
	
	override def postStop() {
		writer.close();
	}
}