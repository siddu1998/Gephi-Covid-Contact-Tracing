package org.gephi.toolkit.demos;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.EgoBuilder;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.AppendProcessor;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import sun.font.FontScaler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sid
 */
public class singleEgoCSV {
    
     public void script() {


            
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        AppearanceModel appearanceModel = Lookup.getDefault().lookup(AppearanceController.class).getModel();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);

        Container container,container2;
    try {
        File file_node = new File(getClass().getResource("/org/gephi/toolkit/demos/44_two_hop_node.csv").toURI());
        container = importController.importFile(file_node);
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);   //Force DIRECTED
        container.getLoader().setAllowAutoNode(true);  //create missing nodes
        container.getLoader().setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        container.getLoader().setAutoScale(true);

        File file_edge = new File(getClass().getResource("/org/gephi/toolkit/demos/44_two_hop_edge.csv").toURI());
        container2 = importController.importFile(file_edge);
        container2.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED); 
        container2.getLoader().setAllowAutoNode(true);  //create missing nodes
        container2.getLoader().setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        container2.getLoader().setAutoScale(true);

    } catch (Exception ex) {
        ex.printStackTrace();
        return;
    }

        //Append imported data to GraphAPI
    importController.process(container, new DefaultProcessor(), workspace);
    importController.process(container2, new AppendProcessor(), workspace);

        
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        
        UndirectedGraph graph = graphModel.getUndirectedGraphVisible();
        //System.out.println("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());
        //Rank size by centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graphModel);
        
        Column centralityColumn = graphModel.getNodeTable().getColumn("Degree");
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking.getTransformer();
        centralityTransformer.setMinSize(10);
        centralityTransformer.setMaxSize(30);
        appearanceController.transform(centralityRanking);

        
        
        System.out.println("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());

                
 
        
    
        String query_node_id= "44";
        Node node_ = graph.getNode(query_node_id);
        System.out.println(query_node_id);
        
         EgoBuilder.EgoFilter egoFilter = new EgoBuilder.EgoFilter();
        
        //String query_node= node_list.get(node);
       // System.out.println(query_node);
        
        egoFilter.setPattern("44"); //Regex accepted
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
        for (int i = 0; i < 200 && layout.canAlgo(); i++) {
         layout.goAlgo();
        }
       
 
      
//
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        PreviewProperties prop = model.getProperties();        
        //prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLACK));
        prop.putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        //prop.putValue(PreviewProperty.NODE_LABEL_FONT, prop.getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(10));
            
        //Node primary_node = graph.getNode(query_node);
        Color original_color=node_.getColor();
        node_.setLabel("Me");
        
        
        
        ArrayList<Node> one_hop = new ArrayList<Node>();
        ArrayList<Node> second_hop = new ArrayList<Node>();
        //nodes in one hop of primary_node 
         
        for (Node n :  graph.getNeighbors(node_).toArray())
        {
            one_hop.add(n);
            n.setColor(new Color(0x588BAE));
        }
        
        for (Node oh : one_hop)
        {
            
                for(Node sh : graph.getNeighbors(oh))
                {
                    if(one_hop.contains(sh)==false)
                    {
                        
                        sh.setColor(new Color(0xC2DFFF));
                    }
                
                }
        }
    
     
        
        
        node_.setColor(new Color(0x8B008B));
        
     
        
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        
        try {
            String path = "/home/sid/Desktop/CODE/Internship-Indian-Institute-of-Science/COVID IISc/gephi_graphs/"+node_.getId().toString()+"_2_curve"+".png";
            ec.exportFile(new File(path));

        } catch (IOException ex) {
           ex.printStackTrace();
           return;
        }

       node_.setLabel((""));
       
    
        
        
   
        
        
    }
    
}
