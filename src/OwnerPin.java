/**
 * 
 */
package im.map;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

/**
 * @author APDU
 *
 */
public class OwnerPinTest extends Applet {
	OwnerPIN oPin = new OwnerPIN((byte)3, (byte)4);	
	private byte[] newPin = new byte[]
			{(byte)0x66, (byte)0x01, (byte)0x77, (byte)0x02 };
	private byte[] oldPin = new byte[]
			{(byte)0x12, (byte)0x34, (byte)0x56 };
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new im.map.OwnerPinTest().register(bArray, (short) (bOffset + 1),
				bArray[bOffset]);
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}
		
		apdu.setIncomingAndReceive();
		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		
		case (byte) 0xE0:
			oPin.update(oldPin, (short)0, (byte)3);
			break;
			
		case (byte) 0xDC:
			oPin.update(newPin, (short)0, (byte)4);
			break;
			
		case (byte) 0x20:
			if ( !oPin.check(buf, (short)ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC]) ) 
				ISOException.throwIt( (short)0x6666 );			
			break;		
			
		case (byte) 0xCA:
			buf[0] = oPin.getTriesRemaining();
			apdu.setOutgoingAndSend((short)0, (short)1);
			break;
		
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}