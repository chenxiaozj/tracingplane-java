package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class ContextLayer implements TransitLayer {
	
	static final Logger log = LoggerFactory.getLogger(ContextLayer.class);
	
	final ContextLayerConfig config;
	final ContextLayerListener listener;
	
	public ContextLayer() {
		this(new ContextLayerConfig());
	}
	
	public ContextLayer(ContextLayerConfig config) {
		this.config = config;
		this.listener = config.tryCreateListeners();
	}

	@Override
	public Baggage newInstance() {
		return null; // Our context layer treats null baggage as empty baggage
	}

	@Override
	public void discard(Baggage baggage) {
		if (baggage instanceof BaggageImpl) {
			((BaggageImpl) baggage).discard();
		} else if (baggage != null) {
			log.warn("discard unknown Baggage implementation class {}", baggage.getClass().getName());
		}
	}

	@Override
	public Baggage branch(Baggage from) {
		if (from == null || from instanceof BaggageImpl) {
			return doBranch((BaggageImpl) from);
		} else {
			log.warn("branch unknown Baggage implementation class {}", from.getClass().getName());
			return null;
		}
	}
	
	private Baggage doBranch(BaggageImpl from) {
		listener.preBranch(from);
		BaggageImpl branched = from == null ? null : from.branch();
		listener.postBranch(from, branched);
		return branched;
	}

	@Override
	public Baggage join(Baggage left, Baggage right) {
		boolean validLeft = left == null || left instanceof BaggageImpl;
		boolean validRight = right == null || right instanceof BaggageImpl;
		if (validLeft && validRight) {
			return doJoin((BaggageImpl) left, (BaggageImpl) right);
		} else {
			if (!validLeft && !validRight) {
				log.warn("merge unknown Baggage implementation class left={} right={}", left.getClass().getName(), right.getClass().getName());
			} else if (!validLeft) {
				log.warn("merge unknown Baggage implementation class left={}", left.getClass().getName());
			} else if (!validRight) {
				log.warn("merge unknown Baggage implementation class right={}", right.getClass().getName());
			}
			return null;
		}
	}
	
	private Baggage doJoin(BaggageImpl left, BaggageImpl right) {
		listener.preJoin(left, right);
		BaggageImpl joined = left == null ? right : left.mergeWith(right);
		listener.postJoin(joined);
		return joined;
	}

	@Override
	public Baggage deserialize(byte[] serialized, int offset, int length) {
		BaggageImpl deserialized = BaggageImplSerialization.deserialize(serialized, offset, length);
		listener.postDeserialize(deserialized);
		return deserialized;
	}

	@Override
	public Baggage readFrom(InputStream in) throws IOException {
		BaggageImpl deserialized = BaggageImplSerialization.readFrom(in);
		listener.postDeserialize(deserialized);
		return deserialized;
	}

	@Override
	public byte[] serialize(Baggage instance) {
		if (instance == null || instance instanceof BaggageImpl) {
			return doSerialize((BaggageImpl) instance);
		} else {
			log.warn("serialize unknown Baggage implementation class {}", instance.getClass().getName());
			return null;
		}
	}
	
	private byte[] doSerialize(BaggageImpl baggage) {
		listener.preSerialize(baggage);
		return baggage == null ? BaggageImpl.EMPTY_BYTES : BaggageImplSerialization.serialize(baggage);
	}

	@Override
	public void writeTo(OutputStream out, Baggage instance) throws IOException {
		if (instance == null || instance instanceof BaggageImpl) {
			doWriteTo(out, (BaggageImpl) instance);
		} else {
			log.warn("writeto unknown Baggage implementation class {}", instance.getClass().getName());
		}
	}
	
	private void doWriteTo(OutputStream out, BaggageImpl baggage) throws IOException {
		listener.preSerialize(baggage);
		if (baggage != null) {
			BaggageImplSerialization.write(out, baggage);
		}
	}

}
