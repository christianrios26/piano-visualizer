package com.chrisgames2003.PianoVisualizer;

import java.awt.Color;

final class PianoKey
{
	protected PianoVisualizerGame pianoVisualizer;
	protected String name;
	protected Color color;
	protected boolean isBlackSized;
	protected int xPos, yPos;

	protected PianoKey(PianoVisualizerGame pianoVisualizer, String name, Color color, boolean isBlackSized)
	{
		this.pianoVisualizer = pianoVisualizer;
		this.name = name;
		this.color = color;
		this.isBlackSized = isBlackSized;
	}

	protected void draw()
	{
		if (!isBlackSized)
		{
			pianoVisualizer.setColor(color);
			pianoVisualizer.fillRect(xPos, yPos, 24, 156);
			pianoVisualizer.setColor(PianoVisualizerGame.getShiftedColor(color, PianoVisualizerGame.WHITE, PianoVisualizerGame.WHITE_BORDER));
			pianoVisualizer.fillRect(xPos + 22, yPos, 2, 156);
		}
		else
		{
			pianoVisualizer.setColor(color);
			pianoVisualizer.fillRect(xPos, yPos, 14, 102);
		}
	}
}