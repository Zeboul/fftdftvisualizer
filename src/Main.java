import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main
{
	public static void usage()
	{
		System.out.println("Usage:");
		System.out.println("\tMainSequentialDFT <wavefile> <method> <parallel>\n");
		System.out.println("Where method is 0 or 1 (DFT or FFT),");
		System.out.println("parallel is 0 or 1 (Sequential or Parallel),");
		System.out.println("and wavefile is a .wav file for input.");
	}

	public static void main(String[] args)
	{
		final int NBINS;
		final int FRAME_SZ;

		Transformer transformer;
		int method;
		boolean parallel = false;
		Song song;
		Visualizer visualizer;
		ConcurrentLinkedQueue<double[]> q = new ConcurrentLinkedQueue<double[]>();
		Thread visualizerThread;
		Thread transformerThread;

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

			// TODO: put this back after testing
			//FRAME_SZ = song.getSamplingFreq() * 1 / 60;
			FRAME_SZ = 256;
			NBINS = FRAME_SZ;

			System.out.format("Frame Size: %d%n", FRAME_SZ);
			System.out.format("Bin Size: %d%n", NBINS);

			if (method == 0)
			{
				if (parallel)
					transformer = new ParallelDiscreteFT(q, song, NBINS, 0, FRAME_SZ);
				else
					transformer = new SequentialDiscreteFT(q, song, NBINS, 0, FRAME_SZ);
			}
			// FIXME: Commented for testing
			/*else if (method == 1)
			{
				if (parallel)
					transformer = new SequentialFFT(q, song, NBINS, 0, FRAME_SZ);
				else
					transformer = new ParallelFFT(q, song, NBINS, 0, FRAME_SZ);
			}*/
			else
			{
				System.out.println("Bad method, quitting");
				return;
			}


			// Initialize and start the visualizer
			System.out.println("Initializing Visualizer...");
			visualizer = new Visualizer(q, NBINS);
			visualizerThread = new Thread(visualizer);
			visualizerThread.start();

			// Transformerz
			System.out.println("Starting Transform...");
			transformerThread = new Thread(transformer);
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
			System.out.println("Joined Transformer");
			visualizerThread.join();
			System.out.println("Joined Visualizer");
		}
		catch (InterruptedException e)
		{
			System.out.println("Exception during join, quitting");
			return;
		}
	}
}

