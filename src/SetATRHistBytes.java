/**
 * 
 */
package im.map;

import org.globalplatform.GPSystem;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;

/**
 * @author APDU
 *
 */
public class SetATRHistBytes extends Applet {
	private byte oldHist[] = new byte[] 
			{ (byte)0xF1, (byte)0x25, (byte)0xF2, (byte)0x84, (byte)0xF3, (byte)0x82, (byte)0xF4, (byte)0x89, (byte)0x90 };
	private byte newHist[] = new byte[] 
			{ (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xAA, };
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new im.map.SetATRHistBytes().register(bArray,
				(short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		
		case (byte) 0xE1:
			if ( !GPSystem.setATRHistBytes(oldHist, (short)0, (byte)oldHist.length) ) 
				ISOException.throwIt( (short)0x6666 );
			break;
			
		case (byte) 0xE2:
			if( !GPSystem.setATRHistBytes(newHist, (short)0, (byte)newHist.length) )
				ISOException.throwIt( (short)0x6666 );
			break;
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}