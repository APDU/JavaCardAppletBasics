/**
 * 
 */
package im.map;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.DESKey;
import javacard.security.KeyBuilder;
import javacard.security.Signature;
import javacardx.crypto.Cipher;

/**
 * @author APDU
 *
 */
public class ALG extends Applet {
	
	private Signature signatureObj;
	private DESKey deskeyObj;
	private Cipher cipherObj;
	private byte[] init = new byte[]
			{(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};
	private byte[] key = new byte[]
			{(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,
			(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2,(byte)2};
	private byte[] dtrByte; 
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new im.map.ALG().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public ALG(){
		deskeyObj = (DESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_DES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_DES3_2KEY, false);
		signatureObj = Signature.getInstance(Signature.ALG_DES_MAC8_ISO9797_1_M2_ALG3, false);
		cipherObj = Cipher.getInstance(Cipher.ALG_DES_CBC_NOPAD, false); 
		dtrByte = JCSystem.makeTransientByteArray((short)12, JCSystem.CLEAR_ON_DESELECT);
	}
	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) 0x00:
			deskeyObj.setKey(key, (short)0);
			signatureObj.init(deskeyObj, Signature.MODE_SIGN, init, (short)0, (short)8);
			byte[] data = new byte[]
					{(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1};	
			signatureObj.sign(data, (short)0, (short)8, dtrByte, (short)0);	
			Util.arrayCopyNonAtomic(dtrByte, (short)0, buf, (short)0, (short)8);	
			apdu.setOutgoingAndSend((short)0, (short)8);
			break;
			
		case (byte) 0x01:
			deskeyObj.setKey(key, (short)0);
			cipherObj.init(deskeyObj, Cipher.ALG_DES_CBC_NOPAD, init, (short)0, (short)8);
			byte[] data1 = new byte[]
					{(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1,(byte)1};	
			cipherObj.doFinal(data1, (short)0, (short)8, dtrByte, (short)0);		
			Util.arrayCopyNonAtomic(dtrByte, (short)0, buf, (short)0, (short)8);	
			apdu.setOutgoingAndSend((short)0, (short)8);
			break;
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}