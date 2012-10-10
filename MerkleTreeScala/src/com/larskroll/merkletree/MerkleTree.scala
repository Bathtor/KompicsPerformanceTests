/**
 *
 */
package com.larskroll.merkletree

import java.security.MessageDigest
import scala.collection.mutable.MutableList
import scala.collection.immutable.SortedSet

/**
 * The <code>MerkleTree</code> .
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 *
 */
class MerkleTree extends Serializable {
	
	@transient
	private val bb = java.nio.ByteBuffer.allocate(4); //Maybe 8? Not sure about 64bit here
	@transient
	private val sha = MessageDigest.getInstance("SHA");
	
	var rootNode: Tree = Empty;
	var hashList = SortedSet.empty[BigInt];
	
	private def shaHash(one: BigInt, two: BigInt): BigInt = {
		val bytes1 = one.toByteArray;
		val bytes2 = two.toByteArray;
		val bytesConcat = bytes1 ++ bytes2;
		val hashBytes = sha.digest(bytesConcat);
		return BigInt(hashBytes);
	}
	
	def shaHash(some: Any): BigInt = {
		val scHash = some.##;
		bb.putInt(scHash);
		bb.flip();
		val bytes = bb.array();
		bb.clear();
		val hashBytes = sha.digest(bytes);
		return BigInt(hashBytes);
	}
	
	def root: BigInt = {
		rootNode match {
			case Empty => 0;
			case x: NonEmpty => x.hash;
		}
	}
	
	def compareWith(other: MerkleTree): Pair[Set[BigInt], Set[BigInt]] = {
		val iHave = hashList &~ other.hashList;
		val otherHas = other.hashList &~ hashList;
		return (otherHas, iHave);
	}
	
	def +=(hash: BigInt) = {
		hashList += hash;
		recalcTree();
	}
	
	private def recalcTree() = {
		var leafList = MutableList.empty[NonEmpty];
		hashList.foreach(h => {leafList += Leaf(h)});
		var nodeList = leafList;
		while (nodeList.size > 2) {
			leafList = MutableList.empty[NonEmpty];
			while (nodeList.size >= 2) {
				val one = nodeList.head;
				nodeList = nodeList.tail;
				val two = nodeList.head;
				nodeList = nodeList.tail;
				leafList += Node(one, two);
			}
			if (nodeList.size == 1) leafList += nodeList.head;
			nodeList = leafList;
		}
		if (nodeList.size == 2) rootNode = Node(nodeList(0), nodeList(1));
		if (nodeList.size == 1) rootNode = nodeList.head;
		if (nodeList.size < 1) rootNode = Empty // If this happens I did something stupid somewhere else^^
	}
	
	override def toString(): String = {
		rootNode.toString;
	}
	
	abstract class Tree extends Serializable {
		def isEmpty: Boolean
	}
	
	abstract class NonEmpty extends Tree with Serializable {
		def isEmpty = false
		def isLeaf: Boolean
		def hash: BigInt
		def left: Tree
		def right: Tree
	}
	
	case object Empty extends Tree {
		def isEmpty = true;
		override def toString: String = {
			return "Ø";
		}
	}
	
	case class Leaf(override val hash: BigInt) extends NonEmpty {
		def isLeaf = true;
		def left = Empty;
		def right = Empty;
		override def toString(): String = {
			return "{" + hash.toString(16) + "}";
		}
	}
	
	case class Node(override val left: NonEmpty, override val right: NonEmpty) extends NonEmpty {
		def isLeaf = false;
		override val hash = shaHash(left.hash, right.hash);
		override def toString(): String = {
			return "{" + left.toString + "," + hash.toString(16) + "," + right.toString + "}";
		}
	}
}

