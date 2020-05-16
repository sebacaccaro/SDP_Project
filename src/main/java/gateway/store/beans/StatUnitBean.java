package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class StatUnitBean implements CloneInterface<StatUnitBean>{
    
    public StatUnitBean clone(){
        /* TODO: change this implementation */
        return this;
    }
}