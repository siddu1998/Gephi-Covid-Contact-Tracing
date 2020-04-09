/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.toolkit.demos;
import java.util.*; 
  
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;

import com.itextpdf.text.PageSize;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.filters.plugin.graph.EgoBuilder.EgoFilter;
import org.gephi.filters.plugin.operator.INTERSECTIONBuilder.IntersectionOperator;
import org.gephi.filters.plugin.partition.PartitionBuilder.NodePartitionFilter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

/**
 * This demo shows how to create and execute filter queries.
 * <p>
 * The demo creates three filters queries and execute them:
 * <ul><li>Filter degrees, remove nodes with degree < 10</li>
 * <
 * li>Filter with partition, keep nodes with 'source' column equal to
 * 'Blogorama'</li>
 * <li>Intersection between degrees and partition, AND filter with two precedent
 * filters</li>
 * <li>Ego filter</li></ul>
 * <p>
 * When a filter query is executed, it creates a new graph view, which is a copy
 * of the graph structure that went through the filter pipeline. Several filters
 * can be chained by setting sub-queries. A query is a tree where the root is
 * the last executed filter.
 *
 * @author Mathieu Bastian
 */
public class BatchEgo {

    public void script(String filePath,String outputFolderPath) {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        AppearanceModel appearanceModel = Lookup.getDefault().lookup(AppearanceController.class).getModel();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);

        //Import file
        Container container;
        try {
            
            File file = new File(getClass().getResource(filePath).toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        
        UndirectedGraph graph = graphModel.getUndirectedGraphVisible();
        
        
//        
//        //Rank color by Degree
//        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingElementColorTransformer.class);
//        RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking.getTransformer();
//        degreeTransformer.setColors(new Color[]{new Color(0x7CFC00), new Color(0x006400)});
//        degreeTransformer.setColorPositions(new float[]{0f, 1f});
//        appearanceController.transform(degreeRanking);

        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        //Rank size by centrality
        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking.getTransformer();
        centralityTransformer.setMinSize(3);
        centralityTransformer.setMaxSize(10);
        appearanceController.transform(centralityRanking);
        
        
        
        
     
                
                
        //DS with all the node names
        ArrayList<String> node_list = new ArrayList<String>();
        for (Node n : graphModel.getGraph().getNodes())
        {
            String node_name = n.getAttribute("Id").toString();
            node_list.add(node_name);             
        }
        

        
        //iterating over each node name
        for(int node=0;node<node_list.size();node++)
        {
        //Restoring graph from previous filters
        graph = graphModel.getUndirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());
        
        //Ego filter
        EgoFilter egoFilter = new EgoFilter();
        
        String query_node= node_list.get(node);
        System.out.println(query_node);
        
        egoFilter.setPattern(query_node); //Regex accepted
        egoFilter.setDepth(2);
        Query queryEgo = filterController.createQuery(egoFilter);
        GraphView viewEgo = filterController.filter(queryEgo);
        graphModel.setVisibleView(viewEgo);    //Set the filter result as the visible view
        
        
        graph = graphModel.getUndirectedGraphVisible();
        
        
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);
        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
         layout.goAlgo();
        }
        
        
        
        System.out.println("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());
        
        Node primary_node = graphModel.getGraph().getNode(query_node);
        Color original_color=primary_node.getColor();
        primary_node.setColor(Color.MAGENTA);
        

        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        PreviewProperties prop = model.getProperties();        
        prop.putValue(PreviewProperty.NODE_LABEL_FONT, prop.getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
        prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.DARK_GRAY));
        prop.putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        
        
       
        
        
        
     
       
                
        
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            String path = outputFolderPath+query_node+"_"+".png";
            ec.exportFile(new File(path));

        } catch (IOException ex) {
           ex.printStackTrace();
           return;
        }
       
        
       primary_node.setColor(original_color);

               }
        
        
   
        
        
    }
}
