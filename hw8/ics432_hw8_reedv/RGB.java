
public class RGB {

	/**
	 * Takes an array of three RGB bytes (that is individual 0-255 values for R, G, and B) 
	 * and returns the RGB int.
	 * @param bytes
	 * @return
	 */
	public static int bytesToInt(byte bytes[]) {
		int value = 0;
		for (int i = 0; i < bytes.length; i++) {
			int shift = (bytes.length - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * Takes an RGB int and returns the array of 3 RGB bytes [R,G,B]. 
	 * @param rgb
	 * @return
	 */
	public static byte[] intToBytes(int rgb) {
		byte[] bytes = new byte[3];
		for (int i = 0; i < 3; i++) {
			int offset = (2 - i) * 8;
			bytes[i] = (byte) ((rgb >>> offset) & 0xFF);
		}
		return bytes;
	}
}
