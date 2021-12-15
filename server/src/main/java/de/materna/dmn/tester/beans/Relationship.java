package de.materna.dmn.tester.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import de.materna.dmn.tester.enums.RelationshipType;

@Entity
@Table(name = "RELATIONSHIP")
public class Relationship {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "USER")
	private String user;
	@Column(name = "LABORATORY")
	private String laboratory;
	@Column(name = "WORKPLACE")
	private String workplace;
	@Column(name = "TYPE")
	private RelationshipType type;

	public Relationship() {
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}

	public String getWorkplace() {
		return workplace;
	}

	public void setWorkplace(String workplace) {
		this.workplace = workplace;
	}

	public RelationshipType getType() {
		return type;
	}

	public void setType(RelationshipType type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

}