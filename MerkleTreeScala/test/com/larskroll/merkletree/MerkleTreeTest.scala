package com.larskroll.merkletree

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import scala.collection.immutable.TreeSet

class MerkleTreeTest extends FlatSpec with ShouldMatchers {
	"A merkle tree" should "be identical independend of construction order" in {
		val tree1 = new MerkleTree;
		val tree2 = new MerkleTree;
		tree1 += 1;
		tree1 += 2;
		tree1 += 3;
		tree2 += 3;
		tree2 += 2;
		tree2 += 1;
		tree1.toString should equal (tree2.toString);		
	}
	it should "be able to to correctly find differences" in {
		val tree1 = new MerkleTree;
		val tree2 = new MerkleTree;
		tree1 += 2;
		tree1 += 3;
		tree2 += 2;
		tree2 += 1;
		tree1.compareWith(tree2) should equal (Pair(TreeSet(1), TreeSet(3)));
	}
}