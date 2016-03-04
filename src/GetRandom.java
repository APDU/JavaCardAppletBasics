/**
 * 
 */
package getRandom;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.security.RandomData;

/**
 * @author APDU
 *
 */
public class GetRandom extends Applet {
	private RandomData randomData;
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new GetRandom(bArray, bOffset, bLength);
	}
	public GetRandom(byte[] bArray, short bOffset, byte bLength){
		randomData = RandomData.getInstance(randomData.ALG_PSEUDO_RANDOM);
		register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) 0x84:
			randomData.generateData(buf, (short)0, (short)8);
			apdu.setOutgoingAndSend((short)0, (short)8);
			break;
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}