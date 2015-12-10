package smp;

import otr.crypt.Provider;
import otr.jca.JCAProvider;
import otr.other.TLV;

public class Connection {
	
	private SMState smstate = new SMState();
	Provider prov = new JCAProvider();
	

		void initRespondSmp(String question, String secret, boolean initiating){
    	  
//    	    byte[] our_fp = PrivKey.fingerprintRaw(us, this.accountName, this.protocol, prov);
//
//    	    int combined_buf_len = 41 + this.sessionid_len + secret.length();
//    	    byte[] combined_buf = new byte[combined_buf_len];
//    	    combined_buf[0]=1;
//    	    if(initiating){
//    	    	System.arraycopy(our_fp, 0, combined_buf, 1, 20);
//    	    	System.arraycopy(this.activeFingerprint.fingerPrint, 0, combined_buf, 21, 20);
//    	    }else{
//    	    	System.arraycopy(this.activeFingerprint.fingerPrint, 0, combined_buf, 1, 20);
//    	    	System.arraycopy(our_fp, 0, combined_buf, 21, 20);
//    	    }
//    	    System.arraycopy(this.sessionId, 0, combined_buf, 41, this.sessionid_len);
//    	    System.arraycopy(secret.getBytes(), 0, 
//    	    		combined_buf, 41+this.sessionid_len, secret.length());
//    	    
			byte[] secretByte = secret.getBytes();
    	    byte[] smpmsg;
    	    if(initiating){
    	    	smpmsg = SMP.step1(smstate, secretByte, prov);
    	    }else{
    	    	smpmsg = SMP.step2b(smstate, secretByte, prov);
    	    }
    	    
    	    // If we've got a question, attach it to the smpmsg 
    	    if(question != null){
    	    	byte[] qsmpmsg = new byte[question.length() + 1 + smpmsg.length];
    	    	System.arraycopy(question.getBytes(), 0, qsmpmsg, 0, question.length());
    	    	System.arraycopy(smpmsg, 0, qsmpmsg, question.length()+1, smpmsg.length);
    	    	smpmsg = qsmpmsg;
    	    }
    	    
    	    //Send msg with next smp msg content 
    	    TLV sendtlv = new TLV(initiating? 
    	    		(question!=null? TLV.SMP1Q:TLV.SMP1):TLV.SMP2, smpmsg);
    	    TLV[] tlvs = new TLV[1];
    	    tlvs[0] = sendtlv;
    	    byte[] emptymsg = new byte[0];
    	    DataMessage dm = Proto.createData(this, emptymsg, Proto.MSGFLAGS_IGNORE_UNREADABLE, tlvs);
    	    
    	    fragmentAndSend(new String(dm.getContent()), Policy.FRAGMENT_SEND_ALL, callback);
    	    this.smstate.nextExpected = initiating? SMP.EXPECT2 : SMP.EXPECT3;

	}

}
