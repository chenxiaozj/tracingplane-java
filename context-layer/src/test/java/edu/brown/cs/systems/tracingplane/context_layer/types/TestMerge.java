package edu.brown.cs.systems.tracingplane.context_layer.types;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.context_layer.Utils;
import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;
import junit.framework.TestCase;

public class TestMerge extends TestCase {

	private static ByteBuffer make(String... ss) {
		ByteBuffer buf = ByteBuffer.allocate(ss.length);
		for (String s : ss) {
			buf.put(Utils.makeByte(s));
		}
		buf.rewind();
		return buf;
	}
	
	private static boolean equals(List<ByteBuffer> a, List<ByteBuffer> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			if (Lexicographic.compare(a.get(i), b.get(i)) != 0) {
				return false;
			}
		}
		return true;
	}
	
	@Test
	public void testByteBufferListMergeUnion() {
		List<ByteBuffer> a = Lists.newArrayList(
			make("00000000")
	    );

		List<ByteBuffer> b = Lists.newArrayList(
			make("11111111")
	    );
		
		List<ByteBuffer> expect = Lists.newArrayList(
			make("00000000"), 
			make("11111111")
	    );
		
		assertTrue(equals(Lexicographic.merge(a, b), expect));
	}
	
	@Test
	public void testByteBufferListMergeDuplicates() {
		List<ByteBuffer> a = Lists.newArrayList(
			make("00000000"),
			make("11111111")
	    );

		List<ByteBuffer> b = Lists.newArrayList(
			make("00000000"),
			make("11111111")
	    );
		
		List<ByteBuffer> expect = Lists.newArrayList(
			make("00000000"), 
			make("11111111")
	    );
		
		assertTrue(equals(Lexicographic.merge(a, b), expect));
	}
	
	@Test
	public void testByteBufferListMergeDuplicatesTricky() {
		List<ByteBuffer> a = Lists.newArrayList(
			make("00000000"),
			make("11111111")
	    );

		List<ByteBuffer> b = Lists.newArrayList(
			make("11111111"),
			make("00000000")
	    );
		
		List<ByteBuffer> expect = Lists.newArrayList(
			make("00000000"), 
			make("11111111"),
			make("00000000")
	    );
		
		assertTrue(equals(Lexicographic.merge(a, b), expect));
	}
	
	@Test
	public void testByteBufferListMergeVariableLength() {
		List<ByteBuffer> a = Lists.newArrayList(
			make("00000000", "00000000", "00000000", "00000001")
	    );

		List<ByteBuffer> b = Lists.newArrayList(
			make("00000000", "00000000", "00000000", "00000001", "00000000")
	    );
		
		List<ByteBuffer> expect = Lists.newArrayList(
			make("00000000", "00000000", "00000000", "00000001"),
			make("00000000", "00000000", "00000000", "00000001", "00000000")
	    );
		
		assertTrue(equals(Lexicographic.merge(a, b), expect));
	}

}