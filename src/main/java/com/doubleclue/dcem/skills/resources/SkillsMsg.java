package com.doubleclue.dcem.skills.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.utils.ResourceBundleUtf8Control;

import com.doubleclue.dcem.skills.logic.SkillsModule;

public class SkillsMsg extends ResourceBundle {
	
	protected static final String BUNDLE_NAME = SkillsModule.RESOURCE_NAME;
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public SkillsMsg () {
    	setParent(ResourceBundle.getBundle(BUNDLE_NAME, 
                FacesContext.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
    }

	@Override
	protected Object handleGetObject(String key) {
		return parent.getObject(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		 return parent.getKeys();
	}
	
	

}
