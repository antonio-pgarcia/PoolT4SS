package poolT4SS;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class Style2DVEColi extends DefaultStyleOGL2D {
	private Color PLASMID1 = new Color(224, 0, 78);
	private Color PLASMID2 = new Color(84, 106, 150);
	private Color PLASMID12 = new Color(249, 243, 69);
	
	@Override
	public Color getColor(Object o){
		
		if (o instanceof VEColi) {
			if(((VEColi) o).isR())
				return Color.GREEN;
			if(((VEColi) o).hasP1() && ((VEColi) o).hasP2())
				return PLASMID12;
			if(((VEColi) o).hasP1())
				return PLASMID1;
			if(((VEColi) o).hasP2())
				return PLASMID2;
		} 
		return null;
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) return shapeFactory.createRectangle(5, 12);
		return super.getVSpatial(agent, spatial);
	}
	
	@Override
	public float getRotation(Object agent) {
		// TODO Auto-generated method stub
		//return super.getRotation(agent);
		return (float) ((VEColi) agent).getHeading();
	}
	
	@Override
	public float getScale(Object o) {
		if (o instanceof VEColi)
			return 1f;
		else if (o instanceof VEColi)
			return 1f;
		
		return 1f;
	}
}
