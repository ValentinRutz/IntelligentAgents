import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Stream;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

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

		IDNumber++;
		ID = IDNumber;
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
		Object2DGrid grid = rgsSpace.getCurrentAgentSpace();
		int[][] neighbors = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

		// Get random free neighboring cell
		Stream<int[]> freeNeighbors = Arrays.stream(neighbors).filter(xy -> rgsSpace.isCellOccupied((x + xy[0]) % grid.getSizeX(), (y + xy[1]) % grid.getSizeY()));
		int[] move = freeNeighbors.findAny().orElse(new int[] { 0, 0 });

		// Go to the cell (update positions in agent and space)
		rgsSpace.moveAgent(x, y, (x + move[0]) % grid.getSizeX(), (y + move[1]) % grid.getSizeY());

	}

	public void decreaseEnergy() {
		energy -= RabbitsGrassSimulationModel.getMoveCost();
	}

	public void increaseEnergyBy(int energyAt) {
		energy += energyAt;
	}

	public void reproduce() {
		energy -= RabbitsGrassSimulationModel.getReproductionCost();
	}
}
