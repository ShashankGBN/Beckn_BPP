package com.tibil.BecknBPP.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Candidate extends BaseModel {

	@Id
	@GeneratedValue
	private long id;
	private String name;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "candidate_designations", joinColumns = { @JoinColumn(name = "candidate_id") }, inverseJoinColumns = {
			@JoinColumn(name = "designation_id") })
	private Set<Designation> designations;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "candidate_skills", joinColumns = { @JoinColumn(name = "candidate_id") }, inverseJoinColumns = {
			@JoinColumn(name = "skill_id") })
	private Set<Skill> skills;
	private float ctc;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Skill> getSkills() {
		return skills;
	}

	public void setSkills(Set<Skill> skills) {
		this.skills = skills;
	}

	public Set<Designation> getDesignations() {
		return designations;
	}

	public void setDesignations(Set<Designation> designations) {
		this.designations = designations;
	}

	public float getCtc() {
		return ctc;
	}

	public void setCtc(float ctc) {
		this.ctc = ctc;
	}

	@Override
	public String toString() {
		return "Candidate [id=" + id + ", name=" + name + ", designations=" + designations + ", skills=" + skills
				+ ", ctc=" + ctc + "]";
	}

}
