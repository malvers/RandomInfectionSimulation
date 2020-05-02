import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SimpleEmailer {

    private DataOutputStream os = null;
    private BufferedReader is = null;
    private String sRt = "";

    public static void main(String[] args) {

        SimpleEmailer emailer = new SimpleEmailer();
        String message = "Ready at: " + Util.getTimeStringNow(System.currentTimeMillis());

        try {
            emailer.sendEmail("cloud.transinsight.com",
                    "malvers@transinsight.com", "Dr. Michael R. Alvers",
                    "malvers@transinsight.com", "Dr. Michael R. Alvers",
                    "Message from SimulatorCore ...", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public synchronized final String sendEmail(String sSmtpServer,
                                               String sFromAdr, String sFromRealName,
                                               String sToAdr, String sToRealName,
                                               String sSubject, String sText)
            throws IOException, Exception {
        Socket so = null;
        try {
            sRt = "";
            if (null == sSmtpServer || 0 >= sSmtpServer.length() || null == sFromAdr || 0 >= sFromAdr.length() ||
                    null == sToAdr || 0 >= sToAdr.length() ||
                    ((null == sSubject || 0 >= sSubject.length())
                            && (null == sText || 0 >= sText.length()))) {
                throw new Exception("Invalid Parameters for SmtpSimple.sendEmail().");
            }
            if (null == sFromRealName || 0 >= sFromRealName.length()) {
                sFromRealName = sFromAdr;
            }
            if (null == sToRealName || 0 >= sToRealName.length()) {
                sToRealName = sToAdr;
            }
            so = new Socket(sSmtpServer, 25);
            os = new DataOutputStream(so.getOutputStream());
            is = new BufferedReader( new InputStreamReader(so.getInputStream()));
            so.setSoTimeout(10000);
            writeRead(true, "220", null);
            writeRead(true, "250", "HELO " + sSmtpServer + "\n");
            writeRead(true, "250", "RSET\n");
            writeRead(true, "250", "MAIL FROM:<" + sFromAdr + ">\n");
            writeRead(true, "250", "RCPT TO:<" + sToAdr + ">\n");
            writeRead(true, "354", "DATA\n");
            writeRead(false, null, "To: " + sToRealName + " <" + sToAdr + ">\n");
            writeRead(false, null, "From: " + sFromRealName + " <" + sFromAdr + ">\n");
            writeRead(false, null, "Subject: " + sSubject + "\n");
            writeRead(false, null, "Mime-Version: 1.0\n");
            writeRead(false, null, "Content-Type: text/plain; charset=\"iso-8859-1\"\n");
            writeRead(false, null, "Content-Transfer-Encoding: quoted-printable\n\n");
            writeRead(false, null, sText + "\n");
            writeRead(true, "250", ".\n");
            writeRead(true, "221", "QUIT\n");
            return sRt;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception ex) {
                }
            }
            if (so != null) {
                try {
                    so.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private final void writeRead(boolean bReadAnswer,
                                 String sAnswerMustStartWith,
                                 String sWrite)
            throws IOException, Exception {
        if (null != sWrite && 0 < sWrite.length()) {
            sRt += sWrite;
            os.writeBytes(sWrite);
        }
        if (bReadAnswer) {
            String sRd = is.readLine() + "\n";
            sRt += sRd;
            if (null != sAnswerMustStartWith
                    && 0 < sAnswerMustStartWith.length()
                    && !sRd.startsWith(sAnswerMustStartWith)) {
                throw new Exception(sRt);
            }
        }
    }
}