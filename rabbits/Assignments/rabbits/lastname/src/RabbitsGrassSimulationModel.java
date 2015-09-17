import java.awt.Color;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private Schedule schedule;
	private RabbitsGrassSimulationSpace rgsSpace;
	private DisplaySurface displaySurf;

	// Default values for parameters
	private static final int NBRABBITS = 1;
	private static final int GRIDHEIGHT = 20;
	private static final int GRIDWIDTH = 20;
	private static final int BIRTHTHRESHOLD = 1;
	private static final int GRASSGROWTHRATE = 1;

	// User modifiable parameters
	private int nbRabbits = NBRABBITS;
	private int gridHeight = GRIDHEIGHT;
	private int gridWidth = GRIDWIDTH;
	private int birthThreshold = BIRTHTHRESHOLD;
	private int grassGrowthRate = GRASSGROWTHRATE;

	// Internal parameters
	private static final int MOVECOST = 1;
	private static final int GRASSENERGY = 1;
	private static final int MAXGRASS = 1000;
	
	public static void main(String[] args) {

		// System.out.println("Rabbit skeleton");
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);

	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
	    displaySurf.display();
	}

	public void buildModel() {
		System.out.println("Building model...");
		rgsSpace = new RabbitsGrassSimulationSpace(gridWidth, gridHeight);
		rgsSpace.growGrass(grassGrowthRate);
	}

	public void buildSchedule() {
		System.out.println("Building schedule...");

	}

	public void buildDisplay() {
		System.out.println("Building display...");
		ColorMap map = new ColorMap();

		map.mapColor(0, Color.black);
		for (int i = 1; i <= MAXGRASS; i++) {
			map.mapColor(i, Color.green);
		}

		Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);

		displaySurf.addDisplayable(displayGrass, "Grass");

	}

	public String[] getInitParam() {
		String[] initParams = { "NbRabbits", "GridHeight", "GridWidth", "BirthThreshold", "GrassGrowthRate" };
		return initParams;
	}

	public String getName() {
		return "Rabbits Grass Simulation";
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getNbRabbits() {
		return nbRabbits;
	}

	public void setNbRabbits(int nbRabbits) {
		this.nbRabbits = nbRabbits;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int grassGrowthRate) {
		this.grassGrowthRate = grassGrowthRate;
	}

	public void setup() {
		System.out.println("Running setup...");
		rgsSpace = null;

		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");

		registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurf);
	}

	public static int getMoveCost() {
		return MOVECOST;
	}

	public static int getGrassEnergy() {
		return GRASSENERGY;
	}

	public static int getMaxGrass() {
		return MAXGRASS;
	}
}
