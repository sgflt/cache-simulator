/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.shared;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Pavel Bzoch
 * Trida pro ulozeni kongigurace do souboru
 */
public class ConfigReaderWriter {
  
    /**
     * metoda pro yapis konfigurace do xml souboru
     */
    public static void write() {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter("config.xml"));

            writer.writeStartDocument();
            writer.writeStartElement("Config");
            
            writer.writeStartElement("workingDir");
            writer.writeAttribute("dir", GlobalVariables.getActDir());
            writer.writeEndElement();
            
            writer.writeStartElement("requestCountForRandomGenerator");
            writer.writeAttribute("value", Integer.toString(GlobalVariables.getRequestCountForRandomGenerator()));
            writer.writeEndElement();
            
            writer.writeStartElement("generateRandomSizesAFS");
            writer.writeAttribute("value", Boolean.toString(GlobalVariables.isRandomFileSizesForLoggedData()));
            writer.writeEndElement();
            
            writer.writeStartElement("gaussianGeneratorParameters");
            writer.writeAttribute("dispersion", Integer.toString(GlobalVariables.getFileRequestGeneratorDispersion()));
            writer.writeAttribute("meanValue", Integer.toString(GlobalVariables.getFileRequestGeneratorMeanValue()));
            writer.writeEndElement();
            
            writer.writeStartElement("prefRandomGeneratorParameters");
            writer.writeAttribute("preferenceFile", Integer.toString(GlobalVariables.getFileRequestPreferenceFile()));
            writer.writeAttribute("preferenceStep", Integer.toString(GlobalVariables.getFileRequestPreferenceStep()));
            writer.writeAttribute("nonPreferenceStep", Integer.toString(GlobalVariables.getFileRequestnNonPreferenceFile()));
            writer.writeEndElement();
            
            writer.writeStartElement("generatorsParameters");
            writer.writeAttribute("minValue", Integer.toString(GlobalVariables.getFileRequestGeneratorMinValue()));
            writer.writeAttribute("maxValue", Integer.toString(GlobalVariables.getFileRequestGeneratorMaxValue()));
            writer.writeAttribute("minSize", Integer.toString(GlobalVariables.getMinGeneratedFileSize()));
            writer.writeAttribute("maxSize", Integer.toString(GlobalVariables.getMaxGeneratedFileSize()));
            writer.writeEndElement();
            
            writer.writeStartElement("sendStatToServer");
            writer.writeAttribute("lfu-ss", Boolean.toString(GlobalVariables.isSendStatisticsToServerLFUSS()));
            writer.writeAttribute("lrfu-ss", Boolean.toString(GlobalVariables.isSendStatisticsToServerLRFUSS()));
            writer.writeEndElement();
            
            writer.writeStartElement("loadStatToServer");
            writer.writeAttribute("statToServer", Boolean.toString(GlobalVariables.isLoadServerStatistic()));
            writer.writeEndElement();
            
            writer.writeStartElement("simulatorSettings");
            writer.writeAttribute("limitForStatistics", Integer.toString(GlobalVariables.getLimitForStatistics()));
            writer.writeAttribute("averageNetworkSpeed", Integer.toString(GlobalVariables.getAverageNetworkSpeed()));
            writer.writeAttribute("cacheCapacityForDownloadWindow", Double.toString(GlobalVariables.getCacheCapacityForDownloadWindow()));
            writer.writeEndElement();
            
            writer.writeStartElement("zipfLambda");
            writer.writeAttribute("value", Double.toString(GlobalVariables.getZipfLambda()));
            writer.writeEndElement();
            
            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  
    
    public static void read(){
       	  try {
 
		File fXmlFile = new File("config.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
 
                Node node = doc.getElementsByTagName("workingDir").item(0);
                GlobalVariables.setActDir(node.getAttributes().item(0).getNodeValue());
                
                node = doc.getElementsByTagName("requestCountForRandomGenerator").item(0);
                GlobalVariables.setRequestCountForRandomGenerator(Integer.parseInt(node.getAttributes().item(0).getNodeValue()));
                
                node = doc.getElementsByTagName("generateRandomSizesAFS").item(0);
                GlobalVariables.setRandomFileSizesForLoggedData(Boolean.parseBoolean(node.getAttributes().item(0).getNodeValue()));
                
                node = doc.getElementsByTagName("zipfLambda").item(0);
                GlobalVariables.setZipfLambda(Double.parseDouble(node.getAttributes().item(0).getNodeValue()));
                
                node = doc.getElementsByTagName("gaussianGeneratorParameters").item(0);
                NamedNodeMap nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);
                    if (node.getNodeName().equals("dispersion")){
                        GlobalVariables.setFileRequestGeneratorDispersion(Integer.parseInt(node.getNodeValue()));
           
                    }
                    else if (node.getNodeName().equals("meanValue")){
                        GlobalVariables.setFileRequestGeneratorMeanValue(Integer.parseInt(node.getNodeValue()));
                    }
                }
                
                node = doc.getElementsByTagName("prefRandomGeneratorParameters").item(0);
                nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);
                    if (node.getNodeName().equals("preferenceFile")){
                        GlobalVariables.setFileRequestPreferenceFile(Integer.parseInt(node.getNodeValue()));
           
                    }
                    else if (node.getNodeName().equals("preferenceStep")){
                        GlobalVariables.setFileRequestPreferenceStep(Integer.parseInt(node.getNodeValue()));
                    }
                    else if (node.getNodeName().equals("nonPreferenceStep")){
                        GlobalVariables.setFileRequestnNonPreferenceFile(Integer.parseInt(node.getNodeValue()));
                    }
                }
                
                node = doc.getElementsByTagName("generatorsParameters").item(0);
                nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);
                    if (node.getNodeName().equals("minValue")){
                        GlobalVariables.setFileRequestGeneratorMinValue(Integer.parseInt(node.getNodeValue()));
           
                    }
                    else if (node.getNodeName().equals("maxValue")){
                        GlobalVariables.setFileRequestGeneratorMaxValue(Integer.parseInt(node.getNodeValue()));
                    }
                    else if (node.getNodeName().equals("minSize")){
                        GlobalVariables.setMinGeneratedFileSize(Integer.parseInt(node.getNodeValue()));
                    }
                    else if (node.getNodeName().equals("maxSize")){
                        GlobalVariables.setMaxGeneratedFileSize(Integer.parseInt(node.getNodeValue()));
                    }
                }
                
                node = doc.getElementsByTagName("sendStatToServer").item(0);
                nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);
                    if (node.getNodeName().equals("lfu-ss")){
                        GlobalVariables.setSendStatisticsToServerLFUSS(Boolean.parseBoolean(node.getNodeValue()));
           
                    }
                    else if (node.getNodeName().equals("lrfu-ss")){
                        GlobalVariables.setSendStatisticsToServerLRFUSS(Boolean.parseBoolean(node.getNodeValue()));
                    }
                }
                
                node = doc.getElementsByTagName("loadStatToServer").item(0);
                nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);
                    if (node.getNodeName().equals("statToServer")){
                        GlobalVariables.setLoadServerStatistic(Boolean.parseBoolean(node.getNodeValue()));
                    }
                }
                
                node = doc.getElementsByTagName("simulatorSettings").item(0);
                nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++){
                    node = nodeMap.item(i);

                    if (node.getNodeName().equals("limitForStatistics")){
                        GlobalVariables.setLimitForStatistics(Integer.parseInt(node.getNodeValue()));
                    }
                    else if (node.getNodeName().equals("averageNetworkSpeed")){
                        GlobalVariables.setAverageNetworkSpeed(Integer.parseInt(node.getNodeValue()));
                    }
                    else if (node.getNodeName().equals("cacheCapacityForDownloadWindow")){
                        GlobalVariables.setCacheCapacityForDownloadWindow((int)(Double.parseDouble(node.getNodeValue())*100));
                    }
                }

	  } catch (Exception e) {
		e.printStackTrace();
	  }
  }
}
