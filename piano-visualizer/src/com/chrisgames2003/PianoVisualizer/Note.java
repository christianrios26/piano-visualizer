package com.chrisgames2003.PianoVisualizer;

import java.awt.Color;

final class Note
{
	protected PianoVisualizerGame pianoVisualizer;
	protected String name;
	protected Color color;
	protected int xPos, yPos, width, height, numericPitch;
	protected boolean isFalling, isGrowing;

	protected Note(PianoVisualizerGame pianoVisualizer, String name, Color color, int xPos, int yPos, int width, int numericPitch, boolean isFalling)
	{
		this.pianoVisualizer = pianoVisualizer;
		this.name = name;
		this.color = color;
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		height = 0;
		this.numericPitch = numericPitch;
		this.isFalling = isFalling;
		isGrowing = true;
	}

	protected void draw()
	{
		pianoVisualizer.setColor(color);
		pianoVisualizer.fillRoundedRect(xPos, yPos, width, height, 8, 8);
	}
}
