/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2016                                           
 *                                                                                                                                 
 *  Creation Date: May 25, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.interfaces;

/**
 * Data interface for trigger definitions
 * 
 * @author miethaner
 */
public interface TriggerDefinitionRest {

    /**
     * Gets the resource ID
     * 
     * @return the resource ID
     */
    public String getResourceId();

    /**
     * Gets the name and description of the definition
     * 
     * @return the description string
     */
    public String getDescription();

    /**
     * Gets the target url of the trigger
     * 
     * @return the target url string
     */
    public String getTargetURL();

    /**
     * Returns true if the trigger is suspending
     * 
     * @return true if suspending
     */
    public Boolean isSuspending();

    /**
     * Gets the organization id of the owner
     * 
     * @return the organization id
     */
    public String getOwnerId();

    /**
     * Gets the corresponding organization object
     * 
     * @return
     */
    public OrganizationRest getOwner();

    /**
     * Gets the trigger action for the definition
     * 
     * @return the trigger action
     */
    public String getAction();

}
