package com.doubleclue.dcem.skills.model;

import java.util.List;

import com.doubleclue.dcem.skills.entities.SkillsComparableInterface;

public class SkillsCompareResult {

	private List<? extends SkillsComparableInterface> oldList;
	private List<? extends SkillsComparableInterface> newList;

	public SkillsCompareResult() {

	}
		
	public <T extends SkillsComparableInterface> SkillsCompareResult(List<T> oldList, List<T> newList) {
		this.oldList = oldList;
		this.newList = newList;
		
	}

	public List<? extends SkillsComparableInterface> getOldList() {
		return oldList;
	}

	public void setOldList(List<? extends SkillsComparableInterface> oldList) {
		this.oldList = oldList;
	}

	public List<? extends SkillsComparableInterface> getNewList() {
		return newList;
	}

	public void setNewList(List<? extends SkillsComparableInterface> newList) {
		this.newList = newList;
	}

}
