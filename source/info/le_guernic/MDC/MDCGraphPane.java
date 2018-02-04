/**
 * Copyright (c) 2018, Gurvan LE GUERNIC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package info.le_guernic.MDC;

import info.le_guernic.MDC.State;
import info.le_guernic.MDC.Transition;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;

class MDCGraphPane extends GraphZoomScrollPane {

    protected MDCGraphPane(Graph<State,Transition> graph, Dimension dim) {
	super(
	  new VisualizationViewer<State,Transition>(
	  	new FRLayout<State,Transition>(graph, dim)	   
	  )
	);
	Component c = this.getComponent(0);
	if (c instanceof VisualizationViewer) {
	    final VisualizationViewer<State,Transition> viewer = ((VisualizationViewer<State,Transition>) c);

	    viewer.getRenderContext().setVertexLabelTransformer(
		    new Function<State,String>(){
			public String apply(State s) { return s.getGUIName(); }
		    }
		);
	    viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
	    
	    viewer.getRenderContext().setEdgeLabelTransformer(
		    new Function<Transition,String>(){
			public String apply(Transition t) { return t.getGUIEdgeDescription(); }
		    }
		);
	    viewer.getRenderContext().setEdgeLabelClosenessTransformer(
		    new ConstantDirectionalEdgeValueTransformer<State,Transition>(.5,.4)
                );
	    viewer.setEdgeToolTipTransformer(
		    new Function<Transition,String>(){
			public String apply(Transition t) { return t.getGUIName(); }
		    }
		);
	    
	    final AbstractModalGraphMouse gm = new DefaultModalGraphMouse<State,Transition>();
	    viewer.setGraphMouse(gm);
	    viewer.addKeyListener(gm.getModeKeyListener());
	    viewer.addKeyListener(new MDCGraphKeyListener<State,Transition>(viewer));
	    viewer.setToolTipText(
	      String.join(
		System.getProperty("line.separator"),
		"<html>",
		"  Type '+' to zoom in<br/>",
		"  Type '-' to zoom out<br/>",
		"  Type 'r' to reset zoom<br/>",
		"  Type 's' to reset layout<br/>",
		"  Type 'p' for Pick mode<br/>",
		"  <p style='margin-left: 10px'>",
		"    +rectangle to select multiple vertices<br/>",
		"    +shift to add to selection<br/>",
		"    +ctrl to center view on clicked vertex",
		"  </p>",
		"  Type 't' for Transform mode<br/>",
		"  <p style='margin-left: 10px'>",
		"    +shift to rotate<br/>",
		"    +ctrl to shear",
		"  </p>",
		"</html>"
	      )
	    );
	} else {
	    System.out.println("This case should never arise ! Check " + getClass().getName());
	}
    }

    private static class MDCGraphKeyListener<State,Transition> extends KeyAdapter {

	private final VisualizationViewer<State,Transition> viewer;
	private final ScalingControl scaler = new CrossoverScalingControl();
	    
	protected MDCGraphKeyListener(VisualizationViewer<State,Transition> v) {
	    viewer = v;
	}

	private void resetZoomScale() {
	    viewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
	    viewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
	}
	    
	public void keyTyped(KeyEvent e) {
	    char c = e.getKeyChar();
	    switch(c){
	    case '+' :
		System.out.println("Zoom in");
		scaler.scale(viewer, 1.1f, viewer.getCenter());
		break;
	    case '-' :
		System.out.println("Zoom out");
		scaler.scale(viewer, 1/1.1f, viewer.getCenter());
		break;
	    case 'r' :
		System.out.println("Reset zoom");
		resetZoomScale();
		break;
	    case 's' :
		System.out.println("Set zoom");
		final Dimension dim = viewer.getParent().getParent().getSize(null);
		dim.setSize(dim.getWidth() * 0.9, dim.getHeight() * 0.9);
		final Layout<State,Transition> layout = viewer.getGraphLayout();
		layout.reset();
		layout.setSize(dim);
		// layout.reset();
		resetZoomScale();
		break;
	    default :
		System.out.println("Key typed: "+c);
	    }
	}

    }
	
}
