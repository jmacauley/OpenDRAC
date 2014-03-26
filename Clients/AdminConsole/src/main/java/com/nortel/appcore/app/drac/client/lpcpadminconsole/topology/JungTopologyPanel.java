/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.client.lpcpadminconsole.topology;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.GetGraphDataWorker;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.JungGraphPreferences.NeLabels;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyViewEventHandler.EVENT_TYPE;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

/**
 * @author pitman
 */

@SuppressWarnings("serial")
public class JungTopologyPanel {
  private static final Logger log = LoggerFactory.getLogger(JungTopologyPanel.class);


	static class DracEdgePaintTransformer<E> implements Transformer<E, Paint> {
		private final PickedInfo<E> pi;
		private final Paint networkDiscoveredPaint;
		private final Paint manualPaint;
		private final Paint networkDiscoveredAndManualPaint;
		private final Paint pickedPaint;

		public DracEdgePaintTransformer(PickedInfo<E> pick,
		    JungGraphPreferences graphPrefs) {
			pi = pick;
			networkDiscoveredPaint = graphPrefs.getEdgeColorNetworkDiscovered();
			manualPaint = graphPrefs.getEdgeColorManual();
			networkDiscoveredAndManualPaint = graphPrefs
			    .getEdgeColorNetworkDiscoveredAndManual();
			pickedPaint = graphPrefs.getEdgeColorSelected();
		}

		@Override
		public Paint transform(E e) {
			DracEdge edge = (DracEdge) e;

			if (pi.isPicked(e)) {
				/*
				 * If we have set a specific color against the edge use that color to
				 * paint with, otherwise the default paint color will do. We use this
				 * trick to show working/protection paths by setting the color different
				 * on each path.
				 */
				Paint p = edge.getPaintColor();
				return p == null ? pickedPaint : p;
			}
			else if (edge.isManual()) {
				return manualPaint;
			}
			else if (edge.hasEclipsedManualLink()) {
				return networkDiscoveredAndManualPaint;
			}
			else {
				return networkDiscoveredPaint;
			}
		}
	}

	/**
	 * Draw manual topological links with a dotted line so they stand out.
	 */
	static class DracEdgeWeightStrokeFunction<E> implements
	    Transformer<E, Stroke> {
		private static final float[] DASHING = { 5.0f };

		private static final Stroke DOTTED = new BasicStroke(2.5f,
		    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, DASHING, 0f);
		private static final Stroke SKINNY_DOTTED = new BasicStroke(1.0f,
		    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, DASHING, 0f);
		private static final Stroke FAT = new BasicStroke(5.0f);

		private static final Stroke NORMAL = new BasicStroke(2.5f);

		private static final Stroke COMPOUND_STROKE = new Stroke() {
			@Override
			public Shape createStrokedShape(Shape shape) {
				Area area = new Area(FAT.createStrokedShape(shape));
				area.subtract(new Area(SKINNY_DOTTED.createStrokedShape(shape)));
				return area;
			}
		};

		@Override
		public Stroke transform(E input) {
			DracEdge edge = (DracEdge) input;

			if (edge.isManual()) {
				return DOTTED;
			}

			else if (edge.hasEclipsedManualLink()) {
				return COMPOUND_STROKE;
			}

			return NORMAL;
		}
	}

	static class DracVertexLabelRender extends DefaultVertexLabelRenderer {

		protected static Border focusBorder = new BevelBorder(BevelBorder.RAISED);

		/**
		 * @param pickedVertexLabelColor
		 */
		public DracVertexLabelRender(Color pickedVertexLabelColor) {
			super(pickedVertexLabelColor);
		}

		@Override
		public <V> Component getVertexLabelRendererComponent(JComponent vv,
		    Object value, Font font, boolean isSelected, V vertex) {
			// DracVertex v = (DracVertex) vertex;
			super.setForeground(vv.getForeground());

			if (isSelected) {
				setBorder(focusBorder);
			}
			else {
				setBorder(noFocusBorder);
			}

			if (font != null) {
				setFont(font);
			}
			else {
				setFont(vv.getFont());
			}
			setIcon(null);

			setValue(value);
			return this;
		}
	}

	static final class EdgeToToolTip<V> implements Transformer<V, String> {
		@Override
		public String transform(V edge) {
			DracEdge e = (DracEdge) edge;
			final String edgeToolTip = "Edge from " + e.getSource().getLabel() + ":"
			    + e.getSourceAid() + " to " + e.getTarget().getLabel() + ":"
			    + e.getTargetAid();
			return edgeToolTip;
		}
	}

	class PopupGraphMousePlugin extends AbstractPopupGraphMousePlugin {
		private final JFrame parent;

		public PopupGraphMousePlugin(JFrame parent) {
			this(parent, InputEvent.BUTTON3_MASK);
		}

		public PopupGraphMousePlugin(JFrame parent, int modifiers) {
			super(modifiers);
			this.parent = parent;
		}

		@Override
		protected void handlePopup(MouseEvent e) {
			
			final VisualizationViewer<DracVertex, DracEdge> vv = (VisualizationViewer<DracVertex, DracEdge>) e
			    .getSource();
			Point2D p = e.getPoint();

			GraphElementAccessor<DracVertex, DracEdge> pickSupport = vv
			    .getPickSupport();
			if (pickSupport != null) {
				
				DracVertex v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(),
				    p.getY());
				if (v != null) {
					
					TopologyPopupMenu popup = new TopologyPopupMenu(jung, parent, v, vv);
					popup.show(vv, e.getX(), e.getY());
					return;
				}

				DracEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(),
				    p.getY());
				if (edge != null) {
					
					TopologyPopupMenu popup = new TopologyPopupMenu(jung, parent, edge,
					    vv);
					popup.show(vv, e.getX(), e.getY());
					return;

				}

				
				TopologyPopupMenu popup = new TopologyPopupMenu(jung, parent, vv);
				popup.show(vv, e.getX(), e.getY());
				return;
			}
		}
	}

	static class VertexPainter<V> implements Transformer<V, Paint> {

		private final PickedInfo<V> pi;

		public VertexPainter(PickedInfo<V> pickedInfo) {
			pi = pickedInfo;
		}

		@Override
		public Paint transform(V v) {
			if (pi.isPicked(v)) {
				return Color.YELLOW;
			}
			return statusToColor(((DracVertex) v).getStatus());
		}

		private Color statusToColor(NeStatus status) {
			switch (status) {
			case NE_DELETED:
				return Color.BLACK;
			case NE_UNKNOWN:
				return Color.YELLOW;
			case NE_NOT_PROVISION:
				return Color.YELLOW;
			case NE_CREATED:
				return Color.YELLOW;
			case NE_NOT_CONNECT:
				return Color.CYAN;
			case NE_NOT_AUTHENTICATED:
				return Color.YELLOW;
			case NE_ASSOCIATED:
				return Color.YELLOW;
			case NE_ALIGNED:
				return Color.LIGHT_GRAY;
			default:
				return Color.BLACK;
			}
		}
	}

	static final class VertexToLabel<V> implements Transformer<V, String> {
		@Override
		public String transform(V v) {
			return ((DracVertex) v).getDisplayString();
		}
	}

	static final class VertexToToolTip<V> implements Transformer<V, String> {
		@Override
		public String transform(V v) {
			return "<html><center>" + ((DracVertex) v).getDisplayString() + " ("
			    + ((DracVertex) v).getStatus() + ")";
		}
	}

	private final ArrayList<TopologyViewEventHandler> listeners = new ArrayList<TopologyViewEventHandler>();
	private final ScalingControl scaler = new CrossoverScalingControl();
	private final JungTopologyPanel jung;
	protected final VisualizationViewer<DracVertex, DracEdge> visualizationViewer;
	private final JPanel graphPanel;
	private final JungGraphPreferences graphPrefs = new JungGraphPreferences(this);
	private final Transformer<DracVertex, String> defaultVertexLabelTransformer;
	private final Transformer<DracVertex, Shape> defaultVertexShapeTransformer = new ConstantTransformer(
	    new Ellipse2D.Float(-10, -10, 20, 20));
	private final Transformer<DracVertex, Shape> smallVertexShapeTransformer = new ConstantTransformer(
	    new Ellipse2D.Float(-4, -4, 8, 8));

	private final Renderer.VertexLabel<DracVertex, DracEdge> defaultVertexLabelRenderer;
	private DefaultModalGraphMouse<DracVertex, DracEdge> graphMouse;
	private Layout<DracVertex, DracEdge> layout;
	private static NetworkGraph networkGraph;

	public JungTopologyPanel(JFrame parent, NetworkGraph networkGraph)
	    throws Exception {
		// initialize fields
		JungTopologyPanel.networkGraph = networkGraph;
		jung = this;

		visualizationViewer = createVisualizationViewer(parent, networkGraph);

		setBackgroundImage("/client/Images/netherlands.png");

		/**
		 * VERTEX RELATED
		 */
		visualizationViewer.getRenderContext().setVertexFillPaintTransformer(
		    new VertexPainter<DracVertex>(visualizationViewer.getPickedVertexState()));
		visualizationViewer.getRenderContext().setVertexLabelRenderer(
		    new DracVertexLabelRender(Color.BLACK));
		visualizationViewer.setVertexToolTipTransformer(new VertexToToolTip<DracVertex>());
		defaultVertexLabelTransformer = visualizationViewer.getRenderContext()
		    .getVertexLabelTransformer();
		defaultVertexLabelRenderer = visualizationViewer.getRenderer().getVertexLabelRenderer();
		graphPrefs.setNeLabel(NeLabels.INSIDE);

		/*
		 * EDGE related
		 */
		visualizationViewer.getRenderContext().setEdgeStrokeTransformer(
		    new DracEdgeWeightStrokeFunction<DracEdge>());
		visualizationViewer.setEdgeToolTipTransformer(new EdgeToToolTip<DracEdge>());

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(visualizationViewer, 1.1f, visualizationViewer.getCenter());
			}
		});
		plus.setToolTipText("Zoom in");
		JButton minus = new JButton("-");
		minus.setName("minus");
		minus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(visualizationViewer, 1 / 1.1f, visualizationViewer.getCenter());
			}
		});
		minus.setToolTipText("Zoom out");

		JButton save = createSaveButton();

		JButton reset = new JButton("reset");
		reset.setName("reset");
		reset.setToolTipText("resets graph, use shift-reset to request NE list from server");
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// Shift reset will reload the graph from the server, a
					// complete reset.
					if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
						new GetGraphDataWorker().execute();
					}

					JungTopologyPanel.networkGraph.resetGraph();
				}
				catch (Exception ex) {
					log.error("Failed to reset graph ", ex);
				}
			}
		});

		JButton manualButton = new JButton("User Manual");
		manualButton.setName("manual");
		manualButton.setToolTipText("Open the user manual");
		manualButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				    Desktop desktop = Desktop.getDesktop();
				    desktop.open(getHelpFile());

				}
				catch (Exception ex) {
					log.error("Failed to reset graph ", ex);
				}
			}
		});
		JComboBox modeBox = graphMouse.getModeComboBox();
		graphMouse.setMode(Mode.PICKING);
		modeBox.addItemListener(((DefaultModalGraphMouse<DracVertex, DracEdge>) visualizationViewer
		    .getGraphMouse()).getModeListener());

		JPanel topPanel = new JPanel(new GridLayout(1, 1));
		JPanel controlPanel = new JPanel();
		controlPanel.add(plus);
		controlPanel.add(minus);
		controlPanel.add(modeBox);
		controlPanel.add(reset);
		controlPanel.add(save);
		controlPanel.add(manualButton);
		topPanel.add(controlPanel);

		graphPanel = new JPanel();
		graphPanel.setBackground(Color.WHITE);
		graphPanel.setLayout(new BorderLayout());
		graphPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		graphPanel.add(topPanel, BorderLayout.NORTH);
		graphPanel.add(visualizationViewer, BorderLayout.CENTER);
		// Add last so we don't get events before we are ready.
		JungTopologyPanel.networkGraph.addViewer(this);
		updateGraphPreferences();
	}

	private JButton createSaveButton() {
		JButton save = new JButton("save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveNetworkElementPositions();
			}
		});

		return save;
	}

	protected void saveNetworkElementPositions() {
		Collection<DracVertex> vertices = layout.getGraph().getVertices();

		for (DracVertex vertex : vertices) {
			Point2D location = layout.transform(vertex);
			vertex.setPositionX(location.getX());
			vertex.setPositionY(location.getY());

			try {
				new ServerOperation().updateNetworkElementPosition(vertex.getIp(),
				    vertex.getPort(), vertex.getPositionX(), vertex.getPositionY());
			}
			catch (Exception e) {
				log.error(
				    String.format("Failed to update positioning on vertex %s", vertex),
				    e);
			}
		}
	}

	private VisualizationViewer<DracVertex, DracEdge> createVisualizationViewer(
	    JFrame parent, NetworkGraph networkGraph) {
		DefaultModalGraphMouse<DracVertex, DracEdge> graphMouse = createGraphMouse(parent);

		layout = createLayout(networkGraph);
		VisualizationViewer<DracVertex, DracEdge> vv = new VisualizationViewer<DracVertex, DracEdge>(
		    layout);
		vv.setName("visualizationViewer");
		vv.setGraphMouse(graphMouse);
		vv.addKeyListener(graphMouse.getModeKeyListener());
		vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

		return vv;
	}

	private DefaultModalGraphMouse<DracVertex, DracEdge> createGraphMouse(
	    JFrame parent) {
		this.graphMouse = new DefaultModalGraphMouse<DracVertex, DracEdge>();
		this.graphMouse.add(new PopupGraphMousePlugin(parent));
		return this.graphMouse;
	}

	private Layout<DracVertex, DracEdge> createLayout(NetworkGraph networkGraph) {
		final Transformer<DracVertex, Point2D> transformer = createVertexTransformer();

		final StaticLayout<DracVertex, DracEdge> result = new StaticLayout<DracVertex, DracEdge>(
		    networkGraph.getGraph(), transformer);
		result.setInitializer(transformer);
		return result;
	}

	private Transformer<DracVertex, Point2D> createVertexTransformer() {
		return new Transformer<DracVertex, Point2D>() {

			private Collection<Point2D> usedPositions = new ArrayList<Point2D>();

			@Override
			public Point2D transform(DracVertex vertex) {
				Double positionX = vertex.getPositionX();
				Double positionY = vertex.getPositionY();

				Point2D result = determineNextPoint(positionX, positionY);

				usedPositions.add(result);
				return result;
			}

			private Point2D determineNextPoint(Double positionX, Double positionY) {
				if (positionX != 0 && positionY != 0) {
					return new Point2D.Double(positionX, positionY);
				}

				Point2D result = null;
				do {
					result = new Point2D.Double(positionX, positionY);
					positionX += 60;
				}
				while (usedPositions.contains(result));

				return result;
			}
		};
	}

	public void addListener(TopologyViewEventHandler listener) {
		listeners.add(listener);
	}

	/**
	 * Clears all selected vertex and edges.
	 */
	public synchronized void clearHighLight() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				visualizationViewer.getRenderContext().getPickedVertexState().clear();
				visualizationViewer.getRenderContext().getPickedEdgeState().clear();

			}
		});
	}

	public JungGraphPreferences getGraphPreferences() {
		return graphPrefs;
	}

	public JPanel getPanel() {
		return graphPanel;
	}

	/**
	 * Selects an edge. Previously selected vertex/edges are not cleared unless
	 * you call the clearHighLight method first.
	 */
	public synchronized void highlight(final DracEdge e, final Paint color) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// If color is non-null we'll use that to paint it, otherwise
				// fall back on the default color.
				e.setPaintColor(color);
				visualizationViewer.getRenderContext().getPickedEdgeState().pick(e, true);
			}
		});
	}

	/**
	 * Selects a vertex. Previously selected vertex/edges are not cleared unless
	 * you call the clearHighLight method first.
	 */
	public synchronized void highlight(final DracVertex v) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				visualizationViewer.getRenderContext().getPickedVertexState().pick(v, true);
			}
		});
	}

	public void notifyListeners(EVENT_TYPE eventid, Object object) {
		for (TopologyViewEventHandler l : listeners) {
			l.handleTopologyViewEvents(eventid, object);
		}
	}

	public void refreshGraphRequired() {
		if (SwingUtilities.isEventDispatchThread()) {
			visualizationViewer.repaint();
		}
		else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					visualizationViewer.repaint();
				}
			});
		}

	}

	public synchronized void resetGraph(final boolean full) throws Exception {
		visualizationViewer.getRenderContext().getMultiLayerTransformer()
		    .getTransformer(Layer.LAYOUT).setToIdentity();
		visualizationViewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
		    .setToIdentity();
		visualizationViewer.getGraphLayout().setInitializer(createVertexTransformer());
	}

	/**
	 * Loads a background image for the jung graph
	 */
	public void setBackgroundImage(String imageLocation) {
		try {
			final ImageIcon mapIcon = new ImageIcon(getClass().getResource(
			    imageLocation));

			if (mapIcon != null) {
				visualizationViewer.addPreRenderPaintable(new VisualizationViewer.Paintable() {
					@Override
					public void paint(Graphics g) {
						Graphics2D g2d = (Graphics2D) g;
						AffineTransform oldXform = g2d.getTransform();
						AffineTransform lat = visualizationViewer.getRenderContext()
						    .getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
						    .getTransform();
						AffineTransform vat = visualizationViewer.getRenderContext()
						    .getMultiLayerTransformer().getTransformer(Layer.VIEW)
						    .getTransform();
						AffineTransform at = new AffineTransform();
						at.concatenate(g2d.getTransform());
						at.concatenate(vat);
						at.concatenate(lat);
						g2d.setTransform(at);
						g.drawImage(mapIcon.getImage(), 0, 0, mapIcon.getIconWidth(),
						    mapIcon.getIconHeight(), visualizationViewer);
						g2d.setTransform(oldXform);
					}

					@Override
					public boolean useTransform() {
						return false;
					}
				});
			}
		}
		catch (Exception ex) {
			log.error("Can't load background image " + imageLocation);
		}
	}

	public void updateGraphPreferences() throws Exception {
		VertexLabelAsShapeRenderer<DracVertex, DracEdge> vlasr = new VertexLabelAsShapeRenderer<DracVertex, DracEdge>(
		    visualizationViewer.getRenderContext());
		NeLabels ne = graphPrefs.getNeLabel();
		switch (ne) {
		case OFF_NORMAL:
			visualizationViewer.getRenderer().setVertexLabelRenderer(defaultVertexLabelRenderer);
			visualizationViewer.getRenderContext().setVertexLabelTransformer(
			    defaultVertexLabelTransformer);
			visualizationViewer.getRenderContext().setVertexShapeTransformer(
			    defaultVertexShapeTransformer);
			break;
		case OFF_SMALL:
			visualizationViewer.getRenderer().setVertexLabelRenderer(defaultVertexLabelRenderer);
			visualizationViewer.getRenderContext().setVertexLabelTransformer(
			    defaultVertexLabelTransformer);
			visualizationViewer.getRenderContext().setVertexShapeTransformer(
			    smallVertexShapeTransformer);
			break;
		case INSIDE:
			// customize the render context
			visualizationViewer.getRenderer().setVertexLabelRenderer(vlasr);
			visualizationViewer.getRenderContext().setVertexLabelTransformer(
			// this chains together Transformers so that the html tags
			// are prepended to the toString method output
			    new ChainedTransformer<DracVertex, String>(new Transformer[] {
			        new VertexToLabel<DracVertex>(),
			        new Transformer<String, String>() {
				        @Override
				        public String transform(String input) {
					        return "<html><center>" + input;
				        }
			        } }));

			visualizationViewer.getRenderContext().setVertexShapeTransformer(vlasr);
			break;
		case OUTSIDE_NORMAL:
			visualizationViewer.getRenderer().setVertexLabelRenderer(defaultVertexLabelRenderer);
			visualizationViewer.getRenderContext().setVertexLabelTransformer(
			// this chains together Transformers so that the html tags
			// are prepended to the toString method output
			    new ChainedTransformer<DracVertex, String>(new Transformer[] {
			        new VertexToLabel<DracVertex>(),
			        new Transformer<String, String>() {
				        @Override
				        public String transform(String input) {
					        return "<html><center>" + input;
				        }
			        } }));
			visualizationViewer.getRenderContext().setVertexShapeTransformer(
			    defaultVertexShapeTransformer);
			break;
		case OUTSIDE_SMALL:
			visualizationViewer.getRenderer().setVertexLabelRenderer(defaultVertexLabelRenderer);
			visualizationViewer.getRenderContext().setVertexLabelTransformer(
			// this chains together Transformers so that the html tags
			// are prepended to the toString method output
			    new ChainedTransformer<DracVertex, String>(new Transformer[] {
			        new VertexToLabel<DracVertex>(),
			        new Transformer<String, String>() {
				        @Override
				        public String transform(String input) {
					        return "<html><center>" + input;
				        }
			        } }));
			visualizationViewer.getRenderContext().setVertexShapeTransformer(
			    smallVertexShapeTransformer);
			break;
		default:
			log.error("unknown NeLabels state " + ne);
		}

		// Edge line style
		switch (graphPrefs.getEdgeLineStyle()) {
		case LINE_CUBIC_CURVE:
			visualizationViewer.getRenderContext().setEdgeShapeTransformer(
			    new EdgeShape.CubicCurve<DracVertex, DracEdge>());
			break;
		case LINE_LINE:
			visualizationViewer.getRenderContext().setEdgeShapeTransformer(
			    new EdgeShape.Line<DracVertex, DracEdge>());
			break;
		case LINE_QUAD_CURVE:
			// Recommended as it displays multiple edges between the same
			// vertex.
			visualizationViewer.getRenderContext().setEdgeShapeTransformer(
			    new EdgeShape.QuadCurve<DracVertex, DracEdge>());
			break;
		default:
			log.error("unknown edge line style " + graphPrefs.getEdgeLineStyle());
		}

		// Edge colors
		visualizationViewer.getRenderContext().setEdgeDrawPaintTransformer(
		    new DracEdgePaintTransformer<DracEdge>(visualizationViewer.getPickedEdgeState(),
		        graphPrefs));

		resetGraph(true);
	}

	public static void resetTopology() {
		try {
			new GetGraphDataWorker().execute();
			networkGraph.resetGraph();
		}
		catch (Exception e) {
			log.error("Error during topology reset: ", e);
		}
	}
	
	private File getHelpFile() throws IOException {		
		File helpFile = new File("manual.pdf");
		InputStream inputStream = this.getClass().getResourceAsStream("/docs/adminconsole_manual_opendrac.pdf");
		OutputStream out = new FileOutputStream(helpFile);
		byte buf[] = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) > 0)
			out.write(buf, 0, len);
		out.close();
		inputStream.close();
		return helpFile;
	}
}
