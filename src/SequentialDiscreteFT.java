import java.util.Queue;

// TODO: Can be simplified due to symmetry from inputs having only a real component 

public class SequentialDiscreteFT extends Transformer
{
	protected int startPos; //this should be 0 for sequential always, however parallel utilizes it
	protected Song song;
	protected int incSize; //this variable keeps track of the amout to increment for the next frame THIS thread renders (whether we are discrete or parallel)
	//in a purely sequential transform, this is equivalent to frame size
	//when we are operating in parallel, this is equivalent to lenght of data divided by number of threads
	
	public SequentialDiscreteFT(Queue<double[]> q, Song song, int numBins, int startPos, int incSize) throws Exception
	{
		super(q, song, numBins);
		this.startPos = startPos;
		this.song = song;
		this.incSize = incSize;
	}

	@Override
	public void run()
	{
		for (int i = startPos; i + numBins < datas.length; i += incSize) //iterate by incsize (1 for sequential, # threads for parallel)
		{
			double[] data;
			
			data = transform(i);			
			
			while (!queue.offer(data)); //place in queue
		}
	}

	@Override
	public double[] transform(int startPos)
	{
		//http://www.analog.com/static/imported-files/tech_docs/dsp_book_Ch31.pdf
		//wikipedia has the complex formula that's why*/
		double[] result = new double[numBins];

		for (int i = 0; i < numBins; i++) //there are NBINS number of results
		{
			double real = 0;
			double imag = 0;

			for (int j = 0; j < numBins; j++)
			{
				real += datas[startPos + j] * Math.cos(2 * Math.PI * i * j / numBins);
				imag += -datas[startPos + j] * Math.sin(2 * Math.PI * i * j / numBins); //the meat of the transform
			}
			
			result[i] = Math.pow(Math.pow(real, (double)2) + Math.pow(imag, 2), (double).5);
		}
		
		return result;
	}

}
