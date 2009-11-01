package ccw.connector;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.WriteAbortedException;
import java.util.ArrayList;
import java.util.Arrays;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

public class StreamProcessorTest {

	@Test
	public void testEmptyStream() throws Exception {
		IMocksControl easyMock = EasyMock.createStrictControl();
		IDispatcher mockDispatcher = easyMock.createMock(IDispatcher.class);
		easyMock.replay();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = putOnInputStream();
		StreamProcessor streamProcessor = new StreamProcessor(inputStream,
				byteArrayOutputStream, mockDispatcher);
		streamProcessor.run();
		easyMock.verify();
		ObjectInputStream ois = toInputStream(byteArrayOutputStream);
		assertStreamEmpty(ois);
	}

	@Test
	public void testSimpleStream() throws Exception {
		IMocksControl easyMock = EasyMock.createStrictControl();
		IDispatcher mockDispatcher = easyMock.createMock(IDispatcher.class);
		Object toTransmit = new ArrayList<Object>(Arrays.asList((Object) "foo",
				Integer.valueOf(123)));
		EasyMock.expect(mockDispatcher.dispatch(toTransmit)).andReturn("bar");
		easyMock.replay();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = putOnInputStream(toTransmit);
		StreamProcessor streamProcessor = new StreamProcessor(inputStream,
				byteArrayOutputStream, mockDispatcher);
		streamProcessor.run();
		easyMock.verify();
		ObjectInputStream ois = toInputStream(byteArrayOutputStream);
		assertEquals("bar",ois.readObject());
		assertStreamEmpty(ois);
	}

	public static class Unserializable {
	}

	@Test
	public void testUnserializable() throws Exception {
		IMocksControl easyMock = EasyMock.createStrictControl();
		IDispatcher mockDispatcher = easyMock.createMock(IDispatcher.class);
		Object toTransmit = new ArrayList<Object>(Arrays.asList((Object) "foo",
				Integer.valueOf(123)));
		EasyMock.expect(mockDispatcher.dispatch(toTransmit)).andReturn(
				new Unserializable());
		easyMock.replay();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = putOnInputStream(toTransmit);
		StreamProcessor streamProcessor = new StreamProcessor(inputStream,
				byteArrayOutputStream, mockDispatcher);
		streamProcessor.run();
		easyMock.verify();
		ObjectInputStream ois = toInputStream(byteArrayOutputStream);
		try {
			ois.readObject();
			fail();
		} catch (WriteAbortedException wae) {

		}
		assertStreamEmpty(ois);
	}

	private void assertStreamEmpty(ObjectInputStream ois) {
		try {
			Object object = ois.readObject();
			if (object instanceof Throwable) {
				Throwable throwable = (Throwable) object;
				throwable.printStackTrace();
			}
			fail(object.toString());
		} catch (EOFException eof) {
			// expected
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private ObjectInputStream toInputStream(
			ByteArrayOutputStream byteArrayOutputStream) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				byteArrayOutputStream.toByteArray()));
		return ois;
	}

	private ByteArrayInputStream putOnInputStream(Object... toTransmit)
			throws IOException {
		byte[] byteArray = objectsToBytes(toTransmit);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		return inputStream;
	}

	private byte[] objectsToBytes(Object... toTransmit) throws IOException {
		ByteArrayOutputStream objectStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(objectStream);
		for (Object object : toTransmit) {
			oos.writeObject(object);
		}
		byte[] byteArray = objectStream.toByteArray();
		return byteArray;
	}

}
