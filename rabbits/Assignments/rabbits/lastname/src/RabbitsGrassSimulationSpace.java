import java.util.Random;

import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		grassSpace = new Object2DGrid(xSize, ySize);
		agentSpace = new Object2DGrid(xSize, ySize);

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	public void growGrass(int rate) {
		Random rand = new Random();
		for (int i = 0; i < rate; i++) {
			int x = rand.nextInt(grassSpace.getSizeX() - 1) + 1;
			int y = rand.nextInt(grassSpace.getSizeY() - 1) + 1;

			// Get the value of the object at those coordinates
			int I = getEnergyAt(x, y);

			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(I + RabbitsGrassSimulationModel.getGrassEnergy()));
		}
	}

	public int getEnergyAt(int x, int y) {
		if (grassSpace.getObjectAt(x, y) != null) {
			return ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			return 0;
		}
	}

	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}

	public Object2DGrid getCurrentAgentSpace() {
		return agentSpace;
	}

	public boolean isCellOccupied(int x, int y) {
		return agentSpace.getObjectAt(x, y) != null;
	}

	public boolean addAgent(RabbitsGrassSimulationAgent agent) {
		boolean agentAdded = false;
		int count = 0;
		int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

		while ((agentAdded == false) && (count < countLimit)) {
			int x = (int) (Math.random() * (agentSpace.getSizeX()));
			int y = (int) (Math.random() * (agentSpace.getSizeY()));
			if (isCellOccupied(x, y) == false) {
				agentSpace.putObjectAt(x, y, agent);
				agent.setXY(x, y);
				agent.setSpace(this);
				agentAdded = true;
			}
			count++;
		}

		return agentAdded;
	}

	public RabbitsGrassSimulationAgent getAgentAt(int x, int y) {
		RabbitsGrassSimulationAgent retVal = null;
		if (agentSpace.getObjectAt(x, y) != null) {
			retVal = (RabbitsGrassSimulationAgent) agentSpace.getObjectAt(x, y);
		}
		return retVal;
	}

	public void removeAgentAt(int x, int y) {
		agentSpace.putObjectAt(x, y, null);
	}
	
	public void moveAgent(int oldX, int oldY, int newX, int newY) {
		RabbitsGrassSimulationAgent agent = getAgentAt(oldX, oldY);
		agent.setXY(newX, newY);
		agent.decreaseEnergy();

		agentSpace.putObjectAt(newX, newY, agent);
		removeAgentAt(oldX, oldY);
	}

}
