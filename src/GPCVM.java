/**
 * 
 */
package im.map;

import org.globalplatform.CVM;
import org.globalplatform.GPSystem;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * @author APDU
 *
 */
public class GPCVM extends Applet {
	CVM gpCVM = GPSystem.getCVM( GPSystem.CVM_GLOBAL_PIN );
	private byte[] newValue = new byte[]
			{(byte)0x66, (byte)0x01, (byte)0x77, (byte)0x02 };
	private byte[] oldValue = new byte[]
			{(byte)0x12, (byte)0x34, (byte)0x56 };
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new im.map.GPCVM().register(bArray, (short) (bOffset + 1),
				bArray[bOffset]);
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return; 
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		
		case (byte) 0xE0:
			if ( !gpCVM.setTryLimit( (byte)3 ) ) 
				ISOException.throwIt( (short)0x6666 );	
			if( !gpCVM.update( oldValue, (short)0, (byte)3, CVM.FORMAT_BCD ) )
				ISOException.throwIt( (short)0x6666 );
			break;
		
		case (byte) 0xDC:
			if ( buf[ISO7816.OFFSET_P1] == (byte)0 ){
				if( !gpCVM.update( newValue, (short)0, (byte)4, CVM.FORMAT_HEX ) )
					ISOException.throwIt( (short)0x6666 );
			}else if ( buf[ISO7816.OFFSET_P1] == (byte)1 ) {
				if ( !gpCVM.setTryLimit( (byte)4 ) ) 
					ISOException.throwIt( (short)0x6666 );				
			}else if ( buf[ISO7816.OFFSET_P1] == (byte)2 ) {
				if ( !gpCVM.resetState() ) 
					ISOException.throwIt( (short)0x6666 );				
			}else if ( buf[ISO7816.OFFSET_P1] == (byte)3 ) {
				if ( !gpCVM.resetAndUnblockState() ) //BLOCKED to ACTIVE.
					ISOException.throwIt( (short)0x6666 );				
			}else 	
				ISOException.throwIt( (short)0x6A86 );		
			break;
			
		case (byte) 0xDA:
			if ( !gpCVM.blockState() ) 
				ISOException.throwIt( (short)0x6666 );	
			break;
			
		case (byte) 0x20:
			if (buf[ISO7816.OFFSET_P1] == (byte)0){
				if( gpCVM.verify(oldValue, (short)0,(byte)3, CVM.FORMAT_BCD ) == CVM.CVM_FAILURE )
					ISOException.throwIt((short)0x6666);
			}else {
				if( gpCVM.verify(newValue, (short)0,(byte)4, CVM.FORMAT_HEX ) == CVM.CVM_FAILURE )
					ISOException.throwIt((short)0x6666);
			}
			break;
			
		case (byte) 0xCA:
			if (buf[ISO7816.OFFSET_P1] == (byte)1){	//DC 02
				if ( !gpCVM.isVerified() ) 	
					ISOException.throwIt( (short)0x6666 );	
				break;
			}
			if (buf[ISO7816.OFFSET_P1] == (byte)2){  //DC 03
				if ( !gpCVM.isBlocked() ) 
					ISOException.throwIt( (short)0x6666 );	
				break;
			}
			buf[0] = gpCVM.getTriesRemaining();
			apdu.setOutgoingAndSend((short)0, (short)1);	
			break;
			
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}