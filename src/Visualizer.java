import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Queue;

@SuppressWarnings("serial")
public class Visualizer extends Frame implements Runnable {
	
	// Graphics constants.
	public final short WINDOW_WIDTH;
	public final short WINDOW_HEIGHT = 400;
	public final short DISPLAY_START_X = 12;
	public final short DISPLAY_START_Y = WINDOW_HEIGHT - 10;
	
	public final int BAR_WIDTH = 5;
	public final int MAX_BAR_HEIGHT = 300;
	
	// Time constants.
	public final short ONE_SIXTIETH_SECOND = 17;
	public final short TIME_OUT = 200;
	
	// States and attributes.
	private final int N;
	private Queue<double[]> amplitudes = null;
	double[] presentamplitudes = null;
	
	
	// Component used to paint the amplitude bars.
	private Component painter = new Component() 
	{
		// Override the paint method to draw the amplitude bars.
		@Override
		public void paint(Graphics graphics)
		{	
			// Paint the background onto the buffered image.
			Image image = createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
			Graphics imagegraphics = image.getGraphics();
			imagegraphics.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
			
			// Paint the amplitude bars onto the buffered image.
			if(presentamplitudes != null)
			{
				imagegraphics.setColor(Color.ORANGE);
				
				double max = 0;
				
				for(int i = 1; i < presentamplitudes.length/2; i++)
				{
					if(presentamplitudes[i] > max) max = presentamplitudes[i];
				}
				
				double scale = (MAX_BAR_HEIGHT/max);
				
				for(int i=1; i<presentamplitudes.length/2; i++)
				{
					imagegraphics.fillRect(i*BAR_WIDTH + DISPLAY_START_X, DISPLAY_START_Y - (int)(presentamplitudes[i]*scale), BAR_WIDTH, (int)(presentamplitudes[i]*scale));
				}	
			}
			
			// Paint the buffered image onto the window.
			graphics.drawImage(image, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);	
		}
	};
	
	
	// Override the default update method to stop flickering.
	@Override
	public void update(Graphics graphics)
	{
		painter.paint(graphics);
	}
	
	
	// Constructor.
	public Visualizer(Queue<double[]> amplitudes ,int N) //N is number of bins
	{
		super("Visualizer");
		
		this.amplitudes = amplitudes;
		this.N = N;
		
		WINDOW_WIDTH = (short) (2*DISPLAY_START_X + BAR_WIDTH*this.N/2); //only display half (data is mirrored in Fourier transforms)
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setVisible(true);
		setBackground(Color.BLACK);
		painter.setVisible(true);
		add(painter);
	}
	
	
	// Visualizer Main.
	@Override
	public void run()
	{
		while(true)
		{
			long starttime = System.currentTimeMillis();
			while(amplitudes.isEmpty())
			{
				if(System.currentTimeMillis() - starttime > TIME_OUT)
				{
					this.dispose();
					return;
				}
			}
			presentamplitudes = amplitudes.poll();
			
			// Repaint the amplitude bars.
			repaint();
			
			// Sleep.
			try 
			{
				Thread.sleep(ONE_SIXTIETH_SECOND);
			} 
			catch (InterruptedException e) 
			{
			}
		}	
	}
}