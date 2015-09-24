import java.awt.Color;
import java.util.Random;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private static int IDNumber = 0;
	private int ID;

	private int energy;
	private int x;
	private int y;

	private RabbitsGrassSimulationSpace rgsSpace;

	public RabbitsGrassSimulationAgent(int energy) {
		this.energy = energy;
		this.x = -1;
		this.y = -1;

		ID = ++IDNumber;
	}

	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.white);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setSpace(RabbitsGrassSimulationSpace space) {
		rgsSpace = space;
	}

	public int getEnergy() {
		return energy;
	}

	public void report() {
		System.out.println("Rabbit " + ID + " at " + x + ", " + y + " has " + getEnergy() + " energy.");
	}

	public void step() {
		// Get all neighboring cells
		int[][] neighbors = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

		Random rand = new Random();
		int ind = rand.nextInt(neighbors.length);

		int sizeX = rgsSpace.getCurrentAgentSpace().getSizeX();
		int sizeY = rgsSpace.getCurrentAgentSpace().getSizeY();

		int newX = (x + neighbors[ind][0] + sizeX) % sizeX;
		int newY = (y + neighbors[ind][1] + sizeY) % sizeY;

		int counter = 0;
		report();

		while (rgsSpace.isCellOccupied(newX, newY) && counter < 20) {

			ind = rand.nextInt(neighbors.length);

			newX = (x + neighbors[ind][0] + sizeX) % sizeX;
			newY = (y + neighbors[ind][1] + sizeY) % sizeY;

			counter++;
		}

		if (counter < 20)
			rgsSpace.moveAgent(x, y, newX, newY);
	}

	public void decreaseEnergy() {
		energy -= RabbitsGrassSimulationModel.getMoveCost();
	}

	public void increaseEnergyBy(int energyAt) {
		if (energy + energyAt > RabbitsGrassSimulationModel.getMaxEnergy()) {
			energy = RabbitsGrassSimulationModel.getMaxEnergy();
		} else {
			energy += energyAt;
		}
	}

	public void reproduce() {
		energy -= RabbitsGrassSimulationModel.getReproductionCost();
	}
}
