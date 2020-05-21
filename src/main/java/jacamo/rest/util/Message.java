package jacamo.rest.util;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Adaptor for Jason Message and Rest Message
 *
 */
@XmlRootElement(name = "message")
public class Message {

    private String performative  = null;
    private String sender   = null;
    private String receiver = null;
    private String content = null;
    private String msgId    = null;
    private String inReplyTo = null;

    public Message() {}
    public Message(String id, String p, String s, String r, String c) {
        msgId = id;
        performative = p;
        sender = s;
        receiver = r;
        content = c;
    }
    public Message(String id, String p, String s, String r, String c, String inReplyTo) {
        this(id,p,s,r,c);
        this.inReplyTo = inReplyTo;
    }
    
    public String getPerformative() {
        return performative;
    }
    public void setPerformative(String performative) {
        this.performative = performative;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getMsgId() {
        return msgId;
    }
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    public String getInReplyTo() {
        return inReplyTo;
    }
    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public Message(jason.asSemantics.Message m) {
        this.performative = m.getIlForce();
        this.sender = m.getSender();
        this.receiver = m.getReceiver();
        this.content = m.getPropCont().toString();
        this.msgId = m.getMsgId();
        this.inReplyTo = m.getInReplyTo();
    }

    public jason.asSemantics.Message getAsJasonMsg() {
        jason.asSemantics.Message jm = new jason.asSemantics.Message(performative, sender, receiver, content, msgId);
        jm.setInReplyTo(inReplyTo);
        return jm;
    }

    public String toString() {
        String irt = (inReplyTo == null ? "" : "->"+inReplyTo);
        return "<"+msgId+irt+","+sender+","+performative+","+receiver+","+content+">";
    }
}
