package org.ecoinformatics.datamanager.quality;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;


public class QualityReport {
  
  /*
   * Class variables
   */
  
  /*
   *  Boolean switch to determine whether quality reporting is turned on or off
   *  in the application that is using the Data Manager library. It is the
   *  application's responsibility to set this value by reading in the value
   *  of the 'qualityReporting' property and calling the 
   *  QualityReport.setQualityReporting() static method, passing in the 
   *  appropriate value.
   */
  private static Boolean qualityReporting = new Boolean(false);
  

  /*
   * Instance variables
   */
  
  // The DataPackage object that this QualityReport is reporting on
  private DataPackage dataPackage;
  
  private String packageId;     // the eml packageId value
  private String dateCreated;   // the date this quality report was generated
  private ArrayList<QualityCheck> qualityChecks;
  private ArrayList<EntityReport> entityReports;
  
  
  /*
   * Constructors
   */
  
  public QualityReport(DataPackage dataPackage) {
    this.dataPackage = dataPackage;
    if (dataPackage != null) {
      this.packageId = dataPackage.getPackageId();
    }
    
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateCreated = simpleDateFormat.format(now);

    this.qualityChecks = new ArrayList<QualityCheck>();
    this.entityReports = new ArrayList<EntityReport>();
  }

  
  public QualityReport(String packageId) {
    this.packageId = packageId;
    
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateCreated = simpleDateFormat.format(now);

    this.qualityChecks = new ArrayList<QualityCheck>();
    this.entityReports = new ArrayList<EntityReport>();
  }

  
  /*
   * Class methods
   */
  
  /**
   * Returns the qualityReporting value, a Boolean. Other classes in the
   * Data Manager library call this method to determine whether quality
   * reporting operations should or should not be executed.
   * 
   * @return  qualityReporting. If true, quality reporting is turned on.
   */
  public static Boolean isQualityReporting() {
    return qualityReporting;
  }
  
  
  /**
   * Sets the value of qualityReporting using a string parameter. If the string
   * equals (ignore case) "true", then quality reporting is turned on. Any other 
   * string value results in quality reporting being turned off (the default
   * setting).
   * 
   * @param trueOrFalse   a string argument. "true" (ignore case) turns on quality
   *                      reporting
   */
  public static void setQualityReporting(String trueOrFalse) {
    Boolean aBoolean = new Boolean(trueOrFalse);
    qualityReporting = aBoolean;
  }
  
  
  /**
   * Sets the value of qualityReporting using a boolean parameter. If the
   * argument is true, then quality reporting is turned on. If false, then 
   * quality reporting is turned off.
   * 
   * @param trueOrFalse    true turns on quality reporting
   */
  public static void setQualityReporting(boolean trueOrFalse) {
    Boolean aBoolean = new Boolean(trueOrFalse);
    qualityReporting = aBoolean;
  }
  
  
  
  /*
   * Instance methods
   */

  /**
   * Adds a quality check to the list of quality checks that have been
   * performed on this data package.
   * 
   * @param qualityCheck    the new quality check to add to the list
   */
  public void addEntityReport(EntityReport entityReport) {
    entityReports.add(entityReport);
  }
  
  
  /**
   * Adds a quality check to the list of quality checks that have been
   * performed on this data package.
   * 
   * @param qualityCheck    the new quality check to add to the list
   */
  public void addQualityCheck(QualityCheck qualityCheck) {
    qualityChecks.add(qualityCheck);
  }
  
  
  public String getDateCreated() {
    return dateCreated;
  }


  public ArrayList<EntityReport> getEntityReports() {
    return entityReports;
  }


  public String getPackageId() {
    return packageId;
  }


  public ArrayList<QualityCheck> getQualityChecks() {
    return qualityChecks;
  }


  public void setDateCreated(String dateCreated) {
    this.dateCreated = dateCreated;
  }


  public void setEntityReports(ArrayList<EntityReport> entityReports) {
    this.entityReports = entityReports;
  }


  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }


  public void setQualityChecks(ArrayList<QualityCheck> qualityChecks) {
    this.qualityChecks = qualityChecks;
  }


  /**
   * Generates an XML quality report string from the quality check objects
   * and the entity report objects stored in the data package.
   * 
   * @return an XML string representation of the full quality report
   */
  public String toXML() {
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateCreated = simpleDateFormat.format(now);
    String xmlString = null;
    
    StringBuffer stringBuffer = new StringBuffer("");
    
    stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    stringBuffer.append("<qualityReport>\n");
    stringBuffer.append("  <creationDate>" + dateCreated + "</creationDate>\n");
    stringBuffer.append("  <packageId>" + packageId + "</packageId>\n");
    
    // Add quality checks at the data package metadata level
    if (qualityChecks != null && qualityChecks.size() > 0) {
      stringBuffer.append("  <metadataReport>\n");
      for (QualityCheck aQualityCheck : qualityChecks) {
        String qualityCheckXML = aQualityCheck.toXML();
        stringBuffer.append(qualityCheckXML);
      }
      stringBuffer.append("  </metadataReport>\n");
    }
    
    // Add entity reports and their quality checks at the entity data level
    if (entityReports != null && entityReports.size() > 0) {
      boolean isFullReport = false; // We want only the <entityReport> fragment
      for (EntityReport entityReport : entityReports) {
        String entityReportXML = entityReport.toXML(isFullReport);
        stringBuffer.append(entityReportXML);
      }
    }

    stringBuffer.append("</qualityReport>\n");
    xmlString = stringBuffer.toString();
    
    return xmlString;
  }

}
