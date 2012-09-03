package br.gov.serpro.aplicacao_demoiselle1_ejb.bean;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import br.gov.component.demoiselle.common.pojo.extension.IPojoExtension;

@Entity
public class Bookmark implements IPojoExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = SEQUENCE)
	private Long id;

	@Column
	private String link;

	public Bookmark() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String toLog() {
		return toString();
	}

}
