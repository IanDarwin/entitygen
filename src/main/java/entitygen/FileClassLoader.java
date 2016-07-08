package entitygen;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class FileClassLoader extends ClassLoader {

	public FileClassLoader() {
		// Empty
	}

	public Class<?> nameToClass(String className, String fileName) {
		try (InputStream is = new FileInputStream(fileName);) {
			int n = is.available();
			byte[] bytes = new byte[n];
			int ret = is.read(bytes, 0, n);
			if (ret != n) {
				throw new IOException("Expected " + n + " bytes but read " + ret);
			}
			ByteBuffer data = ByteBuffer.wrap(bytes);
			return 	defineClass(className, data, null);
		} catch (IOException e) {
			throw new RuntimeException("Couldnt open " + className + "(" + e + ")", e);
		}
		
	}
}
