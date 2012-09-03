package br.gov.serpro.aplicacao_demoiselle2_EJB.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.frameworkdemoiselle.stereotype.PersistenceController;
import br.gov.frameworkdemoiselle.template.JPACrud;
import br.gov.serpro.aplicacao_demoiselle2_EJB.domain.Link;

@PersistenceController
public class LinkDAO extends JPACrud<Link, Long> {
	
	private static final long serialVersionUID = 1L;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public EntityManager getEntityManager() {
		return this.entityManager;
	}
	
}
