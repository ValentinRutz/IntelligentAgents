import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

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
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
	private OpenSequenceGraph numberOfRabbits;

	// Default values for parameters
	private static final int NBRABBITS = 5;
	private static final int GRIDHEIGHT = 20;
	private static final int GRIDWIDTH = 20;
	private static final int BIRTHTHRESHOLD = 18;
	private static final int GRASSGROWTHRATE = 15;

	// User modifiable parameters
	private static int nbRabbits = NBRABBITS;
	private static int gridHeight = GRIDHEIGHT;
	private static int gridWidth = GRIDWIDTH;
	private static int birthThreshold = BIRTHTHRESHOLD;
	private static int grassGrowthRate = GRASSGROWTHRATE;

	// Internal parameters
	private static final int MOVECOST = 1;
	private static final int LIVINGCOST = 1;
	private static int REPRODUCTIONCOST = BIRTHTHRESHOLD / 2;
	private static final int GRASSENERGY = 3;
	private static final int MAXGRASS = 100;
	private static final int MAXENERGY = 100;
	private static int INITENERGY = BIRTHTHRESHOLD;
	private RabbitsGrassSimulationModel self = this;

	class NumberOfRabbits implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return rabbitList.size();
		}
	}

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
		numberOfRabbits.display();
	}

	public void buildModel() {
		System.out.println("Building model...");
		rgsSpace = new RabbitsGrassSimulationSpace(gridWidth, gridHeight);
		// rgsSpace.growGrass(grassGrowthRate);

		for (int i = 0; i < nbRabbits; i++) {
			addNewAgent();
		}

		for (int i = 0; i < rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent cda = rabbitList.get(i);
			cda.report();
		}
	}

	public void buildSchedule() {
		System.out.println("Building schedule...");
		class RabbitsGrassSimulationStep extends BasicAction {
			public void execute() {
				System.out.println("We have " + rabbitList.size() + " rabbits in the grid");
				SimUtilities.shuffle(rabbitList);
				if (rabbitList.size() == 0) {
					self.stop();
				}

				int newborns = 0;
				for (Iterator<RabbitsGrassSimulationAgent> it = rabbitList.iterator(); it.hasNext();) {
					RabbitsGrassSimulationAgent rabbit = it.next();
					rabbit.step();
					int x = rabbit.getX(), y = rabbit.getY(), energy = rabbit.getEnergy();
					// Check if agent should be dead
					if (energy <= 0) {
						rgsSpace.removeAgentAt(x, y);
						it.remove();
						// indicesToRemove.add(i);
					} else {
						// If not, eat grass
						rabbit.increaseEnergyBy(rgsSpace.getEnergyAt(x, y));
						rgsSpace.getCurrentGrassSpace().putObjectAt(x, y, new Integer(0));
						// Check if can reproduce
						if (energy > getBirthThreshold()) {
							rabbit.reproduce();
							newborns++;
						}

					}
				}

				for (int i = 0; i < newborns; i++) {
					addNewAgent();
				}

				rgsSpace.growGrass(grassGrowthRate);

				displaySurf.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(1, new RabbitsGrassSimulationStep());
		
		class RabbitsUpdateNumberOfRabbits extends BasicAction {
		      public void execute(){
		        numberOfRabbits.step();
		      }
		    }

		    schedule.scheduleActionAtInterval(1, new RabbitsUpdateNumberOfRabbits());
	}

	public void buildDisplay() {
		System.out.println("Building display...");
		ColorMap map = new ColorMap();

		map.mapColor(0, Color.black);
		for (int i = 1; i <= MAXGRASS; i++) {
			map.mapColor(i, Color.green);
		}

		Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
		Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
		displayAgents.setObjectList(rabbitList);

		displaySurf.addDisplayable(displayGrass, "Grass");
		displaySurf.addDisplayable(displayAgents, "Agents");
		
	    numberOfRabbits.addSequence("Rabbit", new NumberOfRabbits());

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

	private void addNewAgent() {
		RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(INITENERGY);
		if(rgsSpace.addAgent(a)) {
			rabbitList.add(a);
		}
	}

	public int getNbRabbits() {
		return nbRabbits;
	}

	public void setNbRabbits(int nbRabbits) {
		RabbitsGrassSimulationModel.nbRabbits = nbRabbits;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		if(gridHeight > 0)
			RabbitsGrassSimulationModel.gridHeight = gridHeight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		if(gridWidth > 0)
			RabbitsGrassSimulationModel.gridWidth = gridWidth;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		INITENERGY = birthThreshold;
		REPRODUCTIONCOST = birthThreshold / 3;
		RabbitsGrassSimulationModel.birthThreshold = birthThreshold;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int grassGrowthRate) {
		RabbitsGrassSimulationModel.grassGrowthRate = grassGrowthRate;
	}

	@SuppressWarnings("unchecked")
	public void setup() {
		System.out.println("Running setup...");
		RangePropertyDescriptor rdNbRabbits = new RangePropertyDescriptor("NbRabbits", 0, GRIDHEIGHT * GRIDWIDTH, 100);
		descriptors.put("NbRabbits", rdNbRabbits);
		RangePropertyDescriptor rdGridHeight = new RangePropertyDescriptor("GridHeight", 0, 500, 100);
		descriptors.put("GridHeight", rdGridHeight);
		RangePropertyDescriptor rdGridWidth = new RangePropertyDescriptor("GridWidth", 0, 500, 100);
		descriptors.put("GridWidth", rdGridWidth);
		RangePropertyDescriptor rdBirthThreshold = new RangePropertyDescriptor("BirthThreshold", 0, 100, 20);
		descriptors.put("BirthThreshold", rdBirthThreshold);
		RangePropertyDescriptor rdGrassGrowthRate = new RangePropertyDescriptor("GrassGrowthRate", 0, 500, 100);
		descriptors.put("GrassGrowthRate", rdGrassGrowthRate);
		
		rgsSpace = null;
		rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);

		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		if (numberOfRabbits != null) {
			numberOfRabbits.dispose();
		}
		numberOfRabbits = null;

		displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");
		numberOfRabbits = new OpenSequenceGraph("Number of rabbits",this);

		registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurf);
	    this.registerMediaProducer("Plot", numberOfRabbits);

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

	public static int getMaxEnergy() {
		return MAXENERGY;
	}

	public static int getReproductionCost() {
		return REPRODUCTIONCOST;
	}

	public static int getLivingCost() {
		return LIVINGCOST;
	}
}
