import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class Main
{
	public static void usage()
	{
		System.out.println("Usage:");
		System.out.println("\tjava Main <wavefile> <method> <parallel>\n");
		System.out.println("<method>:\tcurrently only 0 for DFT");
		System.out.println("<parallel>:\t0 or 1 (Sequential or Parallel)");
		System.out.println("<wavefile>:\t.wav filename for input");
	}

	public static void main(String[] args)
	{
		final int NBINS; //number of bins for the fourier transform
		final int FRAME_SZ; //number of samples in one rendered frame of data

		Transformer transformer;
		int method;
		boolean parallel = false;
		Song song; //Song, a parser/container for the raw data
		Visualizer visualizer; //drawing thread
		ConcurrentLinkedQueue<double[]> q;//transformed data container, each one is a frame to be rendered
		Thread visualizerThread;
		Thread transformerThread;
		Clip clip;
		long startTime, endTime; //performance measurment

		if (args.length < 3 || args.length > 3)
		{
			usage();
			return;
		}

		try {
			// Parse arguments
			song = new Song(new File(args[0]));
			method = Integer.parseInt(args[1]);
			if (Integer.parseInt(args[2]) != 0)
				parallel = true;

			FRAME_SZ = song.getSamplingFreq() * 1 / 60; //sampling freq/refresh rate of a monitor is frame size
			NBINS = 512; //number of bins is best kept a power of 2

			System.out.format("Frame Size: %d%n", FRAME_SZ);
			System.out.format("Bin Size: %d%n", NBINS);

			q = new ConcurrentLinkedQueue<double[]>();

			if (method == 0)
			{
				//construct the transformer as either p or s
				if (parallel)
					transformer = new ParallelDiscreteFT(q, song, NBINS, 0, FRAME_SZ); 
				else
					transformer = new SequentialDiscreteFT(q, song, NBINS, 0, FRAME_SZ);
			}
			else
			{
				System.out.println("Bad method, quitting");
				return;
			}
			
			// Play the music file.
			clip = getMusicClip(args[0]);
			clip.start(); //play the music, we don't synchronize the music with the visualizer, so some lag occurs
			/*NOTE ON HOW TO DO THIS
			 * clip contains a useful method called getFramePosition()
			 * This method can be used to keep track of where this thread (that plays the song) is
			 * 
			 * all we would have to do in order to make sure the visualizer stayed synchronized with the song is atomic
			 * await the visualizer with the frame (or a couple before due to lag) it represents
			 * 
			 * eg if frame 256 then start rendering it at 256*framesize - 5 and by the time you reach 256*framesize it
			 * will be fully drawn
			 * 
			 */

			// Initialize and start the visualizer
			System.out.println("Initializing Visualizer...");
			visualizer = new Visualizer(q, NBINS);
			visualizerThread = new Thread(visualizer);
			visualizerThread.start();

			// Transformer
			System.out.println("Starting Transform...");
			transformerThread = new Thread(transformer);
			startTime = System.currentTimeMillis();
			transformerThread.start();
		}
		catch (Exception e)
		{
			// TODO: do real exception handling....
			System.out.format("Caught exception: %s%nQuitting%n", e.getMessage());
			return;
		}

		try
		{
			transformerThread.join();
			endTime = System.currentTimeMillis();
			System.out.format("Joined Transformer: Execution time: %d%n", endTime - startTime); //print time to execute
			visualizerThread.interrupt();
			System.out.println("Interrupting visualizer..");
			visualizerThread.join();
			System.out.println("Joined Visualizer");
			clip.close();
			System.out.println("Closed music clip");
		}
		catch (InterruptedException e)
		{
			System.out.println("Exception during join, quitting");
			return;
		}
	}

	// Get the clip object to play the music.
	public static Clip getMusicClip(String fileName)
	{
		try 
		{
		    AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fileName)); 
		    AudioFormat format = stream.getFormat();
		    DataLine.Info info = new DataLine.Info(Clip.class, format);
		    Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(stream);
		    return clip;
		}
		catch (Exception e) 
		{
			System.out.println("Failed to play music.");
		}
		return null;
	}
}

