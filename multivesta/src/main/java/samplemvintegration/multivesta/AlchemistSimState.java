package samplemvintegration.multivesta;

import it.unibo.alchemist.AlchemistMultiVesta;
import it.unibo.alchemist.adapter.SimultationAdapter;
import vesta.mc.NewState;
import vesta.mc.ParametersForState;

/**
 * This class exemplifies how to integrate a simulator in MultiVeStA
 * This class is the 'connection point' among the simulator and MultiVeStA.
 * It exposes to MultiVeStA the methods necessary to interact with the simulator.
 * @author Andrea Vandin
 *
 */
public class AlchemistSimState extends NewState {

	private static final long serialVersionUID = 736491436944470648L;

	/**
	 * An object representing the actual simulator
	 */
	private SimultationAdapter alchemistSimulator;

	/**
	 * This method is invoked once per MultiVeStA analysis.
	 * It should take care of things necessary to be done only once independently of the number of simulations or simulation steps performed
	 * @param parameters commented within the method
	 */
	public AlchemistSimState(final ParametersForState parameters) {
		super(parameters);
	}

	/**
	 * This method is invoked before starting every new simulation
	 * @param randomSeed the seed to be used to initialize the random number generator of the simulator for the new simulation
	 */
	@Override
	public void setSimulatorForNewSimulation(int randomSeed) {
		// Necessary to reset the simulator to its initial state
		//	e.g., reset timers/counters, empty data structures, etc
		// BEWARE: the parameter randomSeed must be used to reset the random number generator of the simulator
		//			If this is not the case, there are no guarantees on the genuinity of the results computed by MultiVeStA
		alchemistSimulator = AlchemistMultiVesta.INSTANCE.launch(randomSeed, new String[]{}); // todo: impostare args
	}
	
	/**
	 * This method is invoked to perform a single step of simulation
	 * Most simulators perform simulations by having a loop, where each iteration takes care of advancing one step. This method corresponds to one iteration of such loop 
	 */
	@Override
	public void performOneStepOfSimulation() {
		alchemistSimulator.doStep();
	}
	
	/**
	 * This method returns the 'current simulated time' of the current state of the simulation
	 * The notion of 'simulated time' depends on the nature of the simulator. It can be just a counter of steps of simulation, or a some other notion computed by the simulator
	 */
	@Override
	public double getTime() {
		return alchemistSimulator.getTime();
	}
	
	/**
	 *	This method asks the simulator to evaluate a given observation 'obs' in the current simulation state
	 *	What an observation of interest is depends on the considered simulator.
	 *	It can be 
	 *	- a 0/1 property: 'did a given event or condition happen?', returning 0 or 1
	 *	- any real property: 'how many people escaped?' 'how long is the queue'? 
	 */
	@Override
	public double rval(String obs) {
		return alchemistSimulator.rval(obs);
	}
	
	/**
	 *	This is the same as the other rval method. 
	 *	The difference is that here we don't pass the 'name' of an observation, but its ID.
	 *	It can lead to slightly more efficient analysis, but makes writing queries more cumbersome   
	 */
	@Override
	public double rval(int obsID) {
		return alchemistSimulator.rval(obsID);
	}
	
	//Advanced topic to be discussed later on
	/**
	 * If a simulator has a notion of 'final state' (i.e., every simulation terminates when a given condition is met), 
	 * and we are not interested in values from intermediate states, we cdo not need to control every step of simulation.
	 * Rather, we can ask the simulator to go on untile simulation is completed.
	 */
	@Override
	public void performWholeSimulation() {
		// TODO Add here code to perform one whole simulation
	}
}
