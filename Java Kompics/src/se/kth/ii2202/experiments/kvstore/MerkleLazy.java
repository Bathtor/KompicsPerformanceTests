package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import sun.security.util.BigInt;

/**
 * tested
 * 
 * @author carbone
 */
public class MerkleLazy implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2717805891399242778L;

	private final transient MessageDigest sha;

	private BigInteger roothash = BigInteger.ZERO;

	private SortedSet<BigInteger> hashvals = new TreeSet<BigInteger>();

	public MerkleLazy() {
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not load SHA-1 digest!");
		}
	}

	public static class Diff {

		private final Set<BigInteger> excess;
		private final Set<BigInteger> missing;

		protected Diff(MerkleLazy tree1, MerkleLazy tree2) {
			excess = new HashSet<BigInteger>(tree1.hashvals);
			excess.removeAll(tree2.hashvals);
			missing = new HashSet<BigInteger>(tree2.hashvals);
			missing.removeAll(tree1.hashvals);
		}

		public Set<BigInteger> getExcess() {
			return excess;
		}

		public Set<BigInteger> getMissing() {
			return missing;
		}
	}

	public MerkleLazy add(BigInteger value) {
		this.hashvals.add(value);
		recompute();
		return this;
	}

	public MerkleLazy remove(BigInteger value) {
		this.hashvals.remove(value);
		recompute();
		return this;
	}

	public Diff diff(MerkleLazy tree) {
		return new Diff(this, tree);
	}

	private void recompute() {

		if (hashvals.isEmpty()) {
			roothash = new BigInt(0).toBigInteger();
		} else {
			Stack<BigInteger> hashStack = new Stack<BigInteger>();
			hashStack.addAll(hashvals);
			roothash = recomputeStack(hashStack).pop();
		}
	}

	public BigInteger getRoothash() {
		return roothash;
	}

	private Stack<BigInteger> recomputeStack(Stack<BigInteger> inStack) {
		Stack<BigInteger> rtStack = new Stack<BigInteger>();
		while (!inStack.isEmpty()) {
			if (inStack.size() == 1) {
				rtStack.add(inStack.pop());
			} else {
				BigInteger val = inStack.pop();
				BigInteger val2 = inStack.pop();
				byte[] newHash = val.add(val2).toByteArray();
				rtStack.add(new BigInteger(sha.digest(newHash)));
				return recomputeStack(rtStack);

			}
		}
		return rtStack;
	}

	// not tested
	public BigInteger calcHash(Object o) {
		return new BigInteger(sha.digest(new BigInt(o.hashCode()).toByteArray()));
	}

	public static void main(String[] args) {
		// a few 'lazy' checks, no junit
		MerkleLazy merkle1 = new MerkleLazy();
		MerkleLazy merkle2 = new MerkleLazy();

		merkle1.add(new BigInt(1).toBigInteger()).add(new BigInt(3).toBigInteger())
				.add(new BigInt(2).toBigInteger());
		merkle2.add(new BigInt(3).toBigInteger()).add(new BigInt(2).toBigInteger())
				.add(new BigInt(1).toBigInteger());

		System.out.println("roots:: " + merkle1.getRoothash() + ", " + merkle2.getRoothash());
		System.out.println("equal?? " + merkle1.getRoothash().equals(merkle2.getRoothash()));

		merkle1.remove(new BigInt(1).toBigInteger());
		merkle2.remove(new BigInt(1).toBigInteger());

		System.out.println("roots:: " + merkle1.getRoothash() + ", " + merkle2.getRoothash());
		System.out.println("equal?? " + merkle1.getRoothash().equals(merkle2.getRoothash()));

		merkle1.add(new BigInt(4).toBigInteger());
		merkle2.add(new BigInt(5).toBigInteger()).add(new BigInt(6).toBigInteger());

		System.out.println("roots:: " + merkle1.getRoothash() + ", " + merkle2.getRoothash());
		System.out.println("not equal?? " + !merkle1.getRoothash().equals(merkle2.getRoothash()));

		Diff diff = new Diff(merkle1, merkle2);
		System.out.println("Diff 1-2:" + diff.getExcess() + ", " + diff.getMissing());

		Diff diff2 = new Diff(merkle2, merkle1);
		System.out.println("Diff 2-1:" + diff2.getExcess() + ", " + diff2.getMissing());
	}
}
