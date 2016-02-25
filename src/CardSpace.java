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

/**
 * @author APDU
 * ApplyRAM space
 * ApplyE2P space
 * GetCard RAM Space
 * GetCard E2P Space
 */
public class CardSpace extends Applet {
	private byte[] data;
	private byte[] byteDTR;
	private byte[] byteRTR;
	private short[] shortDTR;
	private short[] shortRTR;
	private boolean[] booDTR;
	private boolean[] booRTR;
	private Object[] objDTR;
	private Object[] objRTR;
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new im.map.CardSpace().register(bArray, (short) (bOffset + 1),
				bArray[bOffset]);
	}

	public void process(APDU apdu) {
		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		
		case (byte) 0xCA:
			short space = (short)0x6666;
			if(buf[ISO7816.OFFSET_P1] == (byte)1)
				space = JCSystem.getAvailableMemory( JCSystem.MEMORY_TYPE_PERSISTENT );
			else if(buf[ISO7816.OFFSET_P1] == (byte)2)
				space = JCSystem.getAvailableMemory( JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT );
			else if(buf[ISO7816.OFFSET_P1] == (byte)3)
				space = JCSystem.getAvailableMemory( JCSystem.MEMORY_TYPE_TRANSIENT_RESET );
			else
				ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
			Util.setShort(buf, (short)(0), space);
			apdu.setOutgoingAndSend((short)0, (short)2);
			break;
			
		case (byte) 0xEE:
			short len = 0x10;
			if(buf[ISO7816.OFFSET_LC]>0){
				if (buf[ISO7816.OFFSET_LC] == 1) 
					len = buf[ISO7816.OFFSET_CDATA];
				else{
					byte[] temp = new byte[2]; 
					Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, temp, (short)0, (short)0);
					len = Util.getShort(temp, (short)0);
				}
					
			}
			if(buf[ISO7816.OFFSET_P1] == (byte)1){
				byteDTR = JCSystem.makeTransientByteArray(len, JCSystem.CLEAR_ON_DESELECT);
				byteRTR = JCSystem.makeTransientByteArray(len, JCSystem.CLEAR_ON_RESET);
			
				shortDTR = JCSystem.makeTransientShortArray(len, JCSystem.CLEAR_ON_DESELECT);
				shortRTR = JCSystem.makeTransientShortArray(len, JCSystem.CLEAR_ON_RESET);
			
				booDTR = JCSystem.makeTransientBooleanArray(len, JCSystem.CLEAR_ON_DESELECT);
				booRTR = JCSystem.makeTransientBooleanArray(len, JCSystem.CLEAR_ON_RESET);
			
				objDTR = JCSystem.makeTransientObjectArray(len, JCSystem.CLEAR_ON_DESELECT);
				objRTR = JCSystem.makeTransientObjectArray(len, JCSystem.CLEAR_ON_RESET);
			}else if(buf[ISO7816.OFFSET_P1] == (byte)2)
				data = new byte[len];
			else
					ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		
			break;
			
		case (byte) 0xE0:
			JCSystem.requestObjectDeletion();
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}