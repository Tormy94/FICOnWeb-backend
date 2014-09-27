package es.ficonlan.web.backend.email;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

/**
 * @author Miguel Ángel Castillo Bellagona
 * @version 2.0
 */
public class Email {
    
    private String usuarioCorreo;
    private String password;
    
    private String rutaArchivo;
    private String nombreArchivo;
    
    private String destinatario;
    private String asunto;
    private String mensaje;
    
    public Email(String usuarioCorreo, String password, String rutaArchivo, String nombreArchivo, String destinatario, String asunto,String mensaje) {
        this.usuarioCorreo = usuarioCorreo;
        this.password = password;
        this.rutaArchivo = rutaArchivo;
        this.nombreArchivo = nombreArchivo;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }
    
    public Email(String usuarioCorre,String password,String destinatario,String mensaje){
        this(usuarioCorre,password,"","",destinatario,"",mensaje);
    }
    
    public Email(String usuarioCorre,String password,String destinatario,String asunto,String mensaje){
        this(usuarioCorre,password,"","",destinatario,asunto,mensaje);
    }    

    public boolean sendMail(){
        try
        {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.port", "587");
            props.setProperty("mail.smtp.user", usuarioCorreo);
            props.setProperty("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(props, null);
            BodyPart texto = new MimeBodyPart();
            texto.setText(mensaje);

            BodyPart adjunto = new MimeBodyPart();
            if (!rutaArchivo.equals("")){
                 adjunto.setDataHandler(
                    new DataHandler(new FileDataSource(rutaArchivo)));
                adjunto.setFileName(nombreArchivo);                
            }

            MimeMultipart multiParte = new MimeMultipart();
            multiParte.addBodyPart(texto);
            if (!rutaArchivo.equals("")){
                multiParte.addBodyPart(adjunto);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuarioCorreo));
            message.addRecipient(
                Message.RecipientType.TO,
                new InternetAddress(destinatario));
                message.setSubject(asunto);
            message.setContent(multiParte);

            Transport t = session.getTransport("smtp");
            t.connect(usuarioCorreo, password);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }        
    }
    
	public String leerTextoArchivo(String nombreArchivo) {
		String texto = "";
		FileReader archivo = null;
		String linea = "";
		try {
			archivo = new FileReader(nombreArchivo);
			BufferedReader lector = new BufferedReader(archivo);
			while ((linea = lector.readLine()) != null) {
				texto += linea + "\n";
				lector.close();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Archivo no encontrado");
		} catch (IOException e) {
			throw new RuntimeException("Ocurrio un error de entrada/salida");
		} finally {
			if (archivo != null) {
				try {
					archivo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return texto;
	}
    
    public static void main(String[] args){
        String clave = "patrocinioficonlan"; 
        Email e = new Email("patrocinio@ficonlan.es",clave,"surah.harus@gmail.com","Adjunto","Test de Email 1");
        if (e.sendMail()){
            JOptionPane.showMessageDialog(null,"El email se mandó correctamente");
        }else{
            JOptionPane.showMessageDialog(null,"El email no se mandó correctamente");
        }
    }

}
