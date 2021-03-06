import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Queue;

@SuppressWarnings("serial")
public class Visualizer extends Frame implements Runnable {
	
	// Graphics constants.
	public short WINDOW_WIDTH = 800;
	public final short WINDOW_HEIGHT = 400;
	public final short DISPLAY_START_X = 12;
	public final short DISPLAY_START_Y = WINDOW_HEIGHT - 10;
	
	public short BAR_WIDTH = 5;
	public final int MAX_BAR_HEIGHT = 350;
	
	// Time constants.
	public final short DRAW_DELAY = 23;
	
	// States and attributes.
	private final int N;
	private Queue<double[]> amplitudes = null;
	double[] presentamplitudes = null;
	

	// Component used to paint the amplitude bars.
	private Component painter = new Component() 
	{
		Image image;
		// Override the paint method to draw the amplitude bars.
		@Override
		public void paint(Graphics graphics)
		{	
			// Paint the background onto the buffered image.
			if(image == null)
			{
				image = createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
			}
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
	public Visualizer(Queue<double[]> amplitudes ,int N /* numBins */)
	{
		super("Visualizer");
		
		this.amplitudes = amplitudes;
		this.N = N;
		
		// Adjust bar and window widths.
		if((2*DISPLAY_START_X + this.N/2) >= WINDOW_WIDTH)
		{
			BAR_WIDTH = 1;
			WINDOW_WIDTH = (short) (2*DISPLAY_START_X + BAR_WIDTH*this.N/2);
		}
		else
		{
			BAR_WIDTH = (short) (2*(WINDOW_WIDTH - 2*DISPLAY_START_X)/this.N);
		}
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setVisible(true);
		setBackground(Color.BLACK);
		add(painter);
	}
	
	
	// Visualizer Main.
	@Override
	public void run()
	{
		while(true)
		{
			while(amplitudes.isEmpty())
			{
				if(Thread.interrupted())
				{
					this.dispose();
					System.out.println("Visualizer has been interrupted");
					return;
				}
			}
			presentamplitudes = amplitudes.poll();
			
			// Repaint the amplitude bars.
			repaint();
			
			// Sleep.
			try 
			{
				Thread.sleep(DRAW_DELAY);
			} 
			catch (InterruptedException e) 
			{
				this.dispose();
				System.out.println("Visualizer has been interrupted");
				return;
			}
		}	
	}
}