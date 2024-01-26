package engine.io.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import engine.dev.Log;

public class IOUtil {
	
	private static final String WORKING_DIRECTORY = "C:/Users/Jay/eclipse-workspace/MoonP3/resources/";

	public static String loadAsString(String path) {
		StringBuilder result = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(WORKING_DIRECTORY + path)))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch (IOException e) {
			Log.warn("Couldn't find the file at " + path);
		} catch (NullPointerException e) {

			System.err.println("Null pointer exception " + path);
			System.err.println("Path: " + path);
			System.err.println("classloader: "+IOUtil.class.getClassLoader());
			System.err.println("resource: "+IOUtil.class.getClassLoader().getResourceAsStream(path));
		}

		return result.toString();
	}

	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param resource   the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException if an IO error occurs
	 */
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) {
		try {
			return ioResourceToByteBufferUnsafe(resource, bufferSize);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static ByteBuffer ioResourceToByteBufferUnsafe(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		Path path = Paths.get(resource);
		if (path != null && Files.isReadable(path)) {
			try (SeekableByteChannel fc = Files.newByteChannel(path)) {
				buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
				while (fc.read(buffer) != -1) {}
			}
		} else {
			InputStream source = null;
			
			try {
				source = new FileInputStream(WORKING_DIRECTORY + path);
			} catch (FileNotFoundException e) {
				System.err.println("File not found: " + resource);
			}
			
			ReadableByteChannel rbc = Channels.newChannel(source);
			buffer = BufferUtils.createByteBuffer(bufferSize);

			while(true) {
				int bytes = rbc.read(buffer);
				if (bytes == -1)
					break;
				
				if (buffer.remaining() == 0)
					buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
			}
		}

		buffer.flip();
		return MemoryUtil.memSlice(buffer);
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
	
	/** A wrapper for GSON's fromJson() method, which deserializes the specified JSON into an object of the specified class.
	 * @param <T>  the type of the desired object
	 * @param json the string from which the object is to be deserializedclassOf
	 * @param classOfT the class of T
	 * @return an object of type T from the string. Returns null if json is nullor if json is empty. 
	 */
	public static <T> T deserializeJson(String json, Class<T> classOfT) {
		Gson gson = new Gson();

		// De-serialize to an object
		T t = gson.fromJson(loadAsString(json), classOfT);
		return t;
	}
	
	/** A wrapper for GSON's getAsJsonObject() method, which reads a JSON file and returns it as a JsonObject
	 * @param json the string from which the object is to be deserializedclassOf
	 * @return The json file as a JsonObject
	 */
	public static JsonObject readJson(String json) {
		JsonObject jsonObject = JsonParser.parseString(loadAsString(json)).getAsJsonObject();

		return jsonObject;
	}
}
