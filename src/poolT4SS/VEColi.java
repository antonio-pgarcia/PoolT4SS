package poolT4SS;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

@SuppressWarnings("rawtypes")
public class VEColi {
	private Context context = null;
	private GridValueLayer vl;
	private Grid grid;
	private ContinuousSpace space;
	
	Parameters p = RunEnvironment.getInstance().getParameters();
	private int doublingTime = (Integer) p.getValue("doublingTime");
	private double p1P = ((double) p.getValue("p1P"));
	private int p1Cost = ((Integer) p.getValue("p1Cost"))/100;
	private int p2Cost = ((Integer) p.getValue("p2Cost"))/100;
	private double cov = 0.10;
	private double cyclePoint = 0.70;
	
	// State variables
	private double heading;    			// The Agent heading in degrees
	private double mass;       			// The Agent mass
	private double divisionMass;		// The Agent mass at division
	private boolean plasmid1 = false;	// The Agent is infected by plasmid (T4SS) P1
	private boolean plasmid2 = false;	// The Agent is infected by plasmid (Cheater) P2
	private boolean pgamma = false;		// The outcome of probability P(gamma0) for this Agent 				
	
	public VEColi() {
		Defaults();
	}
	
	public VEColi(boolean p1, boolean p2) {
		Defaults();
		plasmid1 = p1;
		plasmid2 = p2;
		if(plasmid1) divisionMass= addCost(divisionMass, p1Cost); 
		if(plasmid2) divisionMass= addCost(divisionMass, p2Cost);
	}
	
	public VEColi(double h, double m, boolean p1, boolean p2) {
		heading= h;
		mass= m;
		divisionMass = 2 * mass;
		plasmid1 = p1;
		plasmid2 = p2;		
	}

	private void Defaults() {
		heading= RandomHelper.getUniform().nextDouble() * 360;
		mass= Math.round(doublingTime * cov * RandomHelper.getNormal().nextDouble() + doublingTime);
		divisionMass = 2 * mass;
	}
	
	@SuppressWarnings("unchecked")
	private void Context() {
		if(null != context) return;
		
		context = (Context) ContextUtils.getContext(this);
		vl = (GridValueLayer) context.getValueLayer("substrate");
		grid = (Grid) context.getProjection("grid-space");
		space = (ContinuousSpace) context.getProjection("continuous-space");
		
		// Put agent into appropriate grid position
		NdPoint point = space.getLocation(this);
		grid.moveTo(this, (int) point.getX(), (int) point.getY());
		
		if(RandomHelper.getUniform().nextDouble() < p1P) pgamma = true;
	}
	
	public double addCost(double v, double c) {
		return v + v * c;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public double getMass() {
		return mass;
	}
	
	public boolean isR() {
		return !plasmid1 && !plasmid2;  
	}
	
	public boolean isD() {
		return !isR();
	}
	
	public boolean hasP1() {
		return plasmid1;
	}
	
	public boolean hasP2() {
		return plasmid2;
	}
	
	public boolean hasP1P2() {
		return plasmid1 && plasmid2;
	}
	
	public void receiveP1() {
		plasmid1= true;
		divisionMass= addCost(divisionMass, p1Cost); 
	}
	
	public void receiveP2() {
		plasmid2= true;
		divisionMass= addCost(divisionMass, p2Cost); 
	}
	
	public double addNoise(double v, double m) {
		double noise = v * Math.random() * m;
		if(0.5 < Math.random()) 
			return v + noise;
		else
			return v - noise;
	}
	
	public double Uptake(double r) {
		double v= 0;
		NdPoint point = space.getLocation(this); 
		double c= vl.get(point.getX(),point.getY());

		if(c > 0) {
			v=(c >= r ? r : c);
			vl.set(c-v, (int) point.getX(), (int) point.getY());
		} 
		return v;
	}
	
	public double Growth() {
		double v = Uptake(1);
		mass= mass + v;
		return v;
	}
	
	@SuppressWarnings("unchecked")
	public void Division() {
		if( divisionMass < mass) {
			VEColi vecoli = new VEColi(heading + (-45 + 90 * Math.random()), mass - addNoise(mass/2,cov), plasmid1, plasmid2);
			mass = mass - vecoli.getMass();
			if(context.add(vecoli)) {
				NdPoint point = space.getLocation(this);
				space.moveTo(vecoli,point.getX() + 1 * Math.sin(Math.toRadians(heading)), point.getY() + 1 * Math.cos(Math.toRadians(heading)),point.getZ());
			}
		}
	}
	
	public void Conjugation() {
		if(!hasP1()) return;
		if(!pgamma) return;
		if(mass < divisionMass * cyclePoint) return;
		GridPoint p = grid.getLocation(this);
		VEColi vecoli = null;
		for (Object o: grid.getObjectsAt(p.getX(),p.getY())){
			if (o instanceof VEColi) 
				if(((VEColi) o).isR() || ((VEColi) o).hasP2()) {
					vecoli = (VEColi) o;
					break;
				}
		}
		
		if(null != vecoli) {
			if(vecoli.hasP2()) 
				vecoli.receiveP1();
			else{
				if(RandomHelper.getUniform().nextDouble() < 0.5)
					vecoli.receiveP1();
				else
					vecoli.receiveP2();
			}
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, shuffle=true)
	public void step() {
		Context();
		if(Growth() > 0) {
			Conjugation();
			Division();
		}
	}
}
