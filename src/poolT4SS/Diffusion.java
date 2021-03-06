package poolT4SS;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class Diffusion {
	private ValueLayerDiffuser diffuser;
	
	public Diffusion(ValueLayer vl) {
		diffuser = new ValueLayerDiffuser((IGridValueLayer)vl,1.0,0.5);
	}
	
	@ScheduledMethod(start = 1, interval = 10, shuffle=true)
	public void step() {
		diffuser.diffuse();
	}

}
