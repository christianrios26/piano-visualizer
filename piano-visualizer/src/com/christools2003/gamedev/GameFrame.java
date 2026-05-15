package com.christools2003.gamedev;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * An abstract class that represents a window to be used for representing graphics and receiving inputs of a 2-D video game.
 * 
 * <p>
 * The class includes methods for drawing basic shapes and text on the window, allowing for the creation of games with procedural graphics. It supports a full-screen mode and 
 * multiple modes for scaling the game's graphics according to the game window size. The class is intended to be an abstraction for the graphics programming required to create 
 * simple games in Java with procedural graphics that scale properly according to changing window sizes. The class leverages methods from the Abstract Window Toolkit (AWT) 
 * library to draw procedural graphics on a {@link java.awt.image.BufferedImage BufferedImage} that is scaled according to the scaling mode. For scaling mode descriptions, see 
 * {@link #setScalable setScalable()}, {@link #useStaticImage setStaticImage()}, {@link #useAutoImage useAutoImage()}, {@link #setModifiedScaling setModifiedScaling()}, 
 * {@link #setIntegerScaling setIntegerScaling()}, and {@link #setScaleToWindow setScaleToWindow()}.
 * </p>
 * 
 * <p>
 * Subclasses must implement the abstract {@link #drawGame drawGame()} method. The {@code GAME_PANEL} field can be used by subclasses to connect a 
 * {@link java.awt.event.KeyListener KeyListener} to the game window. The class is intended for simple game graphics, so it does not directly support drawing game sprites.
 * </p>
 * 
 * @author Christian Rios
 * @version 1.0
 * @since 1.0
 */
public abstract class GameFrame
{
	public final JPanel GAME_PANEL;
	private int windowWidth;
	private int windowHeight;
	private int currentWindowWidth;
	private int currentWindowHeight;
	private int windowWidthOffset;
	private int windowHeightOffset;
	private int imageBaseWidth;
	private int imageBaseHeight;
	private int imageX;
	private int imageY;
	private int imageWidth;
	private int imageHeight;
	private int bufferedImageType;
	private boolean scalable;
	private boolean modifiedScaling;
	private boolean integerScaling;
	private boolean scaleToWindow;
	private boolean usesStaticImage;
	private boolean antiAliasing;
	private boolean resizable;
	private boolean isFullScreen;
	private String title;
	private Image icon;
	private Color backgroundColor;
	private BufferedImage gameScreen;
	private Graphics2D graphics2D;
	private GraphicsDevice graphicsDevice;
	private Dimension screenSize;
	private JFrame currentFrame;
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window with all specified initial values.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 * @param resizable indicates whether the game window can be resized at runtime (does not affect full-screen functionality) [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)
	{
		// Initialize private fields with parameters
		this.title = title;
		this.icon = icon;
		imageBaseWidth = gameWidth;
		imageBaseHeight = gameHeight;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.isFullScreen = isFullScreen;
		this.resizable = resizable;
		
		// Initialize private fields
		bufferedImageType = BufferedImage.TYPE_INT_ARGB;
		scalable = true;
		modifiedScaling = false;
		integerScaling = false;
		scaleToWindow = false;
		usesStaticImage = false;
		antiAliasing = true;
		backgroundColor = Color.BLACK;
		gameScreen = new BufferedImage(gameWidth, gameHeight, bufferedImageType);
		graphics2D = (Graphics2D) gameScreen.getGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// Set up the frame
		currentFrame = new JFrame();
		currentFrame.setTitle(title);
		currentFrame.setUndecorated(isFullScreen);
		currentFrame.setVisible(true);
		Insets insets = currentFrame.getInsets();
		windowWidthOffset = insets.left + insets.right;
		windowHeightOffset = insets.top + insets.bottom;
		if (isFullScreen)
		{
			currentFrame.setSize(windowWidth, windowHeight);
			graphicsDevice.setFullScreenWindow(currentFrame);
		}
		else
		{
			currentFrame.setBounds
			(
				(int) Math.round((screenSize.width - (windowWidth + windowWidthOffset)) / 2.0),
				(int) Math.round((screenSize.height - (windowHeight + windowHeightOffset)) / 2.0),
				windowWidth + windowWidthOffset,
				windowHeight + windowHeightOffset
			);
		}
		GAME_PANEL = new GamePanel();
		currentFrame.add(GAME_PANEL);
		currentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentFrame.setFocusable(true);
		currentFrame.setResizable(resizable);
		if (icon != null)
		{
			currentFrame.setIconImage(icon);
		}
		currentFrame.addWindowFocusListener(new WindowFocusListener()
		{
			@Override
	        public void windowGainedFocus(WindowEvent e) { GAME_PANEL.requestFocusInWindow(); }
	        @Override
	        public void windowLostFocus(WindowEvent e) {}
		});
		GAME_PANEL.requestFocusInWindow();
	}
	
	// Overloaded constructors for handling optional parameters -------------------------------------------------------------------------------------------------------------------
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen)
	{
		this(title, icon, gameWidth, gameHeight, windowWidth, windowHeight, isFullScreen, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight)
	{
		this(title, icon, gameWidth, gameHeight, windowWidth, windowHeight, false, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 * @param resizable indicates whether the game window can be resized at runtime (does not affect full-screen functionality) [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight, boolean isFullScreen, boolean resizable)
	{
		this(title, icon, gameWidth, gameHeight, gameWidth, gameHeight, isFullScreen, resizable);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight, boolean isFullScreen)
	{
		this(title, icon, gameWidth, gameHeight, gameWidth, gameHeight, isFullScreen, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param icon the icon image to be displayed [Optional. Default Value: {@code null}].
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 */
	public GameFrame(String title, Image icon, int gameWidth, int gameHeight)
	{
		this(title, icon, gameWidth, gameHeight, gameWidth, gameHeight, false, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 * @param resizable indicates whether the game window can be resized at runtime (does not affect full-screen functionality) [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)
	{
		this(title, null, gameWidth, gameHeight, windowWidth, windowHeight, isFullScreen, resizable);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen)
	{
		this(title, null, gameWidth, gameHeight, windowWidth, windowHeight, isFullScreen, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param windowWidth the initial width of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameWidth}].
	 * @param windowHeight the initial height of the game window (can only be changed if the window is resizable) [Optional. Default Value: {@code gameHeight}].
	 */
	public GameFrame(String title, int gameWidth, int gameHeight, int windowWidth, int windowHeight)
	{
		this(title, null, gameWidth, gameHeight, windowWidth, windowHeight, false, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 * @param resizable indicates whether the game window can be resized at runtime (does not affect full-screen functionality) [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, int gameWidth, int gameHeight, boolean isFullScreen, boolean resizable)
	{
		this(title, null, gameWidth, gameHeight, gameWidth, gameHeight, isFullScreen, resizable);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param isFullScreen indicates whether the game window begins in full-screen mode [Optional. Default Value: {@code false}].
	 */
	public GameFrame(String title, int gameWidth, int gameHeight, boolean isFullScreen)
	{
		this(title, null, gameWidth, gameHeight, gameWidth, gameHeight, isFullScreen, false);
	}
	
	/**
	 * Constructs a {@code GameFrame} that represents a game window. This simplified constructor omits some parameters from the complete 
	 * {@code GameFrame(String title, Image icon, int gameWidth, int gameHeight, int windowWidth, int windowHeight, boolean isFullScreen, boolean resizable)} constructor.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 * @param gameWidth the defining width in pixels of the game's coordinate system (cannot be changed after construction).
	 * @param gameHeight the defining height in pixels of the game's coordinate system (cannot be changed after construction).
	 */
	public GameFrame(String title, int gameWidth, int gameHeight)
	{
		this(title, null, gameWidth, gameHeight, gameWidth, gameHeight, false, false);
	}

	// Draw Graphics --------------------------------------------------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private class GamePanel extends JPanel
	{
		@Override
		public void paintComponent(Graphics g)
		{
			if (isFullScreen)
			{
				currentWindowWidth = screenSize.width;
				currentWindowHeight = screenSize.height;
			}
			else
			{
				windowWidth = currentFrame.getWidth() - windowWidthOffset;
				windowHeight = currentFrame.getHeight() - windowHeightOffset;
				currentWindowWidth = windowWidth;
				currentWindowHeight = windowHeight;
			}
			
			g.setColor(backgroundColor);
			g.fillRect(0, 0, currentWindowWidth, currentWindowHeight);
			
			if (scalable)
			{
				if (!scaleToWindow)
				{
					Dimension newImageDimensions = getNewImageDimensions(imageBaseWidth, imageBaseHeight);
					imageWidth = newImageDimensions.width;
					imageHeight = newImageDimensions.height;
					imageX = (int) Math.round((currentWindowWidth - imageWidth) / 2.0);
					imageY = (int) Math.round((currentWindowHeight - imageHeight) / 2.0);
				}
				else
				{
					imageWidth = currentWindowWidth;
					imageHeight = currentWindowHeight;
					imageX = 0;
					imageY = 0;
				}
			}
			else if (!usesStaticImage)
			{
				imageWidth = imageBaseWidth;
				imageHeight = imageBaseHeight;
				imageX = (int) Math.round((currentWindowWidth - imageWidth) / 2.0);
				imageY = (int) Math.round((currentWindowHeight - imageHeight) / 2.0);
			}
			
			drawGame();
	        g.drawImage(gameScreen, imageX, imageY, imageWidth, imageHeight, null);
			g.dispose();
		}
		
		private Dimension getNewImageDimensions(int imageWidth, int imageHeight)
		{
			double scaleFactor = 1.0;
			// Assume the game fits better to the width of the window when calculating the scale factor (uses % with doubles)
			// If GCD(GAME_WIDTH, GAME_HEIGHT) = 1 for modified scaling, then the result is the same as integer scaling
			if (integerScaling)
				scaleFactor = currentWindowWidth / imageWidth;
			else
				scaleFactor = (double) currentWindowWidth / imageWidth - (modifiedScaling ? (double) currentWindowWidth / imageWidth % (1.0 / GCD(imageWidth, imageHeight)) : 0.0);
			
			int newWidth = (int) (imageWidth * scaleFactor);
			int newHeight = (int) (imageHeight * scaleFactor);
			
			// If the corresponding height with this scaling, maintaining the aspect ratio and under the above assumption, exceeds the window size
			if (newHeight > currentWindowHeight)
			{
				// The assumption must have been false, so the game actually fits better to the height of the window.
				if (integerScaling)
					scaleFactor = currentWindowHeight / imageHeight;
				else
					scaleFactor = (double) currentWindowHeight / imageHeight - (modifiedScaling ? (double) currentWindowHeight / imageHeight % (1.0 / GCD(imageWidth, imageHeight)) : 0.0);
				
				newWidth = (int) (imageWidth * scaleFactor);
				newHeight = (int) (imageHeight * scaleFactor);
			}

			return new Dimension(newWidth, newHeight);
		}
		
		private static int GCD(int a, int b)
		{
			// Modify inputs based on assumptions of the algorithm
			a = Math.abs(a);
			b = Math.abs(b);
			if (a < b)
			{
				int temp = a;
				a = b;
				b = temp;
			}
			
			// Euclidean Algorithm
			return b == 0 ? a : GCD(b, a % b);
		}
	}
	
	// Drawing Methods ------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Contains all drawing instructions that should be executed whenever {@code repaintGame()} is called or when conditions are met that automatically trigger the underlying
	 * frame's {@code paintComponent(Graphics g)} method.
	 * @see #setColor setColor()
	 * @see #fillRect fillRect()
	 * @see #drawRect drawRect()
	 * @see #fillRoundedRect fillRoundedRect()
	 * @see #drawRoundedRect drawRoundedRect()
	 * @see #drawString drawString()
	 * @see #repaintGame repaintGame()
	 */
	protected abstract void drawGame();
	
	/**
	 * Sets this graphics context's current color to the specified color. All subsequent graphics operations using this graphics context use this specified color. 
	 * A {@code null} argument is silently ignored.
	 * @param color the new rendering color.
	 */
	public void setColor(Color color)
	{
		graphics2D.setColor(color);
	}
	
	/**
	 * Fills the specified rectangle. The left and right edges of the rectangle are at {@code x} and {@code x + width - 1}. The top and bottom edges are at {@code y} and 
	 * {@code y + height - 1}. The resulting rectangle covers an area {@code width} pixels wide by {@code height} pixels tall. The rectangle is filled using the graphics 
	 * context's current color.
	 * @param x the <i>x</i> coordinate of the rectangle to be filled.
	 * @param y the <i>y</i> coordinate of the rectangle to be filled.
	 * @param width the width of the rectangle to be filled.
	 * @param height the height of the rectangle to be filled.
	 */
	public void fillRect(int x, int y, int width, int height)
	{
		graphics2D.fillRect(x, y, width, height);
	}
	
	/**
	 * Draws the outline of the specified rectangle. The left and right edges of the rectangle are at {@code x} and {@code x + width}. The top and bottom edges are at {@code y} and 
	 * {@code y + height}. The rectangle is drawn using the graphics context's current color.
	 * @param x the <i>x</i> coordinate of the rectangle to be drawn.
	 * @param y the <i>y</i> coordinate of the rectangle to be drawn.
	 * @param width the width of the rectangle to be drawn.
	 * @param height the height of the rectangle to be drawn.
	 */
	public void drawRect(int x, int y, int width, int height)
	{
		graphics2D.drawRect(x, y, width, height);
	}
	
	/**
	 * Constructs and initializes a filled {@code RoundRectangle2D} from the specified {@code int} coordinates.
	 * @param x the <i>x</i> coordinate of the newly constructed {@code RoundRectangle2D}.
	 * @param y the <i>y</i> coordinate of the newly constructed {@code RoundRectangle2D}.
	 * @param width the width of the newly constructed {@code RoundRectangle2D}.
	 * @param height the height of the newly constructed {@code RoundRectangle2D}.
	 * @param arcWidth the width of the arc to use to round off the corners of the newly constructed {@code RoundRectangle2D}.
	 * @param arcHeight the height of the arc to use to round off the corners of the newly constructed {@code RoundRectangle2D}.
	 */
	public void fillRoundedRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		graphics2D.fill(new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight));
	}
	
	/**
	 * Constructs and initializes the outline of a {@code RoundRectangle2D} from the specified {@code int} coordinates.
	 * @param x the <i>x</i> coordinate of the newly constructed {@code RoundRectangle2D}.
	 * @param y the <i>y</i> coordinate of the newly constructed {@code RoundRectangle2D}.
	 * @param width the width of the newly constructed {@code RoundRectangle2D}.
	 * @param height the height of the newly constructed {@code RoundRectangle2D}.
	 * @param arcWidth the width of the arc to use to round off the corners of the newly constructed {@code RoundRectangle2D}.
	 * @param arcHeight the height of the arc to use to round off the corners of the newly constructed {@code RoundRectangle2D}.
	 */
	public void drawRoundedRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		graphics2D.draw(new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight));
	}
	
	/**
	 * Renders the text of the specified {@code String}, using the current text attribute state in the {@code Graphics2D} context. The baseline of the first character is at position 
	 * (<i>x</i>, <i>y</i>) in the User Space. The rendering attributes applied include the {@code Clip}, {@code Transform}, {@code Paint}, {@code Font} and {@code Composite} attributes. 
	 * For characters in script systems such as Hebrew and Arabic, the glyphs can be rendered from right to left, in which case the coordinate supplied is the location of the leftmost 
	 * character on the baseline.
	 * @param str the string to be rendered.
	 * @param x the <i>x</i> coordinate of the location where the {@code String} should be rendered.
	 * @param y the <i>y</i> coordinate of the location where the {@code String} should be rendered.
	 * @param size the size for the new {@code Font}.
	 * @param alpha the constant alpha to be multiplied with the alpha of the source. {@code alpha} must be a floating point number in the inclusive range [0.0, 1.0].
	 * @param font the font to use when drawing the specified string.
	 * @throws NullPointerException if {@code str} or {@code fontFileName} is {@code null}.
	 * @throws IllegalArgumentException if {@code alpha} is less than 0.0 or greater than 1.0.
	 * @throws SecurityException if the executing code does not have permission to read from the file.
	 */
	public void drawString(String str, int x, int y, int size, float alpha, Font font)
	{
		if (font == null) return;
		graphics2D.setFont(font.deriveFont(Font.PLAIN, size));
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics2D.drawString(str, x, y);
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
	}
	
	/**
	 * Repaints the game's graphics (a wrapper for {@code GAME_PANEL.repaint()}).
	 */
	public void repaintGame()
	{
		GAME_PANEL.repaint();
	}
	
	// Customization Methods ------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Indicates that a static (unchanging) image should be used to display the game's graphics. The specified game image's position and dimensions do not change in response to 
	 * any changes of the window size. Dimensions relate to the scaling of the game image; they do not change the defining dimensions of the game's coordinate system. A 
	 * {@code setScalable(true)} or {@code useAutoImage()} call will disable the use of a static image.
	 * @param imageX the <i>x</i> coordinate on the game window that the top-left corner of the game image should be located at.
	 * @param imageY the <i>y</i> coordinate on the game window that the top-left corner of the game image should be located at.
	 * @param imageWidth the width in pixels that the game image should span.
	 * @param imageHeight the height in pixels that the game image should span.
	 */
	public void useStaticImage(int imageX, int imageY, int imageWidth, int imageHeight)
	{
		usesStaticImage = true;
		scalable = false;
		this.imageX = imageX;
		this.imageY = imageY;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
	
	/**
	 * Indicates that an image that automatically scales itself according to the size of the game window should be used to display the game's graphics. Methods like 
	 * {@code setScalable(boolean scalable)}, {@code setModifiedScaling(boolean modifiedScaling)}, {@code setIntegerScaling(boolean integerScaling)}, and 
	 * {@code setScaleToWindow(boolean scaleToWindow)} affect exactly how the game image scales according to the game window size. Disables the use of a static image to display 
	 * the game's graphics.
	 */
	public void useAutoImage()
	{
		usesStaticImage = false;
		scalable = true;
	}
	
	/**
	 * Enters full-screen mode if the game was in windowed mode. Enters windowed mode if the game was in full-screen mode.
	 */
	public void toggleFullScreen()
	{
		JFrame newFrame = new JFrame();
		newFrame.setTitle(title);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		if (isFullScreen)
		{
			newFrame.setVisible(true);
			Insets insets = newFrame.getInsets();
			windowWidthOffset = insets.left + insets.right;
			windowHeightOffset = insets.top + insets.bottom;
			newFrame.setBounds
			(
				(int) Math.round((screenSize.width - (windowWidth + windowWidthOffset)) / 2.0),
				(int) Math.round((screenSize.height - (windowHeight + windowHeightOffset)) / 2.0),
				windowWidth + windowWidthOffset,
				windowHeight + windowHeightOffset
			);
		}
		else
		{
			newFrame.setUndecorated(true);
			newFrame.setVisible(true);
			newFrame.setSize(windowWidth, windowHeight);
			graphicsDevice.setFullScreenWindow(newFrame);
		}
		
		newFrame.add(GAME_PANEL);
		newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		newFrame.setFocusable(true);
		newFrame.setResizable(resizable);
		if (icon != null)
		{
			newFrame.setIconImage(icon);
		}
		
		GAME_PANEL.requestFocusInWindow();
		currentFrame.dispose();
		currentFrame = newFrame;
		isFullScreen = !isFullScreen;
	}
	
	// Getters/Setters ------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Determines whether the image used to display the game's graphics can change its size according to the game window size at runtime. Disables the use of a static image 
	 * to display the game's graphics if {@code true} is used. The scaling mode is remembered if the game image is made to be not scalable then later made to be scalable again.
	 * @param scalable indicates whether the game image is scalable.
	 */
	public void setScalable(boolean scalable)
	{
		this.scalable = scalable;
		usesStaticImage = scalable ? false : usesStaticImage;
	}
	
	/**
	 * Gets whether the image used to display the game's graphics can change its size according to the game window size at runtime.
	 * @return a {@code boolean} indicating whether the game image is scalable.
	 */
	public boolean getScalable()
	{
		return scalable;
	}
	
	/**
	 * Determines whether the image used to display the game's graphics uses a modified scaling algorithm when scaling according to the game window size. The algorithm is 
	 * intended to make scaled images appear less distorted more often with the tradeoff that the image might not scale completely to the game window's width or height. Has no 
	 * effect if the game image is not scalable.
	 * @param modifiedScaling indicates whether the game image uses modified scaling.
	 */
	public void setModifiedScaling(boolean modifiedScaling)
	{
		if (!scalable) return;
		this.modifiedScaling = modifiedScaling;
		integerScaling = modifiedScaling ? false : integerScaling;
		scaleToWindow = modifiedScaling ? false : scaleToWindow;
	}
	
	/**
	 * Gets whether the image used to display the game's graphics uses a modified scaling algorithm when scaling according to the game window size.
	 * @return a {@code boolean} indicating whether the game image uses modified scaling.
	 */
	public boolean getModifiedScaling()
	{
		return modifiedScaling;
	}
	
	/**
	 * Determines whether the image used to display the game's graphics uses integer scaling when scaling according to the game window size. The original game dimensions will 
	 * only scale in integer multiples. Can only be used to make the game image larger than the game's defining size. If the window size is smaller than the game's defining 
	 * size, then the game will not render. Used when a type of perfect scaling is desired with the tradeoff of very few different game image sizes. Has no effect if the game 
	 * image is not scalable.
	 * @param integerScaling indicates whether the game image uses integer scaling.
	 */
	public void setIntegerScaling(boolean integerScaling)
	{
		if (!scalable) return;
		this.integerScaling = integerScaling;
		modifiedScaling = integerScaling ? false : modifiedScaling;
		scaleToWindow = integerScaling ? false : scaleToWindow;
	}
	
	/**
	 * Gets whether the image used to display the game's graphics uses integer scaling when scaling according to the game window size.
	 * @return a {@code boolean} indicating whether the game image uses integer scaling.
	 */
	public boolean getIntegerScaling()
	{
		return integerScaling;
	}
	
	/**
	 * Determines whether the image used to display the game's graphics scales itself to fill the entire game window. The game image will stretch and change the game's 
	 * aspect ratio if necessary. Has no effect if the game image is not scalable.
	 * @param scaleToWindow indicates whether the game image scales to match the dimensions of the game window.
	 */
	public void setScaleToWindow(boolean scaleToWindow)
	{
		if (!scalable) return;
		this.scaleToWindow = scaleToWindow;
		modifiedScaling = scaleToWindow ? false : modifiedScaling;
		integerScaling = scaleToWindow ? false : integerScaling;
	}
	
	/**
	 * Gets whether the image used to display the game's graphics scales itself to fill the entire game window.
	 * @return a {@code boolean} indicating whether the game image scales to match the dimensions of the game window.
	 */
	public boolean getScaleToWindow()
	{
		return scaleToWindow;
	}
	
	/**
	 * Determines whether the image used to display the game's graphics uses anti-aliasing.
	 * @param antiAliasing indicates whether the game image uses anti-aliasing.
	 */
	public void setAntiAliasing(boolean antiAliasing)
	{
		this.antiAliasing = antiAliasing;
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	
	/**
	 * Gets whether the image used to display the game's graphics uses anti-aliasing.
	 * @return a {@code boolean} indicating whether the game image uses anti-aliasing.
	 */
	public boolean getAntiAliasing()
	{
		return antiAliasing;
	}
	
	/**
	 * Sets the type of {@code BufferedImage} used when drawing the game's graphics. See the {@link java.awt.image.BufferedImage#BufferedImage BufferedImage} class for valid 
	 * values.
	 * @param bufferedImageType the type of {@code BufferedImage} that the game image should be.
	 */
	public void setBufferedImageType(int bufferedImageType)
	{
		this.bufferedImageType = bufferedImageType;
		gameScreen = new BufferedImage(imageBaseWidth, imageBaseHeight, bufferedImageType);
		graphics2D = (Graphics2D) gameScreen.getGraphics();
		setAntiAliasing(antiAliasing);
	}
	
	/**
	 * Gets the type of {@link java.awt.image.BufferedImage BufferedImage} used when drawing the game's graphics. 
	 * @return an {@code int} representing the type of {@code BufferedImage} that the game image is.
	 */
	public int getBufferedImageType()
	{
		return bufferedImageType;
	}
	
	/**
	 * Sets the background color of the game window.
	 * @param backgroundColor the desired background color of the game window.
	 */
	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * Gets the background color of the game window.
	 * @return a {@code Color} representing the current background color of the game window.
	 */
	public Color getBackgroundColor()
	{
		return backgroundColor;
	}
	
	/**
	 * Determines whether the game window should be resizable by the user (does not affect full-screen functionality).
	 * @param resizable indicates whether the game window can be resized at runtime.
	 */
	public void setResizable(boolean resizable)
	{
		this.resizable = resizable;
	}
	
	/**
	 * Gets whether the game window is resizable by the user.
	 * @return a {@code boolean} indicating whether the game window can be resized at runtime.
	 */
	public boolean getResizable()
	{
		return resizable;
	}
	
	/**
	 * Sets the title of the game window.
	 * @param title the title to be displayed in the frame's border. A {@code null} value is treated as an empty string, "".
	 */
	public void setTitle(String title)
	{
		this.title = title;
		currentFrame.setTitle(title);
	}
	
	/**
	 * Gets the title of the game window.
	 * @return a {@code String} representing the title being displayed in the frame's border.
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Sets the image to be used as the game window's icon.
	 * @param icon the icon image to be displayed.
	 */
	public void setIcon(Image icon)
	{
		this.icon = icon;
		if (icon == null) return;
		currentFrame.setIconImage(icon);
	}
	
	/**
	 * Gets the image being used as the game window's icon.
	 * @return an {@code Image} object representing the image being used as the game window's icon. If no icon has been specified, returns {@code null)}.
	 */
	public Image getIcon()
	{
		return icon;
	}

	/**
	 * Gets whether the game is in full-screen mode
	 * @return a {@code boolean} indicating whether the game is in full-screen mode.
	 */
	public boolean getIsFullScreen()
	{
		return isFullScreen;
	}
}
