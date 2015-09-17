import java.util.Random;

import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		grassSpace = new Object2DGrid(xSize, ySize);
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

	private int getEnergyAt(int x, int y) {
		if (grassSpace.getObjectAt(x, y) != null) {
			return ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			return 0;
		}
	}
	

	  public Object2DGrid getCurrentGrassSpace(){
	    return grassSpace;
	  }

}
