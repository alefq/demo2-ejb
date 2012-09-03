package br.gov.serpro.aplicacao_demoiselle2_EJB.business;

import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.template.DelegateCrud;
import br.gov.serpro.aplicacao_demoiselle2_EJB.domain.Link;
import br.gov.serpro.aplicacao_demoiselle2_EJB.persistence.LinkDAO;

@BusinessController
public class LinkBC extends DelegateCrud<Link, Long, LinkDAO> {
	
	private static final long serialVersionUID = 1L;
	
}
