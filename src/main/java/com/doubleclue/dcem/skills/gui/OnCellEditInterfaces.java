package com.doubleclue.dcem.skills.gui;

import org.primefaces.event.CellEditEvent;

import com.doubleclue.dcem.core.exceptions.DcemException;

public interface OnCellEditInterfaces {
	public void actionOnCellEdit(CellEditEvent<?> event) throws DcemException, Exception;

	public void listenShowAllSkills();
}
