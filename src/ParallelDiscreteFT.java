import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class ParallelDiscreteFT extends SequentialDiscreteFT
{
	public ParallelDiscreteFT(Queue<double[]> queue, Song song, int numBins, int startPos,
			int incSize) throws Exception
	{
		super(queue, song, numBins, startPos, incSize);
	}
	
	@Override
	public void run()
	{
		int NUM_PROCS = Runtime.getRuntime().availableProcessors();
		Thread[] thread = new Thread[NUM_PROCS];
		SequentialDiscreteFT[] sequentialDiscreteFT = new SequentialDiscreteFT[NUM_PROCS];
		List<Queue<double[]>> threadQueues = new LinkedList<Queue<double[]>>();
		double[] data = null;
		
		try
		{
			for (int i = 0, s = startPos; i < NUM_PROCS; i++, s += incSize)
			{
				threadQueues.add(new LinkedList<double[]>());
				sequentialDiscreteFT[i] = new SequentialDiscreteFT(threadQueues.get(i), song, numBins, s,
						incSize * NUM_PROCS);
				thread[i] = new Thread(sequentialDiscreteFT[i]);
				thread[i].start();
			}

			for (int i = 0; i + numBins < datas.length; i += incSize)
			{
				data = null;

				do
				{
					data = threadQueues.get((i / incSize) % NUM_PROCS).poll();
				} while (data == null);

				while (!queue.offer(data));
			}

			for (int i = 0; i < NUM_PROCS; i++)
				thread[i].join();
		}
		catch (Exception e)
		{
			System.out.println("ParalleleDiscreteFT caught exception, exiting");
		}
	}
}
