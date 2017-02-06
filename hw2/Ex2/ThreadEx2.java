import java.io.File;


import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class ThreadEx2 implements Runnable
{
    File soundFile;
    
    public ThreadEx2(File soundFile)
    {
        this.soundFile = soundFile;
    }
    
    public void run()
    {
        // validate and play the selected sound file
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat audioFormat = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();
            int nBytesRead = 0;
            byte[] buffer = new byte[20000];
            while (nBytesRead != -1) {
                nBytesRead = audioStream.read(buffer, 0, buffer.length);
                if (nBytesRead >= 0) {
                    sourceLine.write(buffer, 0, nBytesRead);
                }
            }
            sourceLine.drain();
            sourceLine.close();
        } catch (Exception e) {
            System.out.println("Can't read audio file!");
            return;
        }
        
    }
}
