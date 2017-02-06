import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class WavFilePlayer {

    public static void main(String args[]) {
        new WavFilePlayer().play("/Users/casanova/Desktop/foo.wav");
    }

    public void play(String filepath) {

        try {
            File soundFile = new File(filepath);
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