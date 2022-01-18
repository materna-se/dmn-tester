package de.materna.dmn.tester.beans.workspace;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import de.materna.dmn.tester.enums.VisabilityType;

@Entity
@Table(name = "workspace", uniqueConstraints = @UniqueConstraint(columnNames = { "uuid" }))
public class Workspace {
	@Id
	@Column(name = "uuid", unique = true, nullable = false)
	private String uuid;
	@Column(name = "name")
	@NotEmpty(message = "The name of the Workspace cannot be empty")
	private String name;
	@Column(name = "visability")
	private VisabilityType visability;

	public Workspace() {
	}

	public Workspace(String name) {
		this(name, VisabilityType.PRIVATE);
	}

	public Workspace(String name, VisabilityType visability) {
		this.uuid = UUID.randomUUID().toString();
		this.name = name;
		this.visability = visability;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VisabilityType getVisability() {
		return visability;
	}

	public void setVisability(VisabilityType visability) {
		this.visability = visability;
	}

}