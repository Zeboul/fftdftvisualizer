import java.util.Queue;

public abstract class Transformer implements Runnable
{
	protected Queue<double[]> queue;
	protected int[] datas;
	protected int numBins;
	protected int samplingFreq;
	protected final int REFRESH_RATE = 60;
	
	public Transformer(Queue<double[]> q, Song song, int numBins) throws Exception
	{
		this.queue = q;
		this.datas = song.getData();
		this.numBins = numBins;
		this.samplingFreq = song.getSamplingFreq();
	}
	
	public abstract double[] transform(int startPos); //to be implemeted by children
}
