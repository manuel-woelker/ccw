package ccw.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StreamProcessor {

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private final ByteArrayInputStream inputStream;
	private final ByteArrayOutputStream outputStream;
	private final IDispatcher dispatcher;

	public StreamProcessor(ByteArrayInputStream inputStream,
			ByteArrayOutputStream outputStream, IDispatcher dispatcher) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.dispatcher = dispatcher;
	}

	public void run() {
		try {
			objectInputStream = new ObjectInputStream(inputStream);
			objectOutputStream = new ObjectOutputStream(outputStream);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create object streams", e);
		}

		Object object = null;
		Object result = null;
		try {
			object = objectInputStream.readObject();
			result = dispatcher.dispatch(object);
		} catch (EOFException e) {
			// end
			return;
		} catch (Exception e) {
			try {
				objectOutputStream.writeObject(e);
			} catch (IOException e1) {
				new RuntimeException("Error serializing exception", e1);
			}
		}
		try {
			objectOutputStream.writeObject(result);
		} catch (IOException e) {
			// ignore
		}
	}

}
